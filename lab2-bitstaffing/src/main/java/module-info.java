module lab1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires jssc;

    opens by.lab2 to javafx.fxml;
    opens by.lab2.controller to javafx.fxml;
    opens by.lab2.event to javafx.fxml;
    opens by.lab2.creator to javafx.fxml;
    opens by.lab2.initializer to javafx.fxml;
    opens by.lab2.model to javafx.fxml;
    opens by.lab2.utils to javafx.fxml;
    exports by.lab2;
    exports by.lab2.controller;
    exports by.lab2.event;
    exports by.lab2.creator;
    exports by.lab2.initializer;
    exports by.lab2.model;
    exports by.lab2.utils;
}