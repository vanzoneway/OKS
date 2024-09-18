module lab1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires jssc;

    opens by.lab1 to javafx.fxml;
    opens by.lab1.controller to javafx.fxml;
    opens by.lab1.event to javafx.fxml;
    opens by.lab1.creator to javafx.fxml;
    opens by.lab1.initializer to javafx.fxml;
    opens by.lab1.model to javafx.fxml;
    exports by.lab1;
    exports by.lab1.controller;
    exports by.lab1.event;
    exports by.lab1.creator;
    exports by.lab1.initializer;
    exports by.lab1.model;
}