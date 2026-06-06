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

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class ReportsController implements Initializable {

    @FXML private DatePicker dpStart;
    @FXML private DatePicker dpEnd;

    @FXML private TableView<RouteReportItem> reportTable;
    @FXML private TableColumn<RouteReportItem, String> colDestination;
    @FXML private TableColumn<RouteReportItem, Integer> colTickets;
    @FXML private TableColumn<RouteReportItem, BigDecimal> colRevenue;

    @FXML private PieChart pieChart;
    @FXML private Label lblTotalRevenue;

    private final ReportDaoImpl reportDao = new ReportDaoImpl();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colDestination.setCellValueFactory(new PropertyValueFactory<>("destination"));
        colTickets.setCellValueFactory(new PropertyValueFactory<>("ticketsCount"));
        colRevenue.setCellValueFactory(new PropertyValueFactory<>("revenue"));

        // Устанавливаем значения по умолчанию: отчет за последний месяц
        dpEnd.setValue(LocalDate.now());
        dpStart.setValue(LocalDate.now().minusMonths(1));

        // Сразу формируем отчет при открытии вкладки
        handleGenerateReport();
    }

    @FXML
    private void handleGenerateReport() {
        LocalDate start = dpStart.getValue();
        LocalDate end = dpEnd.getValue();

        if (start == null || end == null) {
            new Alert(Alert.AlertType.WARNING, "Пожалуйста, выберите период!").showAndWait();
            return;
        }

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

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Ошибка загрузки отчета: " + e.getMessage()).showAndWait();
        }
    }
}