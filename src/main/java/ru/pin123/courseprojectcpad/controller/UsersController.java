package ru.pin123.courseprojectcpad.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.pin123.courseprojectcpad.dao.UserDaoImpl;
import ru.pin123.courseprojectcpad.model.User;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UsersController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(UsersController.class);

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> colLogin;
    @FXML private TableColumn<User, String> colLastName;
    @FXML private TableColumn<User, String> colFirstName;
    @FXML private TableColumn<User, String> colMiddleName;
    @FXML private TableColumn<User, String> colRole;

    // Внедряем бандл ресурсов
    @FXML private ResourceBundle resources;

    private final UserDaoImpl userDao = new UserDaoImpl();
    private final ObservableList<User> userList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Сохраняем ресурсы для алертов
        this.resources = resources;

        logger.info("Инициализация контроллера пользователей. Настройка колонок таблицы.");

        colLogin.setCellValueFactory(new PropertyValueFactory<>("login"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colMiddleName.setCellValueFactory(new PropertyValueFactory<>("middleName"));

        colRole.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRole().getRoleName()));

        userTable.setItems(userList);
        loadData();
    }

    private void loadData() {
        try {
            userList.clear();
            userList.addAll(userDao.findAll());
            logger.info("Успешно загружено {} записей пользователей из базы данных.", userList.size());
        } catch (Exception e) {
            logger.error("Критическая ошибка при попытке загрузки списка пользователей из слоя DAO", e);
            // ИСПРАВЛЕНО: Локализация ошибки
            showAlert(Alert.AlertType.ERROR, resources.getString("alert.error.title"), resources.getString("users.error.load") + ": " + e.getMessage());
        }
    }

    @FXML
    private void handleAdd() {
        User tempUser = new User();
        boolean okClicked = showUserEditDialog(tempUser);
        if (okClicked) {
            logger.info("Новый пользователь успешно создан через диалоговое окно. Обновление таблицы.");
            loadData();
        } else {
            logger.debug("Создание нового пользователя было отменено.");
        }
    }

    @FXML
    private void handleEdit() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            boolean okClicked = showUserEditDialog(selectedUser);
            if (okClicked) {
                logger.info("Данные пользователя ID [{}] успешно обновлены через диалоговое окно.", selectedUser.getUserId());
                loadData();
            } else {
                logger.debug("Редактирование пользователя ID [{}] было отменено.", selectedUser.getUserId());
            }
        } else {
            logger.warn("Попытка редактирования: действие отменено, сотрудник не выбран в таблице.");
            // ИСПРАВЛЕНО: Локализация предупреждения
            showAlert(Alert.AlertType.WARNING, resources.getString("alert.warning.title"), resources.getString("alert.select_item"));
        }
    }

    @FXML
    private void handleDelete() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            try {
                userDao.delete(selectedUser.getUserId());
                logger.info("Из базы данных удален пользователь: Логин='{}', ID=[{}].",
                        selectedUser.getLogin(), selectedUser.getUserId());
                loadData();
            } catch (RuntimeException e) {
                logger.error("Не удалось удалить пользователя ID [{}] из-за ограничений в БД.", selectedUser.getUserId(), e);
                // ИСПРАВЛЕНО: Локализация ошибки удаления
                showAlert(Alert.AlertType.ERROR, resources.getString("alert.error.title"), resources.getString("users.error.delete") + ": " + e.getMessage());
            }
        } else {
            logger.warn("Попытка удаления: действие отменено, сотрудник не выбран в таблице.");
            showAlert(Alert.AlertType.WARNING, resources.getString("alert.warning.title"), resources.getString("alert.select_item"));
        }
    }

    private boolean showUserEditDialog(User user) {
        try {
            logger.debug("Загрузка FXML-формы user-edit-view.fxml для {} пользователя.",
                    user.getUserId() == null ? "создания" : "редактирования");

            // ИСПРАВЛЕНО: Передаем бандл resources
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/pin123/courseprojectcpad/view/user-edit-view.fxml"), resources);
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();

            // ИСПРАВЛЕНО: Динамический заголовок из ресурсов
            String titleKey = user.getUserId() == null ? "users.edit.title_new" : "users.edit.title_edit";
            dialogStage.setTitle(resources.getString(titleKey));

            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(userTable.getScene().getWindow());
            dialogStage.setScene(new Scene(page));

            UserEditController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setUser(user);

            dialogStage.showAndWait();
            return controller.isOkClicked();
        } catch (IOException e) {
            logger.error("Критическая ошибка ввода-вывода интерфейса при загрузке fxml-формы user-edit-view.fxml", e);
            showAlert(Alert.AlertType.ERROR, resources.getString("alert.error.title"), resources.getString("users.error.load_dialog") + ": " + e.getMessage());
            return false;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}