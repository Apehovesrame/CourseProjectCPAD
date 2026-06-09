package ru.pin123.courseprojectcpad.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.pin123.courseprojectcpad.model.Route;

import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Контроллер модального диалогового окна для создания и редактирования данных маршрута.
 * Отвечает за валидацию анкетных данных маршрута (номер, пункты отправления/назначения, время в пути)
 * и синхронизацию состояния объекта модели с интерфейсом.
 */
public class RouteEditController {

    private static final Logger logger = LoggerFactory.getLogger(RouteEditController.class);

    @FXML private TextField tfNumber;
    @FXML private TextField tfFrom;
    @FXML private TextField tfTo;
    @FXML private TextField tfHours;
    @FXML private TextField tfMinutes;

    // Внедряем ресурсы локализации
    @FXML private ResourceBundle resources;

    private Stage dialogStage;
    private Route route;
    private boolean isOkClicked = false;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

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

    public boolean isOkClicked() {
        return isOkClicked;
    }

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

    @FXML
    private void handleCancel() {
        logger.debug("Редактирование маршрута отменено пользователем.");
        dialogStage.close();
    }

    private boolean isInputValid() {
        StringBuilder errorMessage = new StringBuilder();

        if (tfNumber.getText() == null || tfNumber.getText().trim().isEmpty())
            errorMessage.append(resources.getString("routes.edit.error.no_number")).append("\n");

        if (tfFrom.getText() == null || tfFrom.getText().trim().isEmpty())
            errorMessage.append(resources.getString("routes.edit.error.no_from")).append("\n");

        if (tfTo.getText() == null || tfTo.getText().trim().isEmpty())
            errorMessage.append(resources.getString("routes.edit.error.no_to")).append("\n");

        boolean isHoursValid = !tfHours.getText().isEmpty() && tfHours.getText().matches("\\d+");
        boolean isMinutesValid = !tfMinutes.getText().isEmpty() && tfMinutes.getText().matches("\\d+");

        if (!isHoursValid && !isMinutesValid) {
            errorMessage.append(resources.getString("routes.edit.error.no_duration")).append("\n");
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            logger.warn("Валидация формы редактирования маршрута не пройдена.");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            // ИСПРАВЛЕНО: Локализован заголовок и текст алерта
            alert.setTitle(resources.getString("alert.error.fill_title"));
            alert.setHeaderText(resources.getString("alert.error.fix_errors"));
            alert.setContentText(errorMessage.toString());
            alert.showAndWait();
            return false;
        }
    }
}