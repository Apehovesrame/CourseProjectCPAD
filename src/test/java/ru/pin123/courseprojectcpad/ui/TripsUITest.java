package ru.pin123.courseprojectcpad.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxAssert;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;

import java.util.Locale;
import java.util.ResourceBundle;

public class TripsUITest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        Locale.setDefault(new Locale("ru", "RU"));
        ResourceBundle bundle = ResourceBundle.getBundle("main", Locale.getDefault());
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/pin123/courseprojectcpad/view/trips-view.fxml"), bundle);

        Parent root = loader.load();
        stage.setScene(new Scene(root, 1000, 600));
        stage.show();
    }

    @Test
    @DisplayName("Позитивный: Проверка отображения элементов интерфейса рейсов")
    void testTripsUiElementsVisible() {
        FxAssert.verifyThat("#tripTable", NodeMatchers.isVisible());
        FxAssert.verifyThat("Создать рейс", NodeMatchers.isVisible());
        FxAssert.verifyThat("Редактировать", NodeMatchers.isVisible());
        FxAssert.verifyThat("Удалить", NodeMatchers.isVisible());
    }
}