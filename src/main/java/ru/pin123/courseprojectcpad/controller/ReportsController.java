package ru.pin123.courseprojectcpad.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ru.pin123.courseprojectcpad.dao.ReportDaoImpl;
import ru.pin123.courseprojectcpad.model.RouteReportItem;
import javafx.stage.FileChooser;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Контроллер для генерации и отображения отчетов по продажам билетов.
 * Отвечает за выбор периода, формирование табличных данных, визуализацию распределения продаж
 * по направлениям (круговая диаграмма) и экспорт результатов в CSV-файл.
 */
public class ReportsController implements Initializable {

    /** Логгер для фиксации событий генерации отчетов и экспорта данных. */
    private static final Logger logger = LoggerFactory.getLogger(ReportsController.class);

    /** Поле выбора начальной даты периода отчета. */
    @FXML private DatePicker dpStart;
    /** Поле выбора конечной даты периода отчета. */
    @FXML private DatePicker dpEnd;

    /** Таблица для отображения данных отчета по направлениям. */
    @FXML private TableView<RouteReportItem> reportTable;
    /** Колонка с названием направления (пункт назначения). */
    @FXML private TableColumn<RouteReportItem, String> colDestination;
    /** Колонка с количеством проданных билетов. */
    @FXML private TableColumn<RouteReportItem, Integer> colTickets;
    /** Колонка с суммарной выручкой по направлению. */
    @FXML private TableColumn<RouteReportItem, BigDecimal> colRevenue;

    /** Круговая диаграмма для визуализации распределения продаж по направлениям. */
    @FXML private PieChart pieChart;
    /** Метка для отображения итоговой выручки за выбранный период. */
    @FXML private Label lblTotalRevenue;

    /** DAO-объект для получения данных отчетов из базы данных. */
    private final ReportDaoImpl reportDao = new ReportDaoImpl();

    /**
     * Инициализирует контроллер после загрузки FXML-файла.
     * Настраивает привязку данных для колонок таблицы, устанавливает период по умолчанию
     * (последний месяц) и автоматически генерирует отчет при открытии.
     *
     * @param location  URL-адрес для разрешения относительных путей, или null.
     * @param resources Ресурсы для локализации, или null.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colDestination.setCellValueFactory(new PropertyValueFactory<>("destination"));
        colTickets.setCellValueFactory(new PropertyValueFactory<>("ticketsCount"));
        colRevenue.setCellValueFactory(new PropertyValueFactory<>("revenue"));

        // Устанавливаем значения по умолчанию: отчет за последний месяц
        dpEnd.setValue(LocalDate.now());
        dpStart.setValue(LocalDate.now().minusMonths(1));

        logger.info("Инициализация контроллера отчетов. Период по умолчанию: с {} по {}.",
                dpStart.getValue(), dpEnd.getValue());

        // Сразу формируем отчет при открытии вкладки
        handleGenerateReport();
    }

    /**
     * Обрабатывает нажатие кнопки "Сформировать отчет".
     * Получает данные из БД за выбранный период, обновляет таблицу, круговую диаграмму
     * и итоговую сумму выручки.
     */
    @FXML
    private void handleGenerateReport() {
        LocalDate start = dpStart.getValue();
        LocalDate end = dpEnd.getValue();

        if (start == null || end == null) {
            logger.warn("Попытка формирования отчета без указания периода.");
            new Alert(Alert.AlertType.WARNING, "Пожалуйста, выберите период!").showAndWait();
            return;
        }

        logger.info("Формирование отчета по продажам за период: с {} по {}.", start, end);

        try {
            List<RouteReportItem> reportData = reportDao.getSalesReport(start, end);

            // 1. Обновляем таблицу
            reportTable.setItems(FXCollections.observableArrayList(reportData));

            // 2. Обновляем график и считаем итоговую сумму
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            BigDecimal totalRevenue = BigDecimal.ZERO;

            for (RouteReportItem item : reportData) {
                pieData.add(new PieChart.Data(item.getDestination(), item.getTicketsCount()));
                totalRevenue = totalRevenue.add(item.getRevenue());
            }

            pieChart.setData(pieData);
            lblTotalRevenue.setText(String.format("%.2f руб.", totalRevenue));

            logger.info("Отчет успешно сформирован. Количество направлений: {}, общая выручка: {} руб.",
                    reportData.size(), totalRevenue);

        } catch (Exception e) {
            logger.error("Критическая ошибка при формировании отчета за период с {} по {}.", start, end, e);
            new Alert(Alert.AlertType.ERROR, "Ошибка загрузки отчета: " + e.getMessage()).showAndWait();
        }
    }

    /**
     * Обрабатывает нажатие кнопки "Экспорт в CSV".
     * Выгружает текущие данные отчета в CSV-файл с кодировкой UTF-8 (с BOM для совместимости с MS Excel).
     * Разделитель - точка с запятой.
     */
    @FXML
    private void handleExportCsv() {
        if (reportTable.getItems().isEmpty()) {
            logger.warn("Попытка экспорта пустого отчета в CSV.");
            showAlert(Alert.AlertType.WARNING, "Отчет пуст", "Сначала сформируйте отчет для выгрузки.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить отчет");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Файлы", "*.csv"));
        fileChooser.setInitialFileName("sales_report.csv");

        java.io.File file = fileChooser.showSaveDialog(reportTable.getScene().getWindow());

        if (file != null) {
            logger.info("Начало экспорта отчета в файл: {}", file.getAbsolutePath());

            // Используем UTF-8 с BOM для корректного отображения кириллицы в MS Excel
            try (java.io.PrintWriter writer = new java.io.PrintWriter(
                    new java.io.OutputStreamWriter(new java.io.FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8))) {

                writer.write('\ufeff'); // BOM маркер
                writer.println("Направление;Продано билетов;Выручка (руб)");

                for (RouteReportItem item : reportTable.getItems()) {
                    writer.println(String.format("%s;%d;%s",
                            item.getDestination(),
                            item.getTicketsCount(),
                            item.getRevenue().toString()));
                }

                logger.info("Отчет успешно экспортирован в CSV. Количество записей: {}.", reportTable.getItems().size());
                showAlert(Alert.AlertType.INFORMATION, "Успех", "Отчет успешно сохранен!");

            } catch (Exception e) {
                logger.error("Ошибка при экспорте отчета в файл: {}", file.getAbsolutePath(), e);
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось сохранить файл: " + e.getMessage());
            }
        } else {
            logger.debug("Экспорт отчета в CSV был отменен пользователем.");
        }
    }

    /**
     * Отображает модальное всплывающее окно с сообщением для пользователя.
     *
     * @param type    тип предупреждения (INFO, WARNING, ERROR и т.д.).
     * @param title   заголовок окна.
     * @param content текстовое содержание сообщения.
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}