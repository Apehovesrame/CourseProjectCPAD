package ru.pin123.courseprojectcpad.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ru.pin123.courseprojectcpad.dao.UserDaoImpl;
import ru.pin123.courseprojectcpad.model.Role;
import ru.pin123.courseprojectcpad.model.User;

import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Контроллер модального диалогового окна для создания и редактирования учетных записей пользователей.
 * Отвечает за выбор роли, ввод логина, пароля и анкетных данных (ФИО),
 * строгую валидацию введенной информации и сохранение пользователя в базу данных через {@link UserDaoImpl}.
 */
public class UserEditController implements Initializable {

    /** Логгер для фиксации событий создания и редактирования пользователей. */
    private static final Logger logger = LoggerFactory.getLogger(UserEditController.class);

    /** Выпадающий список для выбора роли пользователя. */
    @FXML private ComboBox<Role> comboRole;
    /** Поле ввода логина. */
    @FXML private TextField tfLogin;
    /** Поле ввода пароля. */
    @FXML private PasswordField pfPassword;
    /** Поле ввода фамилии. */
    @FXML private TextField tfLastName;
    /** Поле ввода имени. */
    @FXML private TextField tfFirstName;

    /** Ссылка на модальное окно диалога. */
    private Stage dialogStage;
    /** Объект пользователя, данные которого редактируются (или пустой для создания нового). */
    private User user;
    /** Флаг, указывающий на то, что пользователь подтвердил ввод данных (нажал ОК). */
    private boolean isOkClicked = false;
    /** DAO-объект для работы с базой данных пользователей и ролей. */
    private final UserDaoImpl userDao = new UserDaoImpl();

    /**
     * Устанавливает ссылку на модальное окно диалога.
     * @param dialogStage объект Stage для управления окном.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Возвращает статус подтверждения сохранения данных пользователем.
     * @return true, если пользователь нажал кнопку ОК, иначе false.
     */
    public boolean isOkClicked() {
        return isOkClicked;
    }

    /**
     * Инициализирует контроллер после загрузки FXML-файла.
     * Загружает список доступных ролей из базы данных и заполняет выпадающий список.
     *
     * @param location  URL-адрес для разрешения относительных путей, или null.
     * @param resources Ресурсы для локализации, или null.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            comboRole.setItems(FXCollections.observableArrayList(userDao.findAllRoles()));
            logger.info("Инициализация формы пользователя. Загружено {} ролей.", comboRole.getItems().size());
        } catch (Exception e) {
            logger.error("Критическая ошибка при загрузке списка ролей из БД.", e);
        }
    }

    /**
     * Инициализирует форму данными существующего пользователя.
     * В текущей реализации используется в основном для подготовки объекта перед созданием.
     *
     * @param user объект User для редактирования.
     */
    public void setUser(User user) {
        this.user = user;
        // Если это создание нового пользователя, пароль обязателен.
        // Если редактирование старого - поля заполнятся (пока реализуем только создание)
        logger.debug("Форма подготовки данных пользователя инициализирована.");
    }

    /**
     * Обрабатывает нажатие кнопки ОК. Валидирует введенные данные
     * и сохраняет нового пользователя в базу данных (пароль хешируется внутри DAO),
     * после чего закрывает диалоговое окно.
     */
    @FXML
    private void handleOk() {
        if (isInputValid()) {
            user.setRole(comboRole.getValue());
            user.setLogin(tfLogin.getText().trim());
            user.setLastName(tfLastName.getText().trim());
            user.setFirstName(tfFirstName.getText().trim());

            // Сохраняем в базу (пароль захешируется внутри DAO)
            try {
                userDao.save(user, pfPassword.getText());
                isOkClicked = true;
                dialogStage.close();

                logger.info("Создан новый пользователь: Логин='{}', ФИО='{} {}', Роль='{}'.",
                        user.getLogin(), user.getLastName(), user.getFirstName(), user.getRole().getRoleName());
            } catch (Exception e) {
                logger.error("Ошибка при сохранении пользователя '{}' в базу данных.", user.getLogin(), e);
                new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
            }
        }
    }

    /**
     * Обрабатывает нажатие кнопки Отмена. Закрывает диалоговое окно без сохранения изменений.
     */
    @FXML
    private void handleCancel() {
        logger.debug("Создание/редактирование пользователя отменено.");
        dialogStage.close();
    }

    /**
     * Проверяет корректность заполнения всех полей формы.
     * Проверяет заполненность полей и корректность формата ФИО (кириллица, заглавная буква).
     *
     * @return true, если данные валидны, иначе false.
     */
    private boolean isInputValid() {
        if (comboRole.getValue() == null || tfLogin.getText().isEmpty() ||
                pfPassword.getText().isEmpty() || tfLastName.getText().isEmpty() || tfFirstName.getText().isEmpty()) {
            logger.warn("Валидация формы пользователя не пройдена: не заполнены обязательные поля.");
            new Alert(Alert.AlertType.WARNING, "Заполните все обязательные поля!").showAndWait();
            return false;
        }

        if (!isFioValid(tfLastName.getText().trim())) {
            logger.warn("Валидация формы пользователя не пройдена: некорректный формат Фамилии ({}).", tfLastName.getText());
            new Alert(Alert.AlertType.WARNING, "Фамилия должна быть на кириллице и начинаться с заглавной буквы!").showAndWait();
            return false;
        }

        if (!isFioValid(tfFirstName.getText().trim())) {
            logger.warn("Валидация формы пользователя не пройдена: некорректный формат Имени ({}).", tfFirstName.getText());
            new Alert(Alert.AlertType.WARNING, "Имя должно быть на кириллице и начинаться с заглавной буквы!").showAndWait();
            return false;
        }

        return true;
    }

    /**
     * Валидирует часть ФИО (фамилию или имя).
     * Проверяет, что строка начинается с заглавной буквы и содержит только кириллицу (допускается дефис).
     *
     * @param fioPart часть ФИО для проверки.
     * @return true, если формат корректен, иначе false.
     */
    private boolean isFioValid(String fioPart) {
        return fioPart != null && fioPart.matches("^[А-ЯЁ][а-яё]*(-[А-ЯЁ][а-яё]*)?$");
    }

    /**
     * Валидирует формат паспортных данных (10 цифр).
     * Примечание: в текущей версии контроллера для пользователей не используется,
     * но оставлен для возможного расширения функционала.
     *
     * @param passport строка паспортных данных для проверки.
     * @return true, если формат корректен, иначе false.
     */
    private boolean isPassportValid(String passport) {
        return passport != null && passport.matches("\\d{10}");
    }
}