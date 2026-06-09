package ru.pin123.courseprojectcpad.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pin123.courseprojectcpad.dao.PassengerDaoImpl;
import ru.pin123.courseprojectcpad.dao.StopDaoImpl;
import ru.pin123.courseprojectcpad.dao.TripDaoImpl;
import ru.pin123.courseprojectcpad.model.*;
import ru.pin123.courseprojectcpad.service.TicketingService;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Контроллер экрана продажи билетов.
 * Отвечает за выбор рейса, остановок, мест в салоне автобуса,
 * ввод и строгую валидацию данных пассажира, а также за непосредственное оформление билета
 * и отображение маршрутной квитанции (чека).
 */
public class TicketSellController implements Initializable {

    /** Логгер для фиксации событий продажи билетов, валидации данных и работы с UI. */
    private static final Logger logger = LoggerFactory.getLogger(TicketSellController.class);

    /** DAO для работы с пассажирами (поиск или создание). */
    private final PassengerDaoImpl passengerDao = new PassengerDaoImpl();
    /** DAO для работы с рейсами. */
    private final TripDaoImpl tripDao = new TripDaoImpl();
    /** DAO для работы с остановками. */
    private final StopDaoImpl stopDao = new StopDaoImpl();
    /** Сервис для бизнес-логики продажи билетов и проверки занятых мест. */
    private final TicketingService ticketingService = new TicketingService();

    @FXML private Button sellButton;
    /** Список доступных рейсов для выбора. */
    @FXML private ListView<Trip> tripsListView;
    /** Выпадающий список остановок маршрута с указанием стоимости проезда до них. */
    @FXML private ComboBox<StopItem> stopComboBox;
    /** Метка для отображения стоимости проезда до выбранной остановки. */
    @FXML private Label costLabel;
    /** Сетка для динамического отображения кнопок-мест в салоне автобуса. */
    @FXML private GridPane seatsGrid;
    /** Метка с пунктом отправления выбранного рейса. */
    @FXML private Label lblDeparturePoint;
    /** Метка с пунктом назначения выбранного рейса. */
    @FXML private Label lblDestinationPoint;
    /** Метка с длительностью поездки. */
    @FXML private Label lblDuration;

    /** Поле ввода фамилии пассажира. */
    @FXML private TextField tfLastName;
    /** Поле ввода имени пассажира. */
    @FXML private TextField tfFirstName;
    /** Поле ввода отчества пассажира. */
    @FXML private TextField tfMiddleName;
    /** Поле ввода паспортных данных (с автоматической маской). */
    @FXML private TextField tfPassport;

    /** Номер текущего выбранного места в салоне. */
    private Integer selectedSeatNumber = null;
    /** Ссылка на кнопку выбранного места для управления её визуальным состоянием. */
    private Button selectedSeatButton = null;

    /**
     * Инициализирует контроллер после загрузки FXML-файла.
     * Загружает список рейсов, настраивает слушатели событий для элементов интерфейса
     * и применяет маску ввода для поля паспорта.
     *
     * @param location  URL-адрес для разрешения относительных путей, или null.
     * @param resources Ресурсы для локализации, или null.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Инициализация экрана продажи билетов.");

        // 1. Загружаем все доступные рейсы из базы
        try {
            List<Trip> trips = tripDao.findAll();
            tripsListView.setItems(FXCollections.observableArrayList(trips));
            logger.info("Успешно загружено {} рейсов для продажи билетов.", trips.size());
        } catch (Exception e) {
            logger.error("Критическая ошибка при загрузке списка рейсов из БД.", e);
            showAlert(Alert.AlertType.ERROR, "Ошибка БД", "Не удалось загрузить рейсы");
        }

        // 2. Слушатели кликов
        tripsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) onTripSelected(newVal);
        });

        stopComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                costLabel.setText(newVal.getPrice().toString());
                logger.debug("Выбрана остановка: {}. Стоимость: {} руб.", newVal.getStop().getName(), newVal.getPrice());
            }
        });

        // 3. Подключаем умную маску для паспорта (Авто-пробел и лимит 10 цифр)
        setupPassportMask(tfPassport);
    }

    /**
     * Настраивает автоматическую маску ввода для поля паспортных данных.
     * Форматирует ввод в вид "0000 000000", оставляя только цифры и ограничивая ввод 10 символами.
     *
     * @param textField текстовое поле, к которому применяется маска.
     */
    private void setupPassportMask(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;

            String digits = newValue.replaceAll("[^\\d]", "");

            if (digits.length() > 10) {
                digits = digits.substring(0, 10);
            }

            StringBuilder formatted = new StringBuilder(digits);
            if (formatted.length() > 4) {
                formatted.insert(4, " ");
            }

            if (!newValue.equals(formatted.toString())) {
                textField.setText(formatted.toString());
                Platform.runLater(textField::end);
            }
        });
    }

    /**
     * Обрабатывает событие выбора рейса из списка.
     * Загружает информацию о маршруте, доступные остановки с расчетом стоимости
     * и отрисовывает схему свободных/занятых мест в салоне автобуса.
     *
     * @param trip выбранный объект рейса.
     */
    private void onTripSelected(Trip trip) {
        logger.debug("Обработка выбора рейса ID: {}.", trip != null ? trip.getTripId() : "null");
        try {
            if (trip == null || trip.getRoute() == null) {
                lblDeparturePoint.setText("—");
                lblDestinationPoint.setText("—");
                lblDuration.setText("—");
                stopComboBox.getItems().clear();
                costLabel.setText("0.00");
                seatsGrid.getChildren().clear();
                return;
            }

            Route route = trip.getRoute();
            lblDeparturePoint.setText(route.getDeparturePoint());
            lblDestinationPoint.setText(route.getDestinationPoint());
            lblDuration.setText(route.getFormattedDuration());

            List<Stop> allStops = stopDao.findAll();
            List<StopItem> stopItems = new ArrayList<>();
            BigDecimal basePrice = new BigDecimal("150.00");

            for (int i = 0; i < allStops.size(); i++) {
                BigDecimal price = basePrice.add(new BigDecimal(i * 100));
                stopItems.add(new StopItem(allStops.get(i), price));
            }
            stopComboBox.setItems(FXCollections.observableArrayList(stopItems));

            drawBusSeats(trip);
            logger.debug("Для рейса ID: {} загружено {} остановок и отрисована схема мест.", trip.getTripId(), stopItems.size());

        } catch (Exception e) {
            logger.error("Ошибка при выборе рейса ID [{}] и загрузке связанных данных.", trip.getTripId(), e);
            showAlert(Alert.AlertType.ERROR, "Ошибка загрузки", "Не удалось загрузить данные рейса или остановок:\n" + e.getMessage());
        }
    }

    /**
     * Отрисовывает схему мест в салоне автобуса в виде сетки кнопок.
     * Занятые места блокируются и выделяются красным, свободные остаются активными.
     *
     * @param trip рейс, для которого необходимо отрисовать места.
     */
    private void drawBusSeats(Trip trip) {
        seatsGrid.getChildren().clear();
        seatsGrid.getColumnConstraints().clear();
        selectedSeatNumber = null;
        selectedSeatButton = null;

        if (trip.getBus() == null) {
            logger.warn("Для рейса ID [{}] не найден привязанный автобус. Отрисовка мест невозможна.", trip.getTripId());
            showAlert(Alert.AlertType.ERROR, "Ошибка", "К этому рейсу не привязан автобус!");
            return;
        }

        int capacity = trip.getBus().getSeatCapacity();
        List<Integer> occupiedSeats = ticketingService.getOccupiedSeats(trip.getTripId());
        int seatsPerRow = 4;

        for (int c = 0; c < 5; c++) {
            javafx.scene.layout.ColumnConstraints cc = new javafx.scene.layout.ColumnConstraints();
            if (c == 2) {
                cc.setMinWidth(15);
                cc.setPrefWidth(15);
            } else {
                cc.setMinWidth(30);
                cc.setPrefWidth(30);
                cc.setMaxWidth(30);
            }
            cc.setHalignment(javafx.geometry.HPos.CENTER);
            seatsGrid.getColumnConstraints().add(cc);
        }

        for (int i = 1; i <= capacity; i++) {
            Button seatBtn = new Button(String.valueOf(i));
            seatBtn.setMinSize(30, 30);
            seatBtn.setPrefSize(30, 30);
            seatBtn.setMaxSize(30, 30);

            if (occupiedSeats.contains(i)) {
                seatBtn.setStyle("-fx-background-color: #ffcdd2; -fx-text-fill: #c62828; -fx-font-weight: bold;");
                seatBtn.setDisable(true);
            } else {
                seatBtn.setStyle("-fx-background-color: #e0e0e0; -fx-cursor: hand;");
                int currentSeat = i;
                seatBtn.setOnAction(e -> handleSeatSelection(currentSeat, seatBtn));
            }

            int row = (i - 1) / seatsPerRow;
            int col = (i - 1) % seatsPerRow;
            if (col >= 2) col += 1;

            javafx.scene.layout.GridPane.setHgrow(seatBtn, javafx.scene.layout.Priority.NEVER);
            javafx.scene.layout.GridPane.setVgrow(seatBtn, javafx.scene.layout.Priority.NEVER);

            seatsGrid.add(seatBtn, col, row);
        }
    }

    /**
     * Обрабатывает клик по кнопке места. Снимает выделение с ранее выбранного места
     * и подсвечивает новое выбранное место зеленым цветом.
     *
     * @param seatNumber номер выбранного места.
     * @param seatBtn    кнопка, представляющая выбранное место.
     */
    private void handleSeatSelection(int seatNumber, Button seatBtn) {
        if (selectedSeatButton != null) {
            selectedSeatButton.setStyle("-fx-background-color: #e0e0e0; -fx-cursor: hand;");
        }
        selectedSeatNumber = seatNumber;
        selectedSeatButton = seatBtn;
        seatBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        logger.debug("Пассажир выбрал место №{}.", seatNumber);
    }

    /**
     * Обрабатывает нажатие кнопки продажи билета.
     * Выполняет валидацию введенных данных пассажира, проверяет выбор рейса и места,
     * создает или находит пассажира в БД, оформляет билет и отображает чек.
     */
    @FXML
    public void onSellTicketClick() {
        try {
            // 0. Очищаем старые красные рамки перед новой проверкой
            clearHighlights();
            boolean hasError = false;

            Trip selectedTrip = tripsListView.getSelectionModel().getSelectedItem();
            StopItem selectedStop = stopComboBox.getValue();

            String lastName = tfLastName.getText() != null ? tfLastName.getText().trim() : "";
            String firstName = tfFirstName.getText() != null ? tfFirstName.getText().trim() : "";
            String middleName = tfMiddleName.getText() != null ? tfMiddleName.getText().trim() : "";
            String passport = tfPassport.getText() != null ? tfPassport.getText().trim() : "";

            // 1. ПРОВЕРКА НА ПУСТОТУ (с подсветкой красным)
            if (lastName.isEmpty()) { highlightField(tfLastName); hasError = true; }
            if (firstName.isEmpty()) { highlightField(tfFirstName); hasError = true; }
            if (passport.isEmpty()) { highlightField(tfPassport); hasError = true; }
            if (selectedStop == null) { highlightField(stopComboBox); hasError = true; }

            if (selectedTrip == null || selectedSeatNumber == null) {
                hasError = true;
            }

            if (hasError) {
                logger.warn("Попытка продажи билета прервана: не заполнены обязательные поля формы ввода или не выбрано место/рейс.");
                showAlert(Alert.AlertType.WARNING, "Внимание", "Заполните обязательные поля (выделены красным) и выберите место!");
                return;
            }

            // 2. ЖЕСТКАЯ ВАЛИДАЦИЯ ФИО
            if (!lastName.matches("^[А-ЯЁ][а-яё]*(-[А-ЯЁ][а-яё]*)?$") || !firstName.matches("^[А-ЯЁ][а-яё]*$")) {
                logger.warn("Отказ в оформлении билета: некорректный формат Фамилии ({}) или Имени ({}).", lastName, firstName);
                showAlert(Alert.AlertType.ERROR, "Ошибка ввода", "Фамилия и Имя должны быть на кириллице и начинаться с заглавной буквы!");
                highlightField(tfLastName);
                highlightField(tfFirstName);
                return;
            }
            if (!middleName.isEmpty() && !middleName.matches("^[А-ЯЁ][а-яё]*$")) {
                logger.warn("Отказ в оформлении билета: некорректный формат Отчества ({}).", middleName);
                showAlert(Alert.AlertType.ERROR, "Ошибка ввода", "Отчество должно начинаться с заглавной буквы и содержать только кириллицу!");
                highlightField(tfMiddleName);
                return;
            }

            if (passport.length() != 11) { // 10 цифр + 1 пробел от маски
                logger.warn("Отказ в оформлении билета: неверный формат паспорта ({}). Ожидалось 10 цифр.", passport);
                showAlert(Alert.AlertType.ERROR, "Ошибка ввода", "Паспорт должен содержать серию и номер (10 цифр)!");
                highlightField(tfPassport);
                return;
            }

            // Создаем или получаем пассажира
            Passenger passenger = passengerDao.getOrCreate(lastName, firstName, middleName, passport, 0);

            BigDecimal cost = new BigDecimal(costLabel.getText());
            User currentUser = Session.getCurrentUser();
            String currentLogin = currentUser != null ? currentUser.getLogin() : "unknown_user";

            // Оформляем билет
            Ticket newTicket = ticketingService.sellTicket(
                    selectedTrip, passenger, selectedStop.getStop(), currentUser, selectedSeatNumber, cost
            );

            // Формируем чек
            String regNumber = String.format("TKT-%02d%02d-%04d",
                    java.time.LocalDate.now().getMonthValue(),
                    java.time.LocalDate.now().getDayOfMonth(),
                    (int)(Math.random() * 10000));

            String saleDate = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            String depDate = selectedTrip.getDepartureDatetime().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            String fullPassengerName = lastName + " " + firstName + (!middleName.isEmpty() ? " " + middleName : "");

            logger.info("УСПЕШНАЯ ПРОДАЖА: Билет {} оформлен пользователем [{}]. Пассажир: {}, Паспорт: {}, Рейс ID: {}, Место: №{}, Стоимость: {} руб.",
                    regNumber, currentLogin, fullPassengerName, passport, selectedTrip.getTripId(), selectedSeatNumber, cost);

            showReceipt(regNumber, fullPassengerName + " (Паспорт: " + passport + ")",
                    selectedTrip.getRoute().getDeparturePoint() + " - " + selectedTrip.getRoute().getDestinationPoint(),
                    selectedTrip.getBus().getModel() + " (" + selectedTrip.getBus().getLicensePlate() + ")",
                    String.valueOf(selectedSeatNumber), depDate, saleDate, cost.toString());

            // Очищаем форму для следующего клиента
            tfLastName.clear();
            tfFirstName.clear();
            tfMiddleName.clear();
            tfPassport.clear();
            clearHighlights();
            drawBusSeats(selectedTrip);
            logger.debug("Форма продажи очищена и схема мест обновлена для следующего клиента.");

        } catch (Exception e) {
            logger.error("Критический сбой при попытке оформления билета в кассе", e);
            showAlert(Alert.AlertType.ERROR, "Ошибка", e.getMessage());
        }
    }

    /**
     * Открывает модальное окно с маршрутной квитанцией (чеком) для отображения данных о проданном билете.
     *
     * @param regNum регистрационный номер билета.
     * @param pass   ФИО и паспорт пассажира.
     * @param route  маршрут следования.
     * @param bus    информация об автобусе.
     * @param seat   номер места.
     * @param dep    дата и время отправления.
     * @param sale   дата и время продажи.
     * @param cost   стоимость билета.
     */
    private void showReceipt(String regNum, String pass, String route, String bus, String seat, String dep, String sale, String cost) {
        try {
            logger.debug("Открытие окна маршрутной квитанции для билета {}.", regNum);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/pin123/courseprojectcpad/view/ticket-receipt-view.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Маршрутная квитанция");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(page));

            TicketReceiptController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setTicketData(regNum, pass, route, bus, seat, dep, sale, cost);

            dialogStage.showAndWait();
        } catch (IOException e) {
            logger.error("Ошибка ввода-вывода при загрузке fxml-представления маршрутной квитанции ticket-receipt-view.fxml", e);
        }
    }

    /**
     * Отображает модальное всплывающее окно с сообщением для пользователя.
     *
     * @param type    тип предупреждения (INFO, WARNING, ERROR и т.д.).
     * @param title   заголовок окна.
     * @param content текстовое содержание сообщения.
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Снимает красные рамки (подсветку ошибок) со всех полей ввода.
     */
    private void clearHighlights() {
        String defaultStyle = "-fx-border-color: transparent;";
        tfLastName.setStyle(defaultStyle);
        tfFirstName.setStyle(defaultStyle);
        tfPassport.setStyle(defaultStyle);
        stopComboBox.setStyle(defaultStyle);
    }

    /**
     * Окрашивает поле ввода в красный цвет для индикации ошибки валидации.
     *
     * @param field элемент управления (TextField или ComboBox), который нужно подсветить.
     */
    private void highlightField(Control field) {
        field.setStyle("-fx-border-color: red; -fx-border-radius: 4; -fx-border-width: 2;");
    }

    /**
     * Внутренний вспомогательный класс для хранения данных об остановке
     * и рассчитанной стоимости проезда до неё.
     * Используется для отображения в выпадающем списке {@link #stopComboBox}.
     */
    public static class StopItem {
        /** Объект остановки. */
        private final Stop stop;
        /** Рассчитанная стоимость проезда до данной остановки. */
        private final BigDecimal price;

        /**
         * Создает новый элемент списка остановок.
         * @param stop  объект остановки.
         * @param price стоимость проезда.
         */
        public StopItem(Stop stop, BigDecimal price) {
            this.stop = stop;
            this.price = price;
        }

        /** @return объект остановки. */
        public Stop getStop() { return stop; }

        /** @return стоимость проезда. */
        public BigDecimal getPrice() { return price; }

        /**
         * Возвращает строковое представление для отображения в ComboBox.
         * @return строка в формате "Название остановки — Цена руб.".
         */
        @Override
        public String toString() {
            return stop.getName() + " — " + price + " руб.";
        }
    }
}