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

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> colLogin;
    @FXML private TableColumn<User, String> colLastName;
    @FXML private TableColumn<User, String> colFirstName;
    @FXML private TableColumn<User, String> colMiddleName;
    @FXML private TableColumn<User, String> colRole;

    private final UserDaoImpl userDao = new UserDaoImpl();
    private final ObservableList<User> userList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Привязываем колонки к полям модели User
        colLogin.setCellValueFactory(new PropertyValueFactory<>("login"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colMiddleName.setCellValueFactory(new PropertyValueFactory<>("middleName"));

        // Для роли берем название роли (roleName) из вложенного объекта Role
        colRole.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRole().getRoleName()));

        userTable.setItems(userList);
        loadData();
    }

    private void loadData() {
        userList.clear();
        try {
            // Предполагается, что в UserDaoImpl есть метод findAll()
            userList.addAll(userDao.findAll());
        } catch (Exception e) {
            showAlert("Ошибка загрузки списка сотрудников: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdd() {
        User tempUser = new User();
        boolean okClicked = showUserEditDialog(tempUser);
        if (okClicked) {
            loadData(); // Перезагружаем таблицу после успешного сохранения в диалоге
        }
    }

    @FXML
    private void handleEdit() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            boolean okClicked = showUserEditDialog(selectedUser);
            if (okClicked) {
                loadData();
            }
        } else {
            showAlert("Выберите сотрудника в таблице для редактирования.");
        }
    }

    @FXML
    private void handleDelete() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            try {
                userDao.delete(selectedUser.getUserId());
                loadData();
            } catch (RuntimeException e) {
                showAlert("Невозможно удалить сотрудника: " + e.getMessage());
            }
        } else {
            showAlert("Выберите сотрудника в таблице для удаления.");
        }
    }

    private boolean showUserEditDialog(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/pin123/courseprojectcpad/view/user-edit-view.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(user.getUserId() == null ? "Новый сотрудник" : "Редактирование сотрудника");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(userTable.getScene().getWindow());
            dialogStage.setScene(new Scene(page));

            // Передаем пользователя в контроллер диалога
            UserEditController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setUser(user);

            dialogStage.showAndWait();
            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Ошибка загрузки окна редактирования: " + e.getMessage());
            return false;
        }
    }

    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Внимание");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}