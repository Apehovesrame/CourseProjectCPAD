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

import java.util.Locale;
import java.util.ResourceBundle;

public class LoginUITest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        Locale.setDefault(new Locale("ru", "RU"));
        ResourceBundle bundle = ResourceBundle.getBundle("main", Locale.getDefault());
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/pin123/courseprojectcpad/view/login-view.fxml"), bundle);

        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    @DisplayName("Негативный: Попытка входа с пустым логином")
    void testEmptyLogin() {
        clickOn("Войти в систему");

        // НАДЕЖНАЯ ПРОВЕРКА: Ищем системное окно ошибки (Alert) по его CSS-классу
        FxAssert.verifyThat(".dialog-pane.error", NodeMatchers.isVisible());

        // Закрываем окно ошибки универсально - имитируем нажатие клавиши ENTER
        type(KeyCode.ENTER);
    }

    @Test
    @DisplayName("Негативный: Попытка входа с неверным паролем")
    void testWrongPassword() throws InterruptedException {
        clickOn("#txtLogin").write("admin");
        clickOn("#txtPassword").write("wrong_password_123");

        clickOn("Войти в систему");

        // Ждем долю секунды, пока отработает БД
        Thread.sleep(500);

        // Проверяем, что появилось окно ошибки
        FxAssert.verifyThat(".dialog-pane.error", NodeMatchers.isVisible());

        type(KeyCode.ENTER);
    }
}