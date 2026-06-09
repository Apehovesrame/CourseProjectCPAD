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

public class MainNavigationUITest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        Locale.setDefault(new Locale("ru", "RU"));
        ResourceBundle bundle = ResourceBundle.getBundle("main", Locale.getDefault());
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/pin123/courseprojectcpad/view/main-view.fxml"), bundle);

        Parent root = loader.load();
        // Задаем размер окна побольше, чтобы левое меню полностью поместилось на экране
        stage.setScene(new Scene(root, 1200, 800));
        stage.show();
    }

    @Test
    @DisplayName("Позитивный: Переход в Базу пассажиров")
    void testNavigateToPassengers() {
        // Кликаем по кнопке в боковом меню
        clickOn("База пассажиров");

        // Проверяем, что в центре экрана загрузился нужный FXML
        // (ищем кнопку, которая точно есть в passengers-view.fxml)
        FxAssert.verifyThat("Добавить пассажира", NodeMatchers.isVisible());
        FxAssert.verifyThat("История поездок", NodeMatchers.isVisible());
    }

    @Test
    @DisplayName("Позитивный: Переход в Справочник водителей")
    void testNavigateToDrivers() {
        clickOn("Справочник водителей");

        // Проверяем, что загрузился интерфейс водителей (ищем текст из карточки профиля)
        FxAssert.verifyThat("Личная карточка", NodeMatchers.isVisible());
        FxAssert.verifyThat("Добавить водителя", NodeMatchers.isVisible());
    }

    @Test
    @DisplayName("Позитивный: Переход в Продажу билета")
    void testNavigateToTicketSell() {
        clickOn("Продажа билета");

        // Проверяем, что загрузился интерфейс кассы (проверяем наличие кнопки продажи)
        FxAssert.verifyThat("Информация о маршруте", NodeMatchers.isVisible());
    }
}