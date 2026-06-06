package ru.pin123.courseprojectcpad.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ru.pin123.courseprojectcpad.model.ReportRow;
import ru.pin123.courseprojectcpad.service.ReportService;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class ReportsController implements Initializable {

    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;

    // Элементы таблицы
    @FXML private TableView<ReportRow> tableReport;
    @FXML private TableColumn<ReportRow, String> colRoute;
    @FXML private TableColumn<ReportRow, Integer> colTickets;
    @FXML private TableColumn<ReportRow, BigDecimal> colRevenue;

    // Итоговая сумма
    @FXML private Label lblTotalSum;

    private final ReportService reportService = new ReportService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Связываем колонки таблицы с полями класса ReportRow
        // Строки "routeName", "ticketsSold", "totalRevenue" должны ТОЧНО совпадать с названиями переменных в ReportRow!
        colRoute.setCellValueFactory(new PropertyValueFactory<>("routeName"));
        colTickets.setCellValueFactory(new PropertyValueFactory<>("ticketsSold"));
        colRevenue.setCellValueFactory(new PropertyValueFactory<>("totalRevenue"));

        // Устанавливаем даты по умолчанию (например, текущий месяц)
        dpStartDate.setValue(LocalDate.now().withDayOfMonth(1));
        dpEndDate.setValue(LocalDate.now());
    }

    @FXML
    public void onGenerateReportClick(ActionEvent event) {
        try {
            LocalDate start = dpStartDate.getValue();
            LocalDate end = dpEndDate.getValue();

            // Получаем данные из базы
            List<ReportRow> data = reportService.generateRevenueReport(start, end);

            // Загружаем данные в таблицу
            tableReport.setItems(FXCollections.observableArrayList(data));

            // Считаем общую итоговую сумму по всем маршрутам
            BigDecimal totalSum = data.stream()
                    .map(ReportRow::getTotalRevenue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            lblTotalSum.setText("Итого выручка: " + totalSum + " руб.");

        } catch (RuntimeException e) {
            // Этого достаточно: RuntimeException перехватит и IllegalArgumentException,
            // и любые другие ошибки, возникшие в сервисе или DAO
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка формирования отчета");
            alert.setHeaderText(null);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}