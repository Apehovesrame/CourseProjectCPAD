package ru.pin123.courseprojectcpad;

import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxAssert;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import ru.pin123.courseprojectcpad.model.Bus;
import ru.pin123.courseprojectcpad.service.BusService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testfx.util.WaitForAsyncUtils.waitFor;

public class BusesTabTest extends ApplicationTest {

    private final BusService busService = new BusService();
    // Список для автоматической подчистки базы данных после тестов
    private final List<String> platesToClean = new ArrayList<>();

    @Override
    public void start(Stage stage) throws Exception {
        // Инициализируем приложение БЕЗ кликов робота (это безопасно)
        new MainApplication().start(stage);
    }

    @BeforeEach
    void setUp() throws Exception {
        // 1. Робот проходит авторизацию
        clickOn("#loginField").write("appuser");
        clickOn("#passwordField").write("1234");
        clickOn("Войти");

        // 2. Ждем 1 секунду, чтобы БД подключилась и главное окно полностью прогрузилось

        // 3. Убеждаемся, что панель с вкладками (TabPane) появилась на экране
        waitFor(5, TimeUnit.SECONDS, () -> lookup("#mainTabPane").tryQuery().isPresent());

        // 4. КЛИКАЕМ ПО ВКЛАДКЕ (робот найдет её по тексту на экране)
        clickOn("Автобусы");

        // 5. Теперь, когда вкладка открыта, безопасно ждем появления таблицы
        waitFor(5, TimeUnit.SECONDS, () -> lookup("#busTable").tryQuery().isPresent());
    }

    @AfterEach
    void cleanUp() throws Exception {
        // Очистка БД от тестовых записей
        for (String plate : platesToClean) {
            busService.getAllBuses().stream()
                    .filter(bus -> bus.getLicensePlate().equals(plate))
                    .findFirst()
                    .ifPresent(bus -> busService.deleteBus(bus.getBusId()));
        }
        platesToClean.clear();
    }

    // ПОЗИТИВНЫЕ ТЕСТЫ

    @Test
    @DisplayName("Положительный: Успешное добавление автобуса")
    void testAddBusPositive() throws Exception {
        String testModel = "Тест-ЛиАЗ";
        String testPlate = "О777ОО777";

        // Кликаем по кнопке Добавить
        clickOn("#addBusButton");

        // Динамически ждем открытия карточки редактирования
        waitFor(3, TimeUnit.SECONDS, () -> lookup("#tfModel").query().isVisible());

        // Заполняем поля ввода
        clickOn("#tfModel").write(testModel);
        clickOn("#tfLicensePlate").write(testPlate);
        clickOn("#tfSeatCapacity").write("50");

        // Регистрируем госномер для удаления
        platesToClean.add(testPlate);

        // Сохраняем карточку
        clickOn("#btnSaveBus");
        Thread.sleep(600); // Время на обновление JavaFX потока

        // Проверяем, что автобус успешно добавился в TableView
        TableView<Bus> table = lookup("#busTable").query();
        boolean found = table.getItems().stream()
                .anyMatch(b -> b.getLicensePlate().equals(testPlate) && b.getModel().equals(testModel));

        assertThat(found).isTrue();
    }

    @Test
    @DisplayName("Положительный: Удаление выбранного автобуса")
    void testDeleteBusPositive() throws Exception {
        Bus temporaryBus = new Bus();
        temporaryBus.setModel("Смертник-Тест");
        temporaryBus.setLicensePlate("Т666ТТ66");
        temporaryBus.setSeatCapacity(20);
        busService.saveBus(temporaryBus);

        // 2. Так как у нас нет кнопки "Обновить", принудительно просим JavaFX
        javafx.application.Platform.runLater(() -> {
            TableView<Bus> table = lookup("#busTable").queryAs(TableView.class);
            table.getItems().setAll(busService.getAllBuses());
        });

        Thread.sleep(500);

        // 3. Теперь робот 100% увидит строку на экране. Кликаем по ней!
        clickOn("Т666ТТ66");

        // 4. Нажимаем кнопку удаления
        clickOn("#deleteBusButton");

        Thread.sleep(600);

        // 5. Убеждаемся, что запись навсегда исчезла из TableView
        TableView<Bus> table = lookup("#busTable").query();
        boolean found = table.getItems().stream()
                .anyMatch(b -> b.getLicensePlate().equals("Т666ТТ66"));

        assertThat(found).isFalse();
    }

    // НЕГАТИВНЫЕ ТЕСТЫ
    @Test
    @DisplayName("Негативный: Проверка триггера пустых полей")
    void testAddBusNegativeEmptyFields() throws Exception {
        clickOn("#addBusButton");
        waitFor(3, TimeUnit.SECONDS, () -> lookup("#tfModel").query().isVisible());
        // Не заполняем поля и сразу жмем сохранить
        clickOn("#btnSaveBus");
        // Проверяем появление окна Alert с ошибкой
        FxAssert.verifyThat("Пожалуйста, исправьте следующие ошибки:", NodeMatchers.isVisible());
        // Закрываем информационное окно ошибки и форму
        clickOn("OK");
        clickOn("#btnCancelBus");
    }

    @Test
    @DisplayName("Негативный: Ввод некорректного формата вместимости")
    void testAddBusNegativeCapacityCharacters() throws Exception { // Исправлено: добавили throws Exception
        clickOn("#addBusButton");
        waitFor(3, TimeUnit.SECONDS, () -> lookup("#tfModel").query().isVisible());
        // Заполняем текстовые поля, но ломаем числовое
        clickOn("#tfModel").write("Газель");
        clickOn("#tfLicensePlate").write("М111ММ11");
        clickOn("#tfSeatCapacity").write("Десять"); // Ошибка формата текста!
        clickOn("#btnSaveBus");
        // Проверяем, отработал ли catch-блок исключения NumberFormatException
        FxAssert.verifyThat("Пожалуйста, исправьте следующие ошибки:", NodeMatchers.isVisible());
        // Закрываем окна
        clickOn("OK");
        clickOn("#btnCancelBus");
    }
}