package by.lab1.controller;


import by.lab1.creator.PortCreator;
import by.lab1.event.SendEvent;
import by.lab1.model.PortWithTextArea;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jssc.SerialPortException;
import jssc.SerialPortList;

import java.util.Arrays;
import java.util.Objects;



public class StarterController {

    private PortWithTextArea writer;
    private PortWithTextArea receiver;

    @FXML
    private TextArea input;

    @FXML
    private TextArea output;

    @FXML
    private TextArea logger;

    @FXML
    private Button reset;

    @FXML
    private Label counter;

    @FXML
    private ComboBox<String> comSenderComboBox;

    @FXML
    private ComboBox<String> comReceiverComboBox;

    @FXML
    private Button configButton;

    @FXML
    void initialize() {
        setDisable();
        output.setEditable(false);

        String[] portNames = SerialPortList.getPortNames();
        Arrays.sort(portNames);
        for (String portName : portNames) {
            comSenderComboBox.getItems().add(portName);
            comReceiverComboBox.getItems().add(portName);
        }


        input.setOnKeyPressed(event -> {
            input.positionCaret(input.getText().length());
        });

        input.setOnKeyTyped(keyEvent -> {

            new SendEvent(input, logger, writer, counter).mouseClickedEvent();
        });

        output.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                output.clear();
            }
        });
        input.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                input.clear();
            }
        });


        reset.setOnAction(event -> {
            output.clear();
            input.clear();
            counter.setText("0");
            try {
                closeCurrentPortIfOpenedOrNull(writer);
                closeCurrentPortIfOpenedOrNull(receiver);
                setDisable();
                comSenderComboBox.setValue("");
                comReceiverComboBox.setValue("");
            } catch (SerialPortException e) {
                logger.appendText("Error to reset COM-ports ... ERROR");
            }
        });

        configButton.setOnAction(actionEvent -> {
            if (Objects.equals(comSenderComboBox.getValue(), comReceiverComboBox.getValue())) {
                displayErrorPopup("Error", "Similar names of COM ports ... FAIL");
                setDisable();
            } else if (comSenderComboBox == null && comReceiverComboBox == null) {
                displayErrorPopup("Error", "Choose COM ports! ... FAIL\n");
            } else {
                setAvailable();
                try {

                    output.clear();
                    input.clear();
                    counter.setText("0");
                    closeCurrentPortIfOpenedOrNull(writer);
                    closeCurrentPortIfOpenedOrNull(receiver);


                    writer = new PortWithTextArea(PortCreator.createSerialPort(comSenderComboBox.getValue()), input);
                    logger.appendText(writer.getPortName() + " opened successfully ... SUCCESS\n");

                    receiver = new PortWithTextArea(PortCreator.createSerialPort(comReceiverComboBox.getValue()), output);
                    logger.appendText(receiver.getPortName() + " opened successfully ... SUCCESS\n");


                    PortCreator.setLogger(logger);
                    PortCreator.setEventListener(receiver);

                    PortCreator.setParams(
                            Arrays.asList(writer, receiver)
                    );

                } catch (SerialPortException e) {
                    displayErrorPopup("Error", "Unable to open COM Port. It may already be in use... ERROR\n");
                    setDisable();
                }

            }
        });


    }

    public void setDisable() {
        input.setDisable(true);
    }

    public void setAvailable() {
        input.setDisable(false);
    }


    private void closeCurrentPortIfOpenedOrNull(PortWithTextArea port) throws SerialPortException {
        if (port != null)
            if (port.getSerialPort().isOpened())
                port.getSerialPort().closePort();
    }

    private void displayErrorPopup(String title, String message) {
        Stage popupStage = new Stage();

        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle(title);
        popupStage.setWidth(300);
        popupStage.setHeight(150);

        Label label = new Label(message);
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> popupStage.close());

        HBox buttonLayout = new HBox(10);
        buttonLayout.getChildren().add(closeButton);
        buttonLayout.setAlignment(Pos.CENTER);

        VBox layout = new VBox(10);
        layout.getChildren().addAll(label, buttonLayout);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }


}

