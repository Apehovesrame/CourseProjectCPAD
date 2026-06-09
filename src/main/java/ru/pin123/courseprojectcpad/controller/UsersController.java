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

/**
 * Контроллер для управления списком пользователей (сотрудников) системы в JavaFX приложении.
 * Отвечает за отображение таблицы пользователей с их ролями,
 * а также за выполнение операций CRUD (создание, чтение, обновление, удаление)
 * через модальные диалоговые окна. Взаимодействует с базой данных через {@link UserDaoImpl}.
 */
public class UsersController implements Initializable {

    /** Логгер для фиксации событий управления пользователями. */
    private static final Logger logger = LoggerFactory.getLogger(UsersController.class);

    /** Таблица для отображения списка пользователей. */
    @FXML private TableView<User> userTable;
    /** Колонка с логином пользователя. */
    @FXML private TableColumn<User, String> colLogin;
    /** Колонка с фамилией пользователя. */
    @FXML private TableColumn<User, String> colLastName;
    /** Колонка с именем пользователя. */
    @FXML private TableColumn<User, String> colFirstName;
    /** Колонка с отчеством пользователя. */
    @FXML private TableColumn<User, String> colMiddleName;
    /** Колонка с названием роли пользователя. */
    @FXML private TableColumn<User, String> colRole;

    /** DAO-объект для работы с базой данных пользователей. */
    private final UserDaoImpl userDao = new UserDaoImpl();
    /** Наблюдаемый список пользователей для привязки к таблице. */
    private final ObservableList<User> userList = FXCollections.observableArrayList();

    /**
     * Инициализирует контроллер после загрузки FXML-файла.
     * Настраивает привязку данных для колонок таблицы (включая извлечение названия роли из вложенного объекта)
     * и загружает данные из БД.
     *
     * @param location  URL-адрес для разрешения относительных путей, или null.
     * @param resources Ресурсы для локализации, или null.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Инициализация контроллера пользователей. Настройка колонок таблицы.");

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

    /**
     * Загружает список всех пользователей из базы данных и обновляет таблицу.
     * В случае ошибки выводит сообщение в лог и показывает предупреждение пользователю.
     */
    private void loadData() {
        try {
            userList.clear();
            userList.addAll(userDao.findAll());
            logger.info("Успешно загружено {} записей пользователей из базы данных.", userList.size());
        } catch (Exception e) {
            logger.error("Критическая ошибка при попытке загрузки списка пользователей из слоя DAO", e);
            showAlert("Ошибка загрузки списка сотрудников: " + e.getMessage());
        }
    }

    /**
     * Обработчик нажатия кнопки "Добавить".
     * Открывает диалоговое окно для ввода данных нового пользователя.
     * Если пользователь подтвердил ввод, обновляет таблицу.
     */
    @FXML
    private void handleAdd() {
        User tempUser = new User();
        boolean okClicked = showUserEditDialog(tempUser);
        if (okClicked) {
            logger.info("Новый пользователь успешно создан через диалоговое окно. Обновление таблицы.");
            loadData(); // Перезагружаем таблицу после успешного сохранения в диалоге
        } else {
            logger.debug("Создание нового пользователя было отменено.");
        }
    }

    /**
     * Обработчик нажатия кнопки "Редактировать".
     * Открывает диалоговое окно для изменения данных выбранного пользователя.
     * Если пользователь не выбран, показывает предупреждение.
     */
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
            showAlert("Выберите сотрудника в таблице для редактирования.");
        }
    }

    /**
     * Обработчик нажатия кнопки "Удалить".
     * Удаляет выбранного пользователя из базы данных.
     * Если пользователь не выбран, показывает предупреждение.
     * Обрабатывает исключения, связанные с ограничениями внешних ключей в БД.
     */
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
                showAlert("Невозможно удалить сотрудника: " + e.getMessage());
            }
        } else {
            logger.warn("Попытка удаления: действие отменено, сотрудник не выбран в таблице.");
            showAlert("Выберите сотрудника в таблице для удаления.");
        }
    }

    /**
     * Открывает модальное диалоговое окно для создания или редактирования данных пользователя.
     *
     * @param user объект пользователя с данными для отображения в диалоге (пустой для создания нового).
     * @return true, если пользователь нажал OK и сохранил изменения, false в противном случае.
     */
    private boolean showUserEditDialog(User user) {
        try {
            logger.debug("Загрузка FXML-формы user-edit-view.fxml для {} пользователя.",
                    user.getUserId() == null ? "создания" : "редактирования");

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
            logger.error("Критическая ошибка ввода-вывода интерфейса при загрузке fxml-формы user-edit-view.fxml", e);
            showAlert("Ошибка загрузки окна редактирования: " + e.getMessage());
            return false;
        }
    }

    /**
     * Отображает модальное всплывающее окно с предупреждением для пользователя.
     *
     * @param content текстовое содержание предупреждения.
     */
    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Внимание");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}