package ru.pin123.courseprojectcpad.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import ru.pin123.courseprojectcpad.model.Session;
import ru.pin123.courseprojectcpad.model.User;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainController {

    @FXML private ComboBox<String> langSelector;
    @FXML private Label lblGreeting;

    // Ссылка на центральную область экрана, куда будут грузиться меню
    @FXML private StackPane contentArea;

    @FXML
    public void initialize() {
        if (langSelector != null) {
            langSelector.setItems(FXCollections.observableArrayList("Русский", "English"));

            String currentLang = Locale.getDefault().getLanguage();
            if (currentLang.equals("en")) {
                Locale.setDefault(new Locale("en"));
                langSelector.setValue("English");
            } else {
                Locale.setDefault(new Locale("ru", "RU"));
                langSelector.setValue("Русский");
            }
        }

        User currentUser = Session.getCurrentUser();
        if (currentUser != null && lblGreeting != null) {
            lblGreeting.setText("Добро пожаловать, " + currentUser.getFirstName() + " " + currentUser.getLastName() + "!");
        }
    }

    @FXML
    void onLanguageChange(ActionEvent event) {
        String selected = langSelector.getValue();
        if ("English".equals(selected)) {
            Locale.setDefault(new Locale("en"));
        } else {
            Locale.setDefault(new Locale("ru", "RU"));
        }
        reloadUI();
    }

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- БЛОК НАВИГАЦИИ ---

    @FXML
    void onBusesClick(ActionEvent event) {
        loadView("/ru/pin123/courseprojectcpad/view/buses-table.fxml");
    }

    @FXML
    void onDriversClick(ActionEvent event) {
        loadView("/ru/pin123/courseprojectcpad/view/drivers-view.fxml");
    }

    @FXML
    void onPassengersClick(ActionEvent event) {
        loadView("/ru/pin123/courseprojectcpad/view/passengers-view.fxml");
    }

    @FXML
    void onRoutesClick(ActionEvent event) {
        loadView("/ru/pin123/courseprojectcpad/view/routes-view.fxml");
    }

    @FXML
    void onTripsClick(ActionEvent event) {
        loadView("/ru/pin123/courseprojectcpad/view/trips-view.fxml");
    }

    @FXML
    void onTicketSellClick(ActionEvent event) {
        loadView("/ru/pin123/courseprojectcpad/view/ticket-sell-view.fxml");
    }

    @FXML
    void onReportsClick(ActionEvent event) {
        loadView("/ru/pin123/courseprojectcpad/view/reports-view.fxml");
    }

    @FXML
    void onUsersClick(ActionEvent event) {
        loadView("/ru/pin123/courseprojectcpad/view/users-view.fxml");
    }

    // --- БЛОК ВЫХОДА ---

    @FXML
    void onLogoutClick(ActionEvent event) {
        Session.clear();
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("main", Locale.getDefault());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/pin123/courseprojectcpad/view/login-view.fxml"), bundle);
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Авторизация");
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- УНИВЕРСАЛЬНЫЙ МЕТОД ЗАГРУЗКИ ОКОН (SPA-ПОДХОД) ---

    private void loadView(String fxmlPath) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("main", Locale.getDefault());
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath), bundle);
            Node view = loader.load();

            // Очищаем центральную область и вставляем новый элемент
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

        } catch (IOException e) {
            System.err.println("Ошибка при загрузке FXML файла: " + fxmlPath);
            e.printStackTrace();
        }
    }
}