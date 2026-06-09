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

    private static final Logger logger = LoggerFactory.getLogger(ReportsController.class);

    @FXML private DatePicker dpStart;
    @FXML private DatePicker dpEnd;
    @FXML private TableView<RouteReportItem> reportTable;
    @FXML private TableColumn<RouteReportItem, String> colDestination;
    @FXML private TableColumn<RouteReportItem, Integer> colTickets;
    @FXML private TableColumn<RouteReportItem, BigDecimal> colRevenue;
    @FXML private PieChart pieChart;
    @FXML private Label lblTotalRevenue;

    // Внедрение локализации
    @FXML private ResourceBundle resources;

    private final ReportDaoImpl reportDao = new ReportDaoImpl();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Сохраняем переданный бандл
        this.resources = resources;

        colDestination.setCellValueFactory(new PropertyValueFactory<>("destination"));
        colTickets.setCellValueFactory(new PropertyValueFactory<>("ticketsCount"));
        colRevenue.setCellValueFactory(new PropertyValueFactory<>("revenue"));

        dpEnd.setValue(LocalDate.now());
        dpStart.setValue(LocalDate.now().minusMonths(1));

        logger.info("Инициализация контроллера отчетов. Период по умолчанию: с {} по {}.", dpStart.getValue(), dpEnd.getValue());

        handleGenerateReport();
    }

    @FXML
    private void handleGenerateReport() {
        LocalDate start = dpStart.getValue();
        LocalDate end = dpEnd.getValue();

        if (start == null || end == null) {
            logger.warn("Попытка формирования отчета без указания периода.");
            // ИСПРАВЛЕНО: Локализация предупреждения
            showAlert(Alert.AlertType.WARNING, resources.getString("alert.warning.title"), resources.getString("reports.alert.select_period"));
            return;
        }

        logger.info("Формирование отчета по продажам за период: с {} по {}.", start, end);

        try {
            List<RouteReportItem> reportData = reportDao.getSalesReport(start, end);

            reportTable.setItems(FXCollections.observableArrayList(reportData));

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            BigDecimal totalRevenue = BigDecimal.ZERO;

            for (RouteReportItem item : reportData) {
                pieData.add(new PieChart.Data(item.getDestination(), item.getTicketsCount()));
                totalRevenue = totalRevenue.add(item.getRevenue());
            }

            pieChart.setData(pieData);
            lblTotalRevenue.setText(String.format("%.2f %s", totalRevenue, resources.getString("currency")));

            logger.info("Отчет успешно сформирован. Количество направлений: {}, общая выручка: {} руб.", reportData.size(), totalRevenue);

        } catch (Exception e) {
            logger.error("Критическая ошибка при формировании отчета за период с {} по {}.", start, end, e);
            // ИСПРАВЛЕНО: Локализация ошибки
            showAlert(Alert.AlertType.ERROR, resources.getString("alert.error.title"), resources.getString("reports.alert.load_error") + ": " + e.getMessage());
        }
    }

    @FXML
    private void handleExportCsv() {
        if (reportTable.getItems().isEmpty()) {
            logger.warn("Попытка экспорта пустого отчета в CSV.");
            // ИСПРАВЛЕНО: Локализация предупреждения о пустом отчете
            showAlert(Alert.AlertType.WARNING, resources.getString("alert.warning.title"), resources.getString("reports.alert.empty_report"));
            return;
        }

        FileChooser fileChooser = new FileChooser();
        // ИСПРАВЛЕНО: Локализация окна сохранения
        fileChooser.setTitle(resources.getString("reports.filechooser.title"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(resources.getString("reports.filechooser.csv"), "*.csv"));
        fileChooser.setInitialFileName("sales_report.csv");

        java.io.File file = fileChooser.showSaveDialog(reportTable.getScene().getWindow());

        if (file != null) {
            logger.info("Начало экспорта отчета в файл: {}", file.getAbsolutePath());

            try (java.io.PrintWriter writer = new java.io.PrintWriter(
                    new java.io.OutputStreamWriter(new java.io.FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8))) {

                writer.write('\ufeff');
                // ИСПРАВЛЕНО: Локализация заголовков столбцов внутри самого CSV файла
                writer.println(resources.getString("reports.csv.headers"));

                for (RouteReportItem item : reportTable.getItems()) {
                    writer.println(String.format("%s;%d;%s",
                            item.getDestination(),
                            item.getTicketsCount(),
                            item.getRevenue().toString()));
                }

                logger.info("Отчет успешно экспортирован в CSV. Количество записей: {}.", reportTable.getItems().size());
                // ИСПРАВЛЕНО: Локализация сообщения об успехе
                showAlert(Alert.AlertType.INFORMATION, resources.getString("alert.info.title"), resources.getString("reports.alert.save_success"));

            } catch (Exception e) {
                logger.error("Ошибка при экспорте отчета в файл: {}", file.getAbsolutePath(), e);
                // ИСПРАВЛЕНО: Локализация ошибки сохранения
                showAlert(Alert.AlertType.ERROR, resources.getString("alert.error.title"), resources.getString("reports.alert.save_error") + ": " + e.getMessage());
            }
        } else {
            logger.debug("Экспорт отчета в CSV был отменен пользователем.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}