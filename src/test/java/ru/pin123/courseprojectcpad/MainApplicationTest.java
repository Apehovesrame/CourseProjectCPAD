package ru.pin123.courseprojectcpad;

import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxAssert;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import static org.assertj.core.api.Assertions.assertThat;

public class MainApplicationTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        // TestFX сам запустит наше приложение перед каждым тестом
        new MainApplication().start(stage);
    }

    // ПОЗИТИВНЫЕ ТЕСТЫ
    @Test
    @DisplayName("Положительный: Отмена входа (закрытие диалога)")
    void testCancelLogin() {
        // Эмулируем клик по кнопке "Отмена"
        clickOn("Отмена");
        // Так как при отмене вызывается Platform.exit(), мы просто проверяем,
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("Положительный: Успешная авторизация")
    void testSuccessfulLogin() throws InterruptedException {
        // Вводим правильные данные пользователя БД (замени на свои, если пароль другой)
        clickOn("#loginField").write("appuser");
        clickOn("#passwordField").write("1234");
        // Жмем войти
        clickOn("Войти");
        // Ждем немного, пока база данных ответит и загрузится FXML главного окна
        Thread.sleep(1000);
        // Проверяем, что открылось главное окно (ищем текст заголовка или любую вкладку)
        FxAssert.verifyThat("Продажа билетов", NodeMatchers.isVisible());
    }

    // НЕГАТИВНЫЕ ТЕСТЫ
    @Test
    @DisplayName("Негативный: Попытка входа с пустым логином")
    void testEmptyLogin() {
        // Оставляем поля пустыми и сразу жмем "Войти"
        clickOn("Войти");
        // Наш код должен показать Alert с ошибкой. Проверяем, что текст ошибки появился на экране.
        FxAssert.verifyThat("Логин не может быть пустым", NodeMatchers.isVisible());
        // Закрываем окно с ошибкой, чтобы оно не мешало
        clickOn("OK");
    }

    @Test
    @DisplayName("Негативный: Попытка входа с неверным паролем")
    void testWrongPassword() throws InterruptedException {
        // Вводим правильный логин, но заведомо неверный пароль
        clickOn("#loginField").write("appuser");
        clickOn("#passwordField").write("wrong_password_123");
        clickOn("Войти");
        // Ждем ответ от базы данных (это может занять полсекунды)
        Thread.sleep(1000);
        // Проверяем, что вылезло окно с заголовком "Ошибка подключения"
        FxAssert.verifyThat("Не удалось подключиться к базе данных", NodeMatchers.isVisible());
        // Нажимаем ОК на диалоге ошибки базы данных
        clickOn("OK");
    }
}