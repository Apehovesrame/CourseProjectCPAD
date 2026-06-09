package ru.pin123.courseprojectcpad.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxAssert;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import ru.pin123.courseprojectcpad.controller.PassengerEditController;
import ru.pin123.courseprojectcpad.model.Passenger;

import java.util.Locale;
import java.util.ResourceBundle;

public class ValidationUITest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        Locale.setDefault(new Locale("ru", "RU"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/pin123/courseprojectcpad/view/passenger-edit-view.fxml"));
        Parent root = loader.load();

        // Для формы редактирования обязательно нужно передать пустого пассажира в контроллер,
        // иначе при попытке сохранить программа выдаст NullPointerException
        PassengerEditController controller = loader.getController();
        controller.setDialogStage(stage);
        controller.setPassenger(new Passenger());

        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    @DisplayName("Негативный: Ввод ФИО с маленькой буквы")
    void testLowercaseFioValidation() {
        // Робот вводит фамилию с маленькой буквы
        clickOn("#tfLastName").write("иванов");
        clickOn("#tfFirstName").write("Иван");
        clickOn("#tfPassport").write("1234567890"); // Пробел поставится сам благодаря нашей маске
        clickOn("#tfBirthYear").write("1995");

        // Нажимаем кнопку сохранения (Замени "Сохранить" на точный текст кнопки из твоего passenger-edit-view.fxml, если он отличается)
        clickOn("Сохранить");

        // Проверяем, что появилось окно с ошибкой (класс .error)
        FxAssert.verifyThat(".dialog-pane.error", NodeMatchers.isVisible());

        // Закрываем окно ошибки
        type(KeyCode.ENTER);
    }

    @Test
    @DisplayName("Негативный: Ввод букв вместо года рождения")
    void testInvalidBirthYearValidation() {
        clickOn("#tfLastName").write("Петров");
        clickOn("#tfFirstName").write("Петр");
        clickOn("#tfPassport").write("0987654321");

        clickOn("Сохранить");

        // Ожидаем появление Alert об ошибке заполнения
        FxAssert.verifyThat(".dialog-pane.error", NodeMatchers.isVisible());
        type(KeyCode.ENTER);
    }
}