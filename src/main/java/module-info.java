module com.oop.moneymanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.materialdesign2;
    requires com.google.gson;

    opens com.oop.moneymanager to javafx.fxml;
    opens com.oop.moneymanager.controller to javafx.fxml;
    opens com.oop.moneymanager.model to com.google.gson, javafx.base;

    exports com.oop.moneymanager;
}