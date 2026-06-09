package ru.pin123.courseprojectcpad.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.pin123.courseprojectcpad.model.Route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Контроллер модального диалогового окна для создания и редактирования данных маршрута.
 * Отвечает за валидацию анкетных данных маршрута (номер, пункты отправления/назначения, время в пути)
 * и синхронизацию состояния объекта модели с интерфейсом.
 */
public class RouteEditController {

    /** Логгер для фиксации событий редактирования маршрута. */
    private static final Logger logger = LoggerFactory.getLogger(RouteEditController.class);

    /** Поле ввода номера маршрута. */
    @FXML private TextField tfNumber;
    /** Поле ввода пункта отправления. */
    @FXML private TextField tfFrom;
    /** Поле ввода пункта назначения. */
    @FXML private TextField tfTo;
    /** Поле ввода количества часов в пути. */
    @FXML private TextField tfHours;
    /** Поле ввода количества минут в пути. */
    @FXML private TextField tfMinutes;

    /** Ссылка на модальное окно диалога. */
    private Stage dialogStage;
    /** Объект маршрута, данные которого редактируются. */
    private Route route;
    /** Флаг, указывающий на то, что пользователь подтвердил ввод данных (нажал ОК). */
    private boolean isOkClicked = false;

    /**
     * Устанавливает ссылку на модальное окно диалога.
     * @param dialogStage объект Stage для управления окном.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Инициализирует форму данными выбранного маршрута.
     * Конвертирует общую длительность в минутах в часы и минуты для отображения.
     *
     * @param route объект Route для редактирования.
     */
    public void setRoute(Route route) {
        this.route = route;
        if (route.getRouteNumber() != null) tfNumber.setText(route.getRouteNumber());
        if (route.getDeparturePoint() != null) tfFrom.setText(route.getDeparturePoint());
        if (route.getDestinationPoint() != null) tfTo.setText(route.getDestinationPoint());

        if (route.getDurationMinutes() > 0) {
            tfHours.setText(String.valueOf(route.getDurationMinutes() / 60));
            tfMinutes.setText(String.valueOf(route.getDurationMinutes() % 60));
        }

        logger.debug("Форма заполнена данными маршрута: №{} ({} -> {}).",
                route.getRouteNumber(), route.getDeparturePoint(), route.getDestinationPoint());
    }

    /**
     * Возвращает статус подтверждения данных пользователем.
     * @return true, если пользователь нажал кнопку ОК, иначе false.
     */
    public boolean isOkClicked() {
        return isOkClicked;
    }

    /**
     * Обрабатывает нажатие кнопки ОК. Валидирует введенные данные
     * и сохраняет их в объект модели, после чего закрывает диалоговое окно.
     */
    @FXML
    private void handleOk() {
        if (isInputValid()) {
            route.setRouteNumber(tfNumber.getText().trim());
            route.setDeparturePoint(tfFrom.getText().trim());
            route.setDestinationPoint(tfTo.getText().trim());

            int hours = tfHours.getText().isEmpty() ? 0 : Integer.parseInt(tfHours.getText().trim());
            int minutes = tfMinutes.getText().isEmpty() ? 0 : Integer.parseInt(tfMinutes.getText().trim());
            route.setDurationMinutes(hours * 60 + minutes);

            isOkClicked = true;
            dialogStage.close();

            logger.info("Данные маршрута успешно сохранены: №{} ({} -> {}), время в пути: {} мин.",
                    route.getRouteNumber(), route.getDeparturePoint(), route.getDestinationPoint(), route.getDurationMinutes());
        }
    }

    /**
     * Обрабатывает нажатие кнопки Отмена. Закрывает диалоговое окно без сохранения изменений.
     */
    @FXML
    private void handleCancel() {
        logger.debug("Редактирование маршрута отменено пользователем.");
        dialogStage.close();
    }

    /**
     * Проверяет корректность заполнения всех полей формы.
     * @return true, если данные валидны, иначе false.
     */
    private boolean isInputValid() {
        StringBuilder errorMessage = new StringBuilder();

        if (tfNumber.getText() == null || tfNumber.getText().trim().isEmpty()) errorMessage.append("Не указан номер маршрута!\n");
        if (tfFrom.getText() == null || tfFrom.getText().trim().isEmpty()) errorMessage.append("Не указан пункт отправления!\n");
        if (tfTo.getText() == null || tfTo.getText().trim().isEmpty()) errorMessage.append("Не указан пункт назначения!\n");

        boolean isHoursValid = !tfHours.getText().isEmpty() && tfHours.getText().matches("\\d+");
        boolean isMinutesValid = !tfMinutes.getText().isEmpty() && tfMinutes.getText().matches("\\d+");

        if (!isHoursValid && !isMinutesValid) {
            errorMessage.append("Укажите время в пути (часы и/или минуты)!\n");
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            logger.warn("Валидация формы редактирования маршрута не пройдена.");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Ошибка заполнения");
            alert.setHeaderText("Пожалуйста, исправьте следующие ошибки:");
            alert.setContentText(errorMessage.toString());
            alert.showAndWait();
            return false;
        }
    }
}