package com.oop.moneymanager.controller;

import com.oop.moneymanager.model.Transaction;
import com.oop.moneymanager.util.JsonUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.chart.PieChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.util.StringConverter;
import java.time.YearMonth;
import javafx.scene.Node;
import javafx.scene.control.DateCell;

public class ReportController {
    @FXML private VBox mainContainer;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    private DatePicker monthYearPicker;
    private Label titleLabel;
    private VBox reportContent;


    @FXML public void initialize(){
        try {
            setupUI();
            updateReport(LocalDate.now());
        } catch (Exception e) {
            e.printStackTrace();
            mainContainer.getChildren().add(new Label("Error memuat halaman laporan: " + e.getMessage()));
        }
    }

    private void setupUI() {
        mainContainer.setPadding(new Insets(25));
        mainContainer.setSpacing(20);
        mainContainer.getChildren().clear();

        titleLabel = new Label();
        titleLabel.getStyleClass().add("content-title");

        monthYearPicker = new DatePicker(LocalDate.now());
        monthYearPicker.setPromptText("Select Period");

        monthYearPicker.setConverter(new StringConverter<>() {
            private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMMM uuuu");
            @Override public String toString(LocalDate date) { return date != null ? dtf.format(date) : ""; }
            @Override public LocalDate fromString(String string) {
                try { return YearMonth.parse(string, dtf).atDay(1); }
                catch (Exception e) { return null; }
            }
        });

        monthYearPicker.valueProperty().addListener((obs, oldVal, newVal) -> updateReport(newVal));

        VBox headerContainer = new VBox(5, titleLabel, monthYearPicker);
        headerContainer.setAlignment(Pos.CENTER);

        reportContent = new VBox(20);
        reportContent.setAlignment(Pos.TOP_CENTER);

        mainContainer.getChildren().addAll(headerContainer, reportContent);
    }


    private void updateReport(LocalDate date) {
        if(date == null) return;

        titleLabel.setText("Monthly Report: " + date.format(DateTimeFormatter.ofPattern("MMMM uuuu")));

        reportContent.getChildren().clear();

        List<Transaction> transactions = JsonUtil.loadTransactions().stream()
                .filter(t -> YearMonth.from(t.getDate()).equals(YearMonth.from(date)))
                .collect(Collectors.toList());

        GridPane summaryGrid = createSummaryGrid(transactions);
        summaryGrid.setPadding(new Insets(10, 0, 20, 0));

        GridPane chartsGrid = new GridPane();
        chartsGrid.setHgap(30);
        ColumnConstraints col50 = new ColumnConstraints();
        col50.setPercentWidth(50);
        chartsGrid.getColumnConstraints().addAll(col50, col50);

        PieChart incomeChart = createCategoryChart(transactions, Transaction.Type.INCOME, "Income Breakdown by Category");
        PieChart expenseChart = createCategoryChart(transactions, Transaction.Type.EXPENSE, "Expense Breakdown by Category");

        chartsGrid.add(incomeChart, 0, 0);
        chartsGrid.add(expenseChart, 1, 0);

        if (transactions.isEmpty()) {
            Label noDataLabel = new Label("No transaction data available for this month.");
            noDataLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6b7280;");
            reportContent.getChildren().add(noDataLabel);
            reportContent.setAlignment(Pos.CENTER);
        } else {
            reportContent.setAlignment(Pos.TOP_CENTER);
            reportContent.getChildren().addAll(summaryGrid, chartsGrid);
        }
    }

    private GridPane createSummaryGrid(List<Transaction> transactions) {
        double totalIncome = transactions.stream().filter(t -> t.getType() == Transaction.Type.INCOME).mapToDouble(Transaction::getAmount).sum();
        double totalExpense = transactions.stream().filter(t -> t.getType() == Transaction.Type.EXPENSE).mapToDouble(Transaction::getAmount).sum();
        double netBalance = totalIncome - totalExpense;

        GridPane summaryGrid = new GridPane();
        summaryGrid.setHgap(40);
        summaryGrid.setVgap(15);
        summaryGrid.setAlignment(Pos.CENTER);

        summaryGrid.add(new Label("Total Income:"), 0, 0);
        summaryGrid.add(createStyledLabel(currencyFormatter.format(totalIncome), "amount-income-cell"), 1, 0);
        summaryGrid.add(new Label("Total Expense:"), 2, 0);
        summaryGrid.add(createStyledLabel(currencyFormatter.format(totalExpense), "amount-expense-cell"), 3, 0);
        summaryGrid.add(new Label("Net Balance:"), 4, 0);
        Label netLabel = createStyledLabel(currencyFormatter.format(netBalance), "net-balance-label");
        summaryGrid.add(netLabel, 5, 0);

        return summaryGrid;
    }

    private PieChart createCategoryChart(List<Transaction> transactions, Transaction.Type type, String title) {
        Map<String, Double> dataMap = transactions.stream()
                .filter(t -> t.getType() == type && t.getAmount() > 0)
                .collect(Collectors.groupingBy(Transaction::getCategoryName, Collectors.summingDouble(Transaction::getAmount)));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        dataMap.forEach((category, amount) -> pieChartData.add(new PieChart.Data(category, amount)));

        PieChart chart = new PieChart(pieChartData);
        chart.setTitle(title);
        chart.setLegendSide(Side.BOTTOM);
        chart.setLabelsVisible(true);
        chart.setMaxSize(400, 400);

        if (pieChartData.isEmpty()) {
            chart.setTitle("No " + type.toString().toLowerCase() + " data");
        }

        return chart;
    }

    private Label createStyledLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        label.setStyle("-fx-font-size: 18px;");
        return label;
    }
}
