package ru.pin123.courseprojectcpad.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.pin123.courseprojectcpad.model.Bus;
import ru.pin123.courseprojectcpad.service.BusService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BusController {

    /**
     * Логгер подсистемы SLF4J/Logback для фиксации состояния автопарка и системных сбоев.
     */
    private static final Logger logger = LoggerFactory.getLogger(BusController.class);

    @FXML private TableView<Bus> busTable;
    @FXML private TableColumn<Bus, Long> idColumn;
    @FXML private TableColumn<Bus, String> modelColumn;
    @FXML private TableColumn<Bus, String> plateColumn;
    @FXML private TableColumn<Bus, Number> capacityColumn;

    /**
     * Элемент графического интерфейса для рендеринга изображения выбранного автобуса.
     */
    @FXML private ImageView busImage;

    /**
     * Наблюдаемый список для реактивной синхронизации UI-компонента TableView с коллекцией моделей.
     */
    private final ObservableList<Bus> busList = FXCollections.observableArrayList();

    /**
     * Сервис бизнес-логики, инкапсулирующий методы манипуляции сущностями автобусов.
     */
    private final BusService busService = new BusService();

    /**
     * Автоматический метод инициализации JavaFX. Настраивает фабрики отображения ячеек (Cell Value Factories),
     * привязывает слушатель изменения фокуса строки таблицы и загружает первичный набор данных.
     */
    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getBusId()));
        modelColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getModel()));
        plateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLicensePlate()));
        capacityColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getSeatCapacity()));

        // Добавляем слушатель: при клике на строку таблицы показываем фото автобуса
        busTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showBusDetails(newValue)
        );

        busTable.setItems(busList);
        loadData();
    }

    /**
     * Извлекает массив байт (byte[]) из модели автобуса и преобразует его
     * во входной бинарный поток для последующей отрисовки компонентом ImageView.
     * * @param bus Экземпляр модели автобуса, выбранный пользователем в таблице.
     */
    private void showBusDetails(Bus bus) {
        if (bus != null && bus.getBusImage() != null && bus.getBusImage().length > 0) {
            logger.debug("Загрузка бинарного контента изображения для автобуса ID [{}]. Size: {} bytes", bus.getBusId(), bus.getBusImage().length);
            ByteArrayInputStream bis = new ByteArrayInputStream(bus.getBusImage());
            busImage.setImage(new Image(bis));
        } else {
            busImage.setImage(null);
        }
    }

    /**
     * Запрашивает актуальный список транспортных средств у сервиса и обновляет контейнер данных таблицы.
     */
    private void loadData() {
        busList.clear();
        try {
            busList.addAll(busService.getAllBuses());
        } catch (Exception e) {
            logger.error("Не удалось выполнить чтение данных из репозитория автопарка.", e);
            showAlert(Alert.AlertType.ERROR, "Ошибка БД", "Не удалось загрузить список автобусов: " + e.getMessage());
        }
    }

    /**
     * Обработчик события создания записи. Конструирует пустую модель, вызывает диалог
     * заполнения параметров и сохраняет валидную сущность в базу данных.
     */
    @FXML
    private void onAddBus() {
        Bus newBus = new Bus();
        boolean okClicked = showBusEditDialog(newBus);
        if (okClicked) {
            busService.saveBus(newBus);
            logger.info("В реестр автопарка успешно добавлен новый автобус: {} (Гос. номер: {}).",
                    newBus.getModel(), newBus.getLicensePlate());
            loadData();
        }
    }

    /**
     * Обработчик события модификации записи. Передает ссылку на выбранную строку в модальное окно,
     * после чего фиксирует измененные атрибуты в СУБД.
     */
    @FXML
    private void onEditBus() {
        Bus selectedBus = busTable.getSelectionModel().getSelectedItem();
        if (selectedBus != null) {
            boolean okClicked = showBusEditDialog(selectedBus);
            if (okClicked) {
                busService.saveBus(selectedBus);
                logger.info("Обновлена конфигурация транспортного средства с ID [{}]: {} ({}).",
                        selectedBus.getBusId(), selectedBus.getModel(), selectedBus.getLicensePlate());
                loadData();
                showBusDetails(selectedBus);
            }
        } else {
            logger.warn("Действие отменено: пользователь попытался вызвать редактирование без выбора автобуса.");
            showAlert(Alert.AlertType.WARNING, "Внимание", "Выберите автобус в таблице для редактирования!");
        }
    }

    /**
     * Обработчик события удаления транспортного средства. Стирает запись по первичному ключу ID.
     */
    @FXML
    private void onDeleteBus() {
        Bus selectedBus = busTable.getSelectionModel().getSelectedItem();
        if (selectedBus != null) {
            try {
                busService.deleteBus(selectedBus.getBusId());
                logger.warn("Из реестра автопарка безвозвратно удален автобус с ID [{}], Гос. номер: {}.",
                        selectedBus.getBusId(), selectedBus.getLicensePlate());
                loadData();
                showBusDetails(null);
            } catch (Exception e) {
                logger.error("Ошибка каскадного удаления: автобус ID [{}] задействован в активных рейсах расписания.", selectedBus.getBusId(), e);
                showAlert(Alert.AlertType.ERROR, "Ошибка удаления", "Невозможно удалить автобус. Вероятно, он привязан к существующему рейсу.");
            }
        } else {
            logger.warn("Действие отменено: пользователь попытался удалить автобус без выбора строки.");
            showAlert(Alert.AlertType.WARNING, "Внимание", "Выберите автобус для удаления!");
        }
    }

    /**
     * Конструирует и открывает изолированное модальное диалоговое окно редактирования/создания автобуса.
     * Загружает связанный слой ресурсов локализации (ResourceBundle).
     * * @param bus Модифицируемый объект автобуса.
     * @return true, если сессия редактирования завершилась подтверждением сохранения (кнопка ОК), иначе false.
     */
    private boolean showBusEditDialog(Bus bus) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("main", Locale.getDefault());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/pin123/courseprojectcpad/view/bus-edit-view.fxml"), bundle);
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(bundle.getString("app.title"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(page));

            BusEditController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setBus(bus);

            dialogStage.showAndWait();
            return controller.isOkClicked();
        } catch (IOException e) {
            logger.error("Критическая ошибка I/O при попытке десериализации fxml-представления bus-edit-view.fxml", e);
            return false;
        }
    }

    /**
     * Обобщенный вспомогательный метод вывода всплывающих окон графических уведомлений.
     * * @param type    Тип окна (ERROR, WARNING, INFORMATION).
     * @param title   Текст заголовка окна.
     * @param content Основное текстовое сообщение.
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}