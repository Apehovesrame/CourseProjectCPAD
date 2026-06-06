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
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import ru.pin123.courseprojectcpad.model.Session;
import ru.pin123.courseprojectcpad.model.User;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainController {

    @FXML private ComboBox<String> langSelector;
    @FXML private TabPane mainTabPane;

    // Элементы из нашей логики сессий
    @FXML private Label lblGreeting;

    @FXML
    public void initialize() {
        // 1. Инициализация языков (ваш код)
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

        // 2. Инициализация сессии и приветствия (наш новый код)
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
            // ИСПОЛЬЗУЕМ НОВЫЙ ПУТЬ К ФАЙЛУ ВЕРСТКИ
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
                    stage.setTitle(bundle.getString("app.title")); // Берет название из main_ru.properties
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- БЛОК НАВИГАЦИИ (пути обновлены под новую структуру) ---

    @FXML
    void onBusesClick(ActionEvent event) {
        loadView(event, "/ru/pin123/courseprojectcpad/view/buses-view.fxml", "Справочник автобусов");
    }

    @FXML
    void onDriversClick(ActionEvent event) {
        loadView(event, "/ru/pin123/courseprojectcpad/view/drivers-view.fxml", "Справочник водителей");
    }

    @FXML
    void onPassengersClick(ActionEvent event) {
        loadView(event, "/ru/pin123/courseprojectcpad/view/passengers-view.fxml", "База пассажиров");
    }

    @FXML
    void onRoutesClick(ActionEvent event) {
        loadView(event, "/ru/pin123/courseprojectcpad/view/routes-view.fxml", "Маршруты");
    }

    @FXML
    void onTripsClick(ActionEvent event) {
        loadView(event, "/ru/pin123/courseprojectcpad/view/trips-view.fxml", "Расписание рейсов");
    }

    @FXML
    void onTicketSellClick(ActionEvent event) {
        loadView(event, "/ru/pin123/courseprojectcpad/view/ticket-sell-view.fxml", "Продажа билета");
    }

    // --- БЛОК ВЫХОДА ---

    @FXML
    void onLogoutClick(ActionEvent event) {
        // Очищаем сессию и возвращаемся на окно логина
        Session.clear();
        loadView(event, "/ru/pin123/courseprojectcpad/view/login-view.fxml", "Авторизация");
    }

    @FXML
    void onExitClick(ActionEvent event) {
        Platform.exit(); // Полное закрытие программы
    }

    // --- УНИВЕРСАЛЬНЫЙ МЕТОД ЗАГРУЗКИ ОКОН ---

    private void loadView(ActionEvent event, String fxmlPath, String defaultTitle) {
        try {
            // ВАЖНО: Добавил загрузку ResourceBundle, чтобы окна открывались на выбранном языке!
            ResourceBundle bundle = ResourceBundle.getBundle("main", Locale.getDefault());
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath), bundle);
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Пытаемся взять переведенный заголовок окна, если его нет - ставим дефолтный
            String title = defaultTitle;
            try {
                title = bundle.getString("app.title");
            } catch (Exception ignored) {}

            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Ошибка при загрузке FXML файла: " + fxmlPath);
            e.printStackTrace();
        }
    }
}