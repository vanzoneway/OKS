package by.lab2;

import by.lab2.initializer.Initializer;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        Initializer initializer = new Initializer();
        initializer.initScene(stage);
    }

    public static void main(String[] args) {
        launch(args);
        System.exit(0);
    }
}