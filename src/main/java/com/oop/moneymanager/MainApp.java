package com.oop.moneymanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("view/dashboard.fxml")));
            Scene scene = new Scene(root, 1280, 720);

            primaryStage.setTitle("Money Manager");

            InputStream iconStream = getClass().getResourceAsStream("view/icon/logoin.png");
            if (iconStream != null) {
                primaryStage.getIcons().add(new Image(iconStream));
            } else {
                System.out.println("Peringatan: File ikon 'logo.png' tidak ditemukan.");
            }

            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1024);
            primaryStage.setMinHeight(680);
            primaryStage.show();

        } catch (IOException | NullPointerException e) {
            System.err.println("Gagal memuat resource utama. Pastikan file FXML dan ikon ada di path yang benar.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
