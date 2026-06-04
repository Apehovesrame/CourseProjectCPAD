package ru.pin123.courseprojectcpad.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainController {

    @FXML
    private ComboBox<String> langSelector;
    @FXML
    private TabPane mainTabPane;
    @FXML
    public void initialize() {
        if (langSelector != null) {
            langSelector.setItems(FXCollections.observableArrayList("Русский", "English", "Deutsch"));

            String currentLang = Locale.getDefault().getLanguage();
            if (currentLang.equals("en")) {
                langSelector.setValue("English");
            } else if (currentLang.equals("de")) {
                langSelector.setValue("Deutsch");
            } else {
                langSelector.setValue("Русский");
            }
        }
    }
    @FXML
    void onLanguageChange(ActionEvent event) {
        String selected = langSelector.getValue();
        if ("English".equals(selected)) {
            Locale.setDefault(new Locale("en"));
        } else if ("Deutsch".equals(selected)) {
            Locale.setDefault(new Locale("de"));
        } else {
            Locale.setDefault(new Locale("ru", "RU"));
        }
        reloadUI();
    }

    private void reloadUI() {
        try {
            int activeTab = mainTabPane != null && mainTabPane.getSelectionModel() != null ? mainTabPane.getSelectionModel().getSelectedIndex() : 0;

            ResourceBundle bundle = ResourceBundle.getBundle("main", Locale.getDefault());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/pin123/courseprojectcpad/view/main-view.fxml"), bundle);
            Parent root = loader.load();

            MainController newController = loader.getController();
            if (newController.mainTabPane != null) {
                newController.mainTabPane.getSelectionModel().select(activeTab);
            }

            if (langSelector != null && langSelector.getScene() != null) {
                Scene scene = langSelector.getScene();
                scene.setRoot(root);
                Stage stage = (Stage) scene.getWindow();
                if (stage != null) {
                    stage.setTitle(bundle.getString("app.title"));
                }
            } else if (mainTabPane != null && mainTabPane.getScene() != null) {
                Scene scene = mainTabPane.getScene();
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

    @FXML
    void onBusesClick(ActionEvent event) {
        loadView(event, "/view/buses-view.fxml", "Справочник автобусов");
    }

    @FXML
    void onDriversClick(ActionEvent event) {
        loadView(event, "/view/drivers-view.fxml", "Справочник водителей");
    }

    @FXML
    void onPassengersClick(ActionEvent event) {
        loadView(event, "/view/passengers-view.fxml", "База пассажиров");
    }

    @FXML
    void onRoutesClick(ActionEvent event) {
        loadView(event, "/view/routes-view.fxml", "Маршруты");
    }

    @FXML
    void onTripsClick(ActionEvent event) {
        loadView(event, "/view/trips-view.fxml", "Расписание рейсов");
    }

    @FXML
    void onTicketSellClick(ActionEvent event) {
        loadView(event, "/view/ticket-sell-view.fxml", "Продажа билета");
    }

    @FXML
    void onExitClick(ActionEvent event) {
        Platform.exit();
    }

    private void loadView(ActionEvent event, String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}