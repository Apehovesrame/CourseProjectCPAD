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
import ru.pin123.courseprojectcpad.model.Session;
import ru.pin123.courseprojectcpad.model.User;

import java.util.Locale;
import java.util.ResourceBundle;

public class TicketSellUITest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        // Окно кассы может требовать имя кассира, поэтому искусственно "логинимся" перед тестом
        User mockUser = new User();
        mockUser.setFirstName("Тест");
        mockUser.setLastName("Кассир");
        Session.setCurrentUser(mockUser);

        Locale.setDefault(new Locale("ru", "RU"));
        ResourceBundle bundle = ResourceBundle.getBundle("main", Locale.getDefault());
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/pin123/courseprojectcpad/view/ticket-sell-view.fxml"), bundle);

        Parent root = loader.load();
        stage.setScene(new Scene(root, 1000, 700));
        stage.show();
    }

    @Test
    @DisplayName("Негативный: Попытка оформить билет с пустыми полями")
    void testEmptyTicketSell() {
        // Кликаем по кнопке продажи по её fx:id (это надежнее, чем искать по тексту)
        clickOn("#sellButton");

        // Убеждаемся, что система не дала продать билет и вывела предупреждение
        // Ищем окно с желтым значком Warning (у него CSS-класс .warning)
        FxAssert.verifyThat(".dialog-pane.warning", NodeMatchers.isVisible());

        // Закрываем окно предупреждения
        type(KeyCode.ENTER);
    }
}