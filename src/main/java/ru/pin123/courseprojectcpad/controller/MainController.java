package ru.pin123.courseprojectcpad.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import ru.pin123.courseprojectcpad.model.Session;
import ru.pin123.courseprojectcpad.model.User;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Главный контроллер приложения, отвечающий за навигацию, управление локалью (языком)
 * и отображение базовой информации о текущем пользователе.
 * Реализует SPA-подход, динамически подгружая различные FXML-представления в центральную область экрана.
 */
public class MainController {

    /** Логгер для фиксации событий навигации, смены языка и управления сессией. */
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    /** Выпадающий список для выбора языка интерфейса. */
    @FXML private ComboBox<String> langSelector;

    /** Метка для отображения приветствия текущего авторизованного пользователя. */
    @FXML private Label lblGreeting;

    /** Центральная область экрана (SPA-контейнер), куда загружаются дочерние представления. */
    @FXML private StackPane contentArea;

    // Кнопки бокового меню навигации
    @FXML private Button btnRoutes, btnBuses, btnDrivers, btnPassengers, btnTrips, btnTicketSell, btnReports, btnUsers;

    /**
     * Инициализирует главный контроллер после загрузки FXML-файла.
     * Настраивает доступные языки, определяет текущую локаль системы и отображает приветствие.
     */
    @FXML
    public void initialize() {
        if (langSelector != null) {
            langSelector.setItems(FXCollections.observableArrayList("Русский", "English"));

            String currentLang = Locale.getDefault().getLanguage();
            if ("en".equals(currentLang)) {
                Locale.setDefault(new Locale("en"));
                langSelector.setValue("English");
            } else {
                Locale.setDefault(new Locale("ru", "RU"));
                langSelector.setValue("Русский");
            }
            logger.info("Инициализация главного окна. Установлена локаль: {}", Locale.getDefault());
        }

        User currentUser = Session.getCurrentUser();
        if (currentUser != null && lblGreeting != null) {
            lblGreeting.setText("Добро пожаловать, " + currentUser.getFirstName() + " " + currentUser.getLastName() + "!");
            logger.debug("Отображение приветствия для пользователя: {} {}", currentUser.getFirstName(), currentUser.getLastName());
        }
    }

    /**
     * Обрабатывает событие изменения языка в выпадающем списке.
     * Устанавливает новую локаль по умолчанию и перезагружает интерфейс.
     *
     * @param event событие действия от ComboBox.
     */
    @FXML
    void onLanguageChange(ActionEvent event) {
        String selected = langSelector.getValue();
        if ("English".equals(selected)) {
            Locale.setDefault(new Locale("en"));
        } else {
            Locale.setDefault(new Locale("ru", "RU"));
        }
        logger.info("Язык интерфейса изменен пользователем на: {}", selected);
        reloadUI();
    }

    /**
     * Полностью перезагружает главное окно приложения для применения новых настроек локали.
     */
    private void reloadUI() {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("main", Locale.getDefault());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/pin123/courseprojectcpad/view/main-view.fxml"), bundle);
            Parent root = loader.load();

            if (langSelector != null && langSelector.getScene() != null) {
                Scene scene = langSelector.getScene();
                scene.setRoot(root);
                Stage stage = (Stage) scene.getWindow();
                if (stage != null) {
                    stage.setTitle(bundle.getString("app.title"));
                }
            }
            logger.debug("Интерфейс успешно перезагружен после смены языка.");
        } catch (IOException e) {
            logger.error("Критическая ошибка при перезагрузке главного интерфейса (reloadUI).", e);
        }
    }

    // ================= БЛОК НАВИГАЦИИ =================

    /**
     * Обрабатывает клик по кнопке "Маршруты".
     * @param event событие клика.
     */
    @FXML
    void onRoutesClick(ActionEvent event) {
        logger.info("Переход в раздел: Маршруты");
        setActiveButton(btnRoutes);
        loadView("/ru/pin123/courseprojectcpad/view/routes-view.fxml");
    }

    /**
     * Обрабатывает клик по кнопке "Автобусы".
     * @param event событие клика.
     */
    @FXML
    void onBusesClick(ActionEvent event) {
        logger.info("Переход в раздел: Автобусы");
        setActiveButton(btnBuses);
        loadView("/ru/pin123/courseprojectcpad/view/buses-table.fxml");
    }

    /**
     * Обрабатывает клик по кнопке "Водители".
     * @param event событие клика.
     */
    @FXML
    void onDriversClick(ActionEvent event) {
        logger.info("Переход в раздел: Водители");
        setActiveButton(btnDrivers);
        loadView("/ru/pin123/courseprojectcpad/view/drivers-view.fxml");
    }

    /**
     * Обрабатывает клик по кнопке "Пассажиры".
     * @param event событие клика.
     */
    @FXML
    void onPassengersClick(ActionEvent event) {
        logger.info("Переход в раздел: Пассажиры");
        setActiveButton(btnPassengers);
        loadView("/ru/pin123/courseprojectcpad/view/passengers-view.fxml");
    }

    /**
     * Обрабатывает клик по кнопке "Рейсы".
     * @param event событие клика.
     */
    @FXML
    void onTripsClick(ActionEvent event) {
        logger.info("Переход в раздел: Рейсы");
        setActiveButton(btnTrips);
        loadView("/ru/pin123/courseprojectcpad/view/trips-view.fxml");
    }

    /**
     * Обрабатывает клик по кнопке "Продажа билетов".
     * @param event событие клика.
     */
    @FXML
    void onTicketSellClick(ActionEvent event) {
        logger.info("Переход в раздел: Продажа билетов");
        setActiveButton(btnTicketSell);
        loadView("/ru/pin123/courseprojectcpad/view/ticket-sell-view.fxml");
    }

    /**
     * Обрабатывает клик по кнопке "Отчеты".
     * @param event событие клика.
     */
    @FXML
    void onReportsClick(ActionEvent event) {
        logger.info("Переход в раздел: Отчеты");
        setActiveButton(btnReports);
        loadView("/ru/pin123/courseprojectcpad/view/reports-view.fxml");
    }

    /**
     * Обрабатывает клик по кнопке "Пользователи".
     * @param event событие клика.
     */
    @FXML
    void onUsersClick(ActionEvent event) {
        logger.info("Переход в раздел: Пользователи");
        setActiveButton(btnUsers);
        loadView("/ru/pin123/courseprojectcpad/view/users-view.fxml");
    }

    // ================= БЛОК ВЫХОДА =================

    /**
     * Выполняет выход пользователя из системы, очищает сессию
     * и перенаправляет на экран авторизации (login-view).
     *
     * @param event событие клика по кнопке выхода.
     */
    @FXML
    void onLogoutClick(ActionEvent event) {
        User currentUser = Session.getCurrentUser();
        String userName = (currentUser != null) ? currentUser.getFirstName() + " " + currentUser.getLastName() : "Неизвестный пользователь";

        Session.clear();
        logger.info("Пользователь {} успешно вышел из системы. Переход на экран авторизации.", userName);

        try {
            ResourceBundle bundle = ResourceBundle.getBundle("main", Locale.getDefault());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/pin123/courseprojectcpad/view/login-view.fxml"), bundle);
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Авторизация");
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            logger.error("Критическая ошибка при загрузке экрана авторизации после выхода из системы.", e);
        }
    }

    // ================= УТИЛИТЫ =================

    /**
     * Универсальный метод загрузки FXML-представлений в центральную область (SPA-подход).
     * Очищает текущее содержимое {@link #contentArea} и добавляет новое представление.
     *
     * @param fxmlPath путь к FXML-файлу представления.
     */
    private void loadView(String fxmlPath) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("main", Locale.getDefault());
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath), bundle);
            Node view = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            logger.debug("Представление успешно загружено в contentArea: {}", fxmlPath);

        } catch (IOException e) {
            logger.error("Ошибка ввода-вывода при загрузке FXML файла: {}", fxmlPath, e);
        }
    }

    /**
     * Управляет визуальным выделением (подсветкой) активной кнопки в боковом меню.
     * Снимает класс "active-menu-btn" со всех кнопок и добавляет его к указанной.
     *
     * @param activeBtn кнопка, которую необходимо выделить как активную.
     */
    private void setActiveButton(Button activeBtn) {
        Button[] allButtons = {btnRoutes, btnBuses, btnDrivers, btnPassengers, btnTrips, btnTicketSell, btnReports, btnUsers};
        for (Button btn : allButtons) {
            if (btn != null) btn.getStyleClass().remove("active-menu-btn");
        }
        if (activeBtn != null) {
            activeBtn.getStyleClass().add("active-menu-btn");
            logger.debug("Активной кнопкой меню установлена: {}", activeBtn.getId());
        }
    }
}