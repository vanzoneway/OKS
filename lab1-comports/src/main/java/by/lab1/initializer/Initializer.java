package by.lab1.initializer;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Initializer {
    private static final String RESOURCE = "/main.fxml";
    private static final String TITLE = "COM-ports-lab1";

    public void initScene(Stage stage) throws IOException {
        URL url = getClass().getResource(RESOURCE);
        Parent root = FXMLLoader.load(url);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle(TITLE);
        stage.show();
    }
}