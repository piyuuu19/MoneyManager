package com.oop.moneymanager.controller;

import com.oop.moneymanager.model.Category;
import com.oop.moneymanager.util.JsonUtil;
import com.oop.moneymanager.util.ValidationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import java.util.List;
import java.util.Optional;

public class CategoryController {
    @FXML private VBox mainContainer;
    private final ObservableList<Category> categoryData = FXCollections.observableArrayList();

    @FXML public void initialize() {
        try {
            setupUI();
            loadData();
        } catch (Exception e) {
            e.printStackTrace();
            mainContainer.getChildren().add(new Label("Error memuat halaman kategori: " + e.getMessage()));
        }
    }

    private void setupUI() {
        mainContainer.setPadding(new Insets(25));
        mainContainer.setSpacing(20);
        mainContainer.getChildren().clear();

        Label title = new Label("Manage Categories");
        title.getStyleClass().add("content-title");

        TextField newCategoryField = new TextField();
        newCategoryField.setPromptText("Enter new category name");
        HBox.setHgrow(newCategoryField, Priority.ALWAYS);

        Button addButton = new Button("Add Category");
        addButton.getStyleClass().add("add-button");
        addButton.setOnAction(e -> {
            addCategory(newCategoryField.getText());
            newCategoryField.clear();
        });

        HBox addBox = new HBox(10, newCategoryField, addButton);
        addBox.setAlignment(Pos.CENTER_LEFT);

        TableView<Category> categoryTable = new TableView<>(categoryData);
        categoryTable.setEditable(true);
        VBox.setVgrow(categoryTable, Priority.ALWAYS);

        TableColumn<Category, String> nameCol = new TableColumn<>("Category Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(e -> {
            e.getRowValue().setName(e.getNewValue());
            saveChanges();
        });

        TableColumn<Category, Void> actionCol = new TableColumn<>("Action");
        actionCol.setSortable(false);
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button delBtn = new Button("", new FontIcon("mdi2d-delete"));
            {
                delBtn.getStyleClass().add("action-button-delete");
                delBtn.setOnAction(e -> deleteCategory(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(delBtn);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        nameCol.prefWidthProperty().bind(categoryTable.widthProperty().subtract(actionCol.widthProperty()).subtract(2));

        categoryTable.getColumns().setAll(nameCol, actionCol);
        mainContainer.getChildren().addAll(title, addBox, categoryTable);
    }

    private void loadData() {
        List<Category> loaded = JsonUtil.loadCategories();
        categoryData.setAll(loaded);
    }

    private void addCategory(String name) {
        if (name == null || name.trim().isEmpty()) {
            ValidationUtil.showAlert(Alert.AlertType.ERROR, "Input Error", "Category name cannot be empty.");
            return;
        }
        if (categoryData.stream().anyMatch(c -> c.getName().equalsIgnoreCase(name.trim()))) {
            ValidationUtil.showAlert(Alert.AlertType.WARNING, "Duplicate Category", "Category '" + name.trim() + "' already exists.");
            return;
        }
        categoryData.add(new Category(name.trim()));
        saveChanges();
    }

    private void deleteCategory(Category category) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure? This cannot be undone.", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Delete Category: " + category.getName());
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            categoryData.remove(category);
            saveChanges();
        }
    }

    private void saveChanges() {
        JsonUtil.saveData(categoryData, "categories.json");
    }
}