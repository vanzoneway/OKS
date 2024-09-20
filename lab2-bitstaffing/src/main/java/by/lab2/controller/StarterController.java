package by.lab2.controller;


import by.lab2.creator.PortCreator;
import by.lab2.event.SendEvent;
import by.lab2.model.PortWithTextArea;
import by.lab2.model.Speed;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import jssc.SerialPortException;
import jssc.SerialPortList;

import java.util.Arrays;


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
    private ComboBox<String> speed;

    @FXML
    private ComboBox<String> comSenderComboBox;

    @FXML
    private ComboBox<String> comReceiverComboBox;

    @FXML
    private Button configButton;

    @FXML
    void initialize() {
        setDisable();

        for (Speed var : Speed.values())
            speed.getItems().add(String.valueOf(var));


        speed.setValue("BAUDRATE_9600");

        String[] portNames = SerialPortList.getPortNames();
        Arrays.sort(portNames);
        for (String portName : portNames) {
            comSenderComboBox.getItems().add(portName);
            comReceiverComboBox.getItems().add(portName);
        }

        output.setOnKeyPressed( keyEvent -> {
            if (keyEvent.isShiftDown() && keyEvent.getCode() == KeyCode.DELETE) {
                output.clear();
            }
        });



        input.setOnKeyPressed(keyEvent -> {
            if (keyEvent.isShiftDown() && keyEvent.getCode() == KeyCode.ENTER) {
                input.appendText("\n");
            } else if (keyEvent.getCode() == KeyCode.ENTER) {

                new SendEvent(input, logger, writer).mouseClickedEvent();
            }
        });

        output.setOnKeyPressed(keyEvent -> {
            if (keyEvent.isShiftDown() && keyEvent.getCode() == KeyCode.ENTER) {
                output.appendText("\n");
            } else if (keyEvent.getCode() == KeyCode.ENTER) {
                new SendEvent(output, logger, receiver).mouseClickedEvent();
            }
        });


        speed.setOnAction(
                actionEvent -> PortCreator.setParams(
                        Arrays.asList(writer, receiver),
                        Speed.valueOf(speed.getValue()))
        );

        configButton.setOnAction(actionEvent -> {
            if (comSenderComboBox.getValue().equals(comReceiverComboBox.getValue())) {
                logger.appendText("Similar names of COM ports ... FAIL\n");
                setDisable();
            } else if (comSenderComboBox == null && comReceiverComboBox == null) {
                logger.appendText("Choose COM ports! ... FAIL\n");
            } else {
                setAvailable();
                try {
                    closeCurrentPortIfOpenedOrNull(writer);
                    closeCurrentPortIfOpenedOrNull(receiver);

                    writer = new PortWithTextArea(PortCreator.createSerialPort(comSenderComboBox.getValue()), input);
                    logger.appendText("\n" + writer.getPortName() + " opened successfully ... SUCCESS\n");

                    receiver = new PortWithTextArea(PortCreator.createSerialPort(comReceiverComboBox.getValue()), output);
                    logger.appendText("\n" + receiver.getPortName() + " opened successfully ... SUCCESS\n");
                    output.clear();

                    PortCreator.setLogger(logger);
                    PortCreator.setEventListener(receiver);
                    PortCreator.setEventListener(writer);

                    PortCreator.setParams(
                            Arrays.asList(writer, receiver),
                            Speed.valueOf(speed.getValue())
                    );

                } catch (SerialPortException e) {
                    logger.appendText("Unable to open COM Port. It may already be in use... ERROR\n");
                    setDisable();
                }

            }
        });


    }

    public void setDisable() {
        speed.setDisable(true);
        output.setDisable(true);
        input.setDisable(true);
    }

    public void setAvailable() {
        speed.setDisable(false);
        output.setDisable(false);
        input.setDisable(false);
    }

    private void closeCurrentPortIfOpenedOrNull(PortWithTextArea port) throws SerialPortException {
        if (port != null)
            if (port.getSerialPort().isOpened())
                port.getSerialPort().closePort();
    }


}

