package ru.pin123.courseprojectcpad.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxAssert;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import ru.pin123.courseprojectcpad.controller.PassengerEditController;
import ru.pin123.courseprojectcpad.model.Passenger;

import java.util.ResourceBundle;

public class ValidationUITest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        ResourceBundle bundle = ResourceBundle.getBundle("main_ru");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/pin123/courseprojectcpad/view/passenger-edit-view.fxml"), bundle);
        Parent root = loader.load();

        PassengerEditController controller = loader.getController();
        controller.setDialogStage(stage);
        controller.setPassenger(new Passenger());

        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    public void testLowercaseFioValidation() {
        // Вводим ФИО с маленькой буквы (ожидаем ошибку)
        clickOn("#tfLastName").write("иванов");
        clickOn("#tfFirstName").write("Иван");
        clickOn("#tfPassport").write("1234567890");

        // Поля года рождения больше нет, поэтому сразу жмем Сохранить
        clickOn(".button");

        // Проверяем, что появилось окно с ошибкой (класс .error или .warning)
        FxAssert.verifyThat(".dialog-pane", NodeMatchers.isVisible());
    }
}