package ru.pin123.courseprojectcpad;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Optional;

public class LoginDialog {

    public Optional<LoginResult> showAndWait() {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Авторизация");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);

        Label userLabel = new Label("Логин:");
        TextField userField = new TextField();
        userField.setPromptText("например, appuser");
        userField.setId("loginField");

        Label passLabel = new Label("Пароль:");
        PasswordField passField = new PasswordField();
        passField.setId("passwordField");

        grid.add(userLabel, 0, 0);
        grid.add(userField, 1, 0);
        grid.add(passLabel, 0, 1);
        grid.add(passField, 1, 1);

        Button btnOk = new Button("Войти");
        Button btnCancel = new Button("Отмена");

        HBox buttonBar = new HBox(10, btnOk, btnCancel);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(15, grid, buttonBar);
        root.setPadding(new Insets(10));

        final String[] username = {null};
        final String[] password = {null};
        final boolean[] okClicked = {false};

        btnOk.setOnAction(e -> {
            String u = userField.getText().trim();
            String p = passField.getText();
            if (u.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Логин не может быть пустым");
                return;
            }
            username[0] = u;
            password[0] = p;
            okClicked[0] = true;
            dialogStage.close();
        });

        btnCancel.setOnAction(e -> {
            okClicked[0] = false;
            dialogStage.close();
        });

        dialogStage.setScene(new Scene(root, 300, 150));
        dialogStage.showAndWait();

        if (okClicked[0]) {
            return Optional.of(new LoginResult(username[0], password[0]));
        }
        return Optional.empty();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Вспомогательный класс для хранения логина и пароля
    public static class LoginResult {
        private final String username;
        private final String password;

        public LoginResult(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() { return username; }
        public String getPassword() { return password; }
    }
}