package com.oop.moneymanager.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import java.util.Objects;

public class DashboardController {
    @FXML private StackPane contentArea;
    @FXML private Button btnTransactions;
    @FXML private Button btnCategories;
    @FXML private Button btnBudget;
    @FXML private Button btnReport;

    private Button currentButton;

    @FXML public void initialize() {
        // Atur tampilan default saat aplikasi pertama kali dibuka
        currentButton = btnTransactions;
        currentButton.getStyleClass().add("active");
        loadView("transaction_view.fxml");
    }

    @FXML private void handleMenuClick(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();

        if (clickedButton == currentButton) return;

        if (currentButton != null) {
            currentButton.getStyleClass().remove("active");
        }

        clickedButton.getStyleClass().add("active");
        currentButton = clickedButton;

        String viewName = "";
        // Menggunakan switch pada ID tombol untuk identifikasi yang pasti
        switch (clickedButton.getId()) {
            case "btnTransactions":
                viewName = "transaction_view.fxml";
                break;
            case "btnCategories":
                viewName = "category_view.fxml";
                break;
            case "btnBudget":
                viewName = "budget_view.fxml";
                break;
            case "btnReport":
                viewName = "report_view.fxml";
                break;
            default:
                System.out.println("Tombol tidak dikenali: " + clickedButton.getId());
                break;
        }

        if (!viewName.isEmpty()) {
            loadView(viewName);
        }
    }

    private void loadView(String fxmlFile) {
        try {
            String resourcePath = "/com/oop/moneymanager/view/" + fxmlFile;
            Node view = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(resourcePath)));
            contentArea.getChildren().setAll(view);
        } catch (IOException | NullPointerException e) {
            System.err.println("Gagal memuat view: " + fxmlFile);
            e.printStackTrace();
            Label errorLabel = new Label("Gagal memuat tampilan: " + fxmlFile);
            contentArea.getChildren().setAll(errorLabel);
        }
    }
}