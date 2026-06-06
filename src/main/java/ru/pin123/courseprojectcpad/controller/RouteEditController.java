package ru.pin123.courseprojectcpad.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.pin123.courseprojectcpad.model.Route;

public class RouteEditController {

    @FXML private TextField tfNumber;
    @FXML private TextField tfFrom;
    @FXML private TextField tfTo;
    @FXML private TextField tfDuration;

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
        if (route.getDurationMinutes() > 0) tfDuration.setText(String.valueOf(route.getDurationMinutes()));
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
            route.setDurationMinutes(Integer.parseInt(tfDuration.getText().trim()));

            isOkClicked = true;
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean isInputValid() {
        StringBuilder errorMessage = new StringBuilder();

        if (tfNumber.getText() == null || tfNumber.getText().trim().isEmpty()) errorMessage.append("Не указан номер маршрута!\n");
        if (tfFrom.getText() == null || tfFrom.getText().trim().isEmpty()) errorMessage.append("Не указан пункт отправления!\n");
        if (tfTo.getText() == null || tfTo.getText().trim().isEmpty()) errorMessage.append("Не указан пункт назначения!\n");

        if (tfDuration.getText() == null || tfDuration.getText().trim().isEmpty()) {
            errorMessage.append("Не указано время в пути!\n");
        } else {
            try {
                int duration = Integer.parseInt(tfDuration.getText().trim());
                if (duration <= 0) errorMessage.append("Время в пути должно быть больше нуля!\n");
            } catch (NumberFormatException e) {
                errorMessage.append("Время в пути должно быть целым числом!\n");
            }
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
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