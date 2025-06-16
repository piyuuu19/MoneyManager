package com.oop.moneymanager.controller;

import com.oop.moneymanager.model.Budget;
import com.oop.moneymanager.model.Category;
import com.oop.moneymanager.model.Transaction;
import com.oop.moneymanager.util.JsonUtil;
import com.oop.moneymanager.util.ValidationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class TransactionController {
    @FXML private VBox mainContainer;
    private final ObservableList<Transaction> transactionData = FXCollections.observableArrayList();
    private TableView<Transaction> transactionTable;
    private ComboBox<Category> categoryCB;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM uuuu");

    @FXML public void initialize() {
        try {
            setupUI();
            loadData();
        } catch (Exception e) {
            e.printStackTrace();
            Label errorLabel = new Label("Terjadi error saat memuat halaman transaksi.\nPastikan file .json tidak rusak dan coba lagi.");
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
            mainContainer.setAlignment(Pos.CENTER);
            mainContainer.getChildren().add(errorLabel);
        }
    }

    private void setupUI() {
        mainContainer.setPadding(new Insets(25));
        mainContainer.setSpacing(20);
        mainContainer.getChildren().clear();

        Label titleLabel = new Label("Transactions");
        titleLabel.getStyleClass().add("content-title");

        GridPane addTransactionForm = createAddTransactionForm();

        setupTable();

        mainContainer.getChildren().addAll(titleLabel, addTransactionForm, transactionTable);
        VBox.setVgrow(transactionTable, Priority.ALWAYS);
    }

    private void loadData() {
        transactionData.setAll(JsonUtil.loadTransactions());

        List<Category> categories = JsonUtil.loadCategories();
        if (categories != null && !categories.isEmpty()) {
            categoryCB.setItems(FXCollections.observableArrayList(categories));
        }
    }

    private GridPane createAddTransactionForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(12);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setHgrow(Priority.ALWAYS); col2.setPercentWidth(28);
        ColumnConstraints col3 = new ColumnConstraints(); col3.setHgrow(Priority.ALWAYS); col3.setPercentWidth(22);
        ColumnConstraints col4 = new ColumnConstraints(); col4.setPercentWidth(14);
        ColumnConstraints col5 = new ColumnConstraints(); col5.setPercentWidth(14);
        ColumnConstraints col6 = new ColumnConstraints(); col6.setPercentWidth(10);
        grid.getColumnConstraints().addAll(col1, col2, col3, col4, col5, col6);

        grid.add(new Label("Type"), 0, 0);
        grid.add(new Label("Description"), 1, 0);
        grid.add(new Label("Category"), 2, 0);
        grid.add(new Label("Amount"), 3, 0);
        grid.add(new Label("Date"), 4, 0);

        ComboBox<Transaction.Type> typeCB = new ComboBox<>(FXCollections.observableArrayList(Transaction.Type.values()));
        typeCB.getSelectionModel().select(Transaction.Type.EXPENSE);
        typeCB.setMaxWidth(Double.MAX_VALUE);

        TextField descTF = new TextField();
        descTF.setPromptText("e.g., Makan siang");

        categoryCB = new ComboBox<>();
        categoryCB.setPromptText("Select or type");
        categoryCB.setEditable(true);
        categoryCB.setMaxWidth(Double.MAX_VALUE);

        TextField amountTF = new TextField();
        amountTF.setPromptText("e.g., 50000");

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setMaxWidth(Double.MAX_VALUE);

        Button addButton = new Button("Add");
        addButton.getStyleClass().add("add-button");
        addButton.setDefaultButton(true);
        addButton.setMaxWidth(Double.MAX_VALUE);

        addButton.setOnAction(e -> {
            try {
                double amount = Double.parseDouble(amountTF.getText());
                String categoryText = categoryCB.getEditor().getText();

                if (amount <= 0 || typeCB.getValue() == null || categoryText.isBlank() || descTF.getText().isBlank() || datePicker.getValue() == null) {
                    ValidationUtil.showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please fill all fields correctly.");
                    return;
                }

                Transaction newTransaction = new Transaction(typeCB.getValue(), amount, categoryText, descTF.getText(), datePicker.getValue());

                checkBudgetAndShowAlert(newTransaction);

                transactionData.add(newTransaction);
                saveChanges();

                descTF.clear();
                amountTF.clear();
                categoryCB.getEditor().clear();
                categoryCB.getSelectionModel().clearSelection();

            } catch (NumberFormatException ex) {
                ValidationUtil.showAlert(Alert.AlertType.ERROR, "Invalid Amount", "Please enter a valid number for amount.");
            } catch (Exception ex) {
                ValidationUtil.showAlert(Alert.AlertType.ERROR, "An Error Occurred", "Could not add transaction.");
                ex.printStackTrace();
            }
        });

        grid.add(typeCB, 0, 1);
        grid.add(descTF, 1, 1);
        grid.add(categoryCB, 2, 1);
        grid.add(amountTF, 3, 1);
        grid.add(datePicker, 4, 1);
        grid.add(addButton, 5, 1);

        return grid;
    }

    private void setupTable() {
        transactionTable = new TableView<>();
        SortedList<Transaction> sortedData = new SortedList<>(transactionData);
        sortedData.comparatorProperty().bind(transactionTable.comparatorProperty());
        transactionTable.setItems(sortedData);
        transactionTable.setPlaceholder(new Label("No content in table. Use the form above to add your first transaction."));

        transactionTable.setRowFactory(tv -> new TableRow<Transaction>() {
            @Override
            protected void updateItem(Transaction item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("income-row", "expense-row");
                if (item != null && !empty) {
                    if (item.getType() == Transaction.Type.INCOME) {
                        // getStyleClass().add("income-row");
                    } else {
                        // getStyleClass().add("expense-row");
                    }
                }
            }
        });

        TableColumn<Transaction, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : dateFormatter.format(item));
            }
        });

        TableColumn<Transaction, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Transaction, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        TableColumn<Transaction, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("amount-income-cell", "amount-expense-cell");
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    setText(currencyFormatter.format(item));
                    Transaction trx = (Transaction) getTableRow().getItem();
                    if (trx.getType() == Transaction.Type.INCOME) {
                        getStyleClass().add("amount-income-cell");
                    } else {
                        getStyleClass().add("amount-expense-cell");
                    }
                }
            }
        });
        amountCol.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<Transaction, Void> actionCol = createActionColumn();

        dateCol.prefWidthProperty().bind(transactionTable.widthProperty().multiply(0.15));
        descCol.prefWidthProperty().bind(transactionTable.widthProperty().multiply(0.35));
        catCol.prefWidthProperty().bind(transactionTable.widthProperty().multiply(0.19));
        amountCol.prefWidthProperty().bind(transactionTable.widthProperty().multiply(0.18));
        actionCol.prefWidthProperty().bind(transactionTable.widthProperty().multiply(0.12));

        transactionTable.getColumns().setAll(dateCol, descCol, catCol, amountCol, actionCol);
        transactionTable.getSortOrder().add(dateCol);
        dateCol.setSortType(TableColumn.SortType.DESCENDING);
    }

    private TableColumn<Transaction, Void> createActionColumn() {
        TableColumn<Transaction, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("", new FontIcon("mdi2p-pencil"));
            private final Button delBtn = new Button("", new FontIcon("mdi2d-delete"));
            private final HBox pane = new HBox(10, editBtn, delBtn);
            {
                pane.setAlignment(Pos.CENTER);
                editBtn.getStyleClass().add("action-button-edit");
                delBtn.getStyleClass().add("action-button-delete");
                editBtn.setOnAction(e -> showEditTransactionDialog(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> deleteTransaction(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        return actionCol;
    }

    private void showEditTransactionDialog(Transaction transaction) {
        Dialog<Transaction> dialog = new Dialog<>();
        dialog.setTitle("Edit Transaction");
        dialog.getDialogPane().getStyleClass().add("dialog-pane");
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20, 100, 10, 10));

        ComboBox<Transaction.Type> typeCB = new ComboBox<>(FXCollections.observableArrayList(Transaction.Type.values()));
        TextField amountTF = new TextField();
        ComboBox<Category> categoryCB = new ComboBox<>(FXCollections.observableArrayList(JsonUtil.loadCategories()));
        TextField descTF = new TextField();
        DatePicker datePicker = new DatePicker();

        typeCB.setValue(transaction.getType());
        amountTF.setText(String.valueOf(transaction.getAmount()));
        JsonUtil.loadCategories().stream().filter(c -> c.getName().equals(transaction.getCategoryName())).findFirst().ifPresent(categoryCB::setValue);
        descTF.setText(transaction.getDescription());
        datePicker.setValue(transaction.getDate());
        categoryCB.setEditable(true);

        grid.add(new Label("Type:"), 0, 0); grid.add(typeCB, 1, 0);
        grid.add(new Label("Amount:"), 0, 1); grid.add(amountTF, 1, 1);
        grid.add(new Label("Category:"), 0, 2); grid.add(categoryCB, 1, 2);
        grid.add(new Label("Description:"), 0, 3); grid.add(descTF, 1, 3);
        grid.add(new Label("Date:"), 0, 4); grid.add(datePicker, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn == saveButtonType) {
                try {
                    transaction.setType(typeCB.getValue());
                    transaction.setAmount(Double.parseDouble(amountTF.getText()));
                    transaction.setCategoryName(categoryCB.getEditor().getText());
                    transaction.setDescription(descTF.getText());
                    transaction.setDate(datePicker.getValue());
                    return transaction;
                } catch (Exception e) {
                    ValidationUtil.showAlert(Alert.AlertType.ERROR, "Update Failed", "Could not update transaction. Please check your inputs.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            transactionTable.refresh();
            saveChanges();
        });
    }

    private void deleteTransaction(Transaction transaction) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this transaction?", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText("Delete transaction: " + transaction.getDescription());
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                transactionData.remove(transaction);
                saveChanges();
            }
        });
    }

    private void saveChanges() {
        transactionData.sort(Comparator.comparing(Transaction::getDate).reversed());
        JsonUtil.saveData(transactionData, "transactions.json");
    }

    private void checkBudgetAndShowAlert(Transaction newTransaction) {
        if (newTransaction.getType() != Transaction.Type.EXPENSE) {
            return;
        }

        YearMonth currentMonth = YearMonth.from(newTransaction.getDate());
        String categoryName = newTransaction.getCategoryName();

        Optional<Budget> budgetOpt = JsonUtil.loadBudgets().stream()
                .filter(b -> b.getCategoryName().equals(categoryName) && b.getMonth().equals(currentMonth))
                .findFirst();

        if (budgetOpt.isEmpty() || budgetOpt.get().getAmount() == 0) {
            return;
        }

        double budgetAmount = budgetOpt.get().getAmount();

        double totalSpent = JsonUtil.loadTransactions().stream()
                .filter(t -> t.getType() == Transaction.Type.EXPENSE
                        && t.getCategoryName().equals(categoryName)
                        && YearMonth.from(t.getDate()).equals(currentMonth))
                .mapToDouble(Transaction::getAmount)
                .sum() + newTransaction.getAmount();

        if (totalSpent > budgetAmount) {
            ValidationUtil.showAlert(Alert.AlertType.WARNING,
                    "Budget Exceeded",
                    "You have exceeded the budget for '" + categoryName + "' this month.\n" +
                            "Budget: " + currencyFormatter.format(budgetAmount) + "\n" +
                            "Total Spent: " + currencyFormatter.format(totalSpent));
        }
    }
}