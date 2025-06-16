package com.oop.moneymanager.controller;

import com.oop.moneymanager.model.Budget;
import com.oop.moneymanager.model.Category;
import com.oop.moneymanager.model.Transaction;
import com.oop.moneymanager.util.JsonUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class BudgetController {
    @FXML
    private VBox mainContainer;
    private final ObservableList<Budget> budgetData = FXCollections.observableArrayList();
    private TableView<Budget> budgetTable;
    private DatePicker monthYearPicker;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    @FXML
    public void initialize() {
        try {
            setupUI();
            filterBudgets();
        } catch (Exception e) {
            e.printStackTrace();
            mainContainer.getChildren().add(new Label("Error memuat halaman budget: " + e.getMessage()));
        }
    }

    private void setupUI() {
        mainContainer.setPadding(new Insets(25));
        mainContainer.setSpacing(20);
        mainContainer.getChildren().clear();

        Label title = new Label("Monthly Budgeting");
        title.getStyleClass().add("content-title");

        monthYearPicker = new DatePicker(LocalDate.now());
        monthYearPicker.setPromptText("Select Month and Year");
        // FIX: Mengganti format agar lebih jelas dan menggunakan LocalDate
        monthYearPicker.setConverter(new StringConverter<LocalDate>() {
            private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

            @Override
            public String toString(LocalDate date) {
                return (date != null) ? dtf.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return (string != null && !string.isEmpty()) ? YearMonth.parse(string, dtf).atDay(1) : null;
            }
        });

        monthYearPicker.setOnAction(e -> filterBudgets());

        HBox filterBox = new HBox(10, new Label("Select period:"), monthYearPicker);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        setupTable();

        mainContainer.getChildren().addAll(title, filterBox, budgetTable);
        VBox.setVgrow(budgetTable, javafx.scene.layout.Priority.ALWAYS);
    }

    private void setupTable() {
        budgetTable = new TableView<>(budgetData);
        budgetTable.setEditable(true);

        TableColumn<Budget, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        TableColumn<Budget, Double> amountCol = new TableColumn<>("Budgeted (Editable)");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        amountCol.setOnEditCommit(e -> {
            Budget budget = e.getRowValue();
            budget.setAmount(e.getNewValue() < 0 ? 0 : e.getNewValue());
            saveChanges();
            filterBudgets();
        });

        TableColumn<Budget, Double> spentCol = new TableColumn<>("Spent");
        spentCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        spentCol.setCellFactory(col -> createSpentCell());

        TableColumn<Budget, Double> remainingCol = new TableColumn<>("Remaining");
        remainingCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        remainingCol.setCellFactory(col -> createRemainingCell());

        TableColumn<Budget, Double> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        statusCol.setCellFactory(col -> createStatusCell());

        categoryCol.prefWidthProperty().bind(budgetTable.widthProperty().multiply(0.25));
        amountCol.prefWidthProperty().bind(budgetTable.widthProperty().multiply(0.15));
        spentCol.prefWidthProperty().bind(budgetTable.widthProperty().multiply(0.15));
        remainingCol.prefWidthProperty().bind(budgetTable.widthProperty().multiply(0.15));
        statusCol.prefWidthProperty().bind(budgetTable.widthProperty().multiply(0.30));

        budgetTable.getColumns().setAll(categoryCol, amountCol, spentCol, remainingCol, statusCol);
    }

    private void filterBudgets() {
        if (monthYearPicker.getValue() == null) return;
        YearMonth selectedPeriod = YearMonth.from(monthYearPicker.getValue());

        List<Budget> allBudgets = JsonUtil.loadBudgets();
        Map<String, Double> existingBudgets = allBudgets.stream()
                .filter(b -> b.getMonth().equals(selectedPeriod))
                .collect(Collectors.toMap(Budget::getCategoryName, Budget::getAmount));

        List<Category> allCategories = JsonUtil.loadCategories();
        List<Budget> displayBudgets = allCategories.stream()
                .map(cat -> new Budget(cat.getName(), existingBudgets.getOrDefault(cat.getName(), 0.0), selectedPeriod))
                .collect(Collectors.toList());
        budgetData.setAll(displayBudgets);
    }

    private void saveChanges() {
        YearMonth selectedPeriod = YearMonth.from(monthYearPicker.getValue());
        List<Budget> allBudgets = JsonUtil.loadBudgets();
        allBudgets.removeIf(b -> b.getMonth().equals(selectedPeriod));
        budgetData.stream().filter(b -> b.getAmount() > 0).forEach(allBudgets::add);
        JsonUtil.saveData(allBudgets, "budgets.json");
    }

    private double calculateSpentForCategory(String categoryName, YearMonth month) {
        return JsonUtil.loadTransactions().stream()
                .filter(t -> t.getType() == Transaction.Type.EXPENSE &&
                        t.getCategoryName().equals(categoryName) &&
                        YearMonth.from(t.getDate()).equals(month))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    private TableCell<Budget, Double> createSpentCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    Budget budget = getTableRow().getItem();
                    double spent = calculateSpentForCategory(budget.getCategoryName(), budget.getMonth());
                    setText(currencyFormatter.format(spent));
                }
            }
        };
    }

    private TableCell<Budget, Double> createRemainingCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    Budget budget = getTableRow().getItem();
                    double spent = calculateSpentForCategory(budget.getCategoryName(), budget.getMonth());
                    double remaining = budget.getAmount() - spent;
                    setText(currencyFormatter.format(remaining));
                    getStyleClass().removeAll("amount-expense-cell", "amount-income-cell");
                    if (remaining < 0) {
                        getStyleClass().add("amount-expense-cell");
                    } else {
                        getStyleClass().add("amount-income-cell");
                    }
                }
            }
        };
    }

    private TableCell<Budget, Double> createStatusCell() {
        return new TableCell<>() {
            private final ProgressBar progressBar = new ProgressBar();
            private final Label percentLabel = new Label();
            private final HBox container = new HBox(10, progressBar, percentLabel);

            {
                progressBar.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(progressBar, javafx.scene.layout.Priority.ALWAYS);
                container.setAlignment(Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Budget budget = getTableRow().getItem();
                    double spent = calculateSpentForCategory(budget.getCategoryName(), budget.getMonth());
                    double budgetAmount = budget.getAmount();

                    if (budgetAmount > 0) {
                        double progressRaw = spent / budgetAmount;
                        double progressBarValue = Math.min(progressRaw, 1.0);
                        progressBar.setProgress(progressBarValue);
                        percentLabel.setText(String.format("%.0f%%", Math.min(progressRaw, 1.0) * 100));
                        progressBar.getStyleClass().removeAll("progress-bar-safe", "progress-bar-warning", "progress-bar-over");
                        if (progressRaw > 0.7) {
                            progressBar.getStyleClass().add("progress-bar-over");
                        } else if (progressRaw > 0.4) {
                            progressBar.getStyleClass().add("progress-bar-warning");
                        } else {
                            progressBar.getStyleClass().add("progress-bar-safe");
                        }

                        setGraphic(container);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        };
    }
}