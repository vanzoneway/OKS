package by.lab1.controller;


import by.lab1.creator.PortCreator;
import by.lab1.event.PortReader;
import by.lab1.event.SendEvent;
import by.lab1.model.PortWithTextArea;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jssc.SerialPortException;
import jssc.SerialPortList;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class StarterController {

    private final ReentrantLock lock = new ReentrantLock();


    private PortWithTextArea writer;
    private PortWithTextArea receiver;

    private String previousSenderValue = "-";
    private String previousReceiverValue = "-";

    private String previousDeletedReceiverValue;
    private String previousDeletedSenderValue;

    private boolean flag = false;
    private boolean isClearing = false;

    private Integer inputCounter = 0;

    private final ConcurrentLinkedQueue<SendEvent> sendQueue = new ConcurrentLinkedQueue<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    @FXML
    private TextArea input;

    @FXML
    private TextArea output;

    @FXML
    private TextArea logger;

    @FXML
    private Label counter;

    @FXML
    private Label packetLabel;

    @FXML
    private ComboBox<String> comSenderComboBox;

    @FXML
    private ComboBox<String> comReceiverComboBox;

    @FXML
    public Label csmaCdLabel;


    @FXML
    void initialize() {
        output.setEditable(false);

        prepareSenderAndReceiverComboBoxes();
        prepareInputTextArea();

        comSenderComboBox.setOnAction(event -> {
            isClearing = true;
            if (!flag) {
                String currentSenderValue = comSenderComboBox.getValue();
                flag = true;
                if (currentSenderValue == null) {
                    flag = false;
                    sendQueue.clear();
                    PortReader.dataAsStringBuilder.setLength(0);
                    return;
                }

                if (Objects.equals(previousSenderValue, currentSenderValue)) {
                    flag = false;
                    sendQueue.clear();
                    PortReader.dataAsStringBuilder.setLength(0);
                    return;
                } else if (Objects.equals(currentSenderValue, "-")) {

                    try {
                        closeCurrentPortIfOpenedOrNull(writer);
                    } catch (SerialPortException e) {
                        logger.appendText("Unable to close " + writer.getSerialPort().getPortName());
                    }

                    Optional.ofNullable(previousDeletedReceiverValue).ifPresent(x -> {
                        comReceiverComboBox.getItems().add(x);

                        sortComPorts(comReceiverComboBox.getItems());
                        previousDeletedReceiverValue = null;
                        input.setDisable(true);
                    });

                    input.clear();
                    counter.setText("0");
                    flag = false;
                    PortReader.dataAsStringBuilder.setLength(0);
                    sendQueue.clear();
                    previousSenderValue = currentSenderValue;
                    inputCounter = 0;
                } else {
                    try {
                        input.setDisable(false);

                        closeCurrentPortIfOpenedOrNull(writer);
                        writer = new PortWithTextArea(PortCreator.createSerialPort(currentSenderValue), input);
                        logger.appendText(writer.getPortName() + " opened successfully ... SUCCESS\n");

                        Optional.ofNullable(previousDeletedReceiverValue).ifPresent(x -> {
                            comReceiverComboBox.getItems().add(x);
                            sortComPorts(comReceiverComboBox.getItems());
                            previousDeletedReceiverValue = null;
                        });

                        input.clear();
                        counter.setText("0");


                        previousDeletedReceiverValue = "COM" + (extractNumber(currentSenderValue) + 1);
                        comReceiverComboBox.getItems().remove(previousDeletedReceiverValue);
                        sortComPorts(comReceiverComboBox.getItems());


                        PortCreator.setLogger(logger);
                        PortCreator.setParams(Collections.singletonList(writer));
                        previousSenderValue = currentSenderValue;
                        inputCounter = 0;

                    } catch (SerialPortException e) {

                        comSenderComboBox.setValue(previousSenderValue);

                        if (previousSenderValue.equals("-")) {
                            input.setDisable(true);
                        }

                        if (previousSenderValue != null && !previousSenderValue.equals("-")) {
                            try {
                                writer.getSerialPort().openPort();
                            } catch (SerialPortException ex) {
                                throw new RuntimeException(ex);
                            }

                            PortCreator.setLogger(logger);
                            PortCreator.setParams(Collections.singletonList(writer));

                        }

                        displayErrorPopup("Error", """
                                Unable to open COM-ports.
                                One of them used in other application!
                                """, input.getParent().getParent());

                    }

                }

                input.requestFocus();
                flag = false;
                PortReader.dataAsStringBuilder.setLength(0);
                sendQueue.clear();
                startCtsMonitoring();
            }
            isClearing = false;
        });


        comReceiverComboBox.setOnAction(event -> {
            if (!flag) {
                String currentReceiverValue = comReceiverComboBox.getValue();
                flag = true;

                if (currentReceiverValue == null) {
                    flag = false;
                    PortReader.dataAsStringBuilder.setLength(0);
                    sendQueue.clear();
                    return;
                }


                if (Objects.equals(previousReceiverValue, currentReceiverValue)) {
                    flag = false;
                    PortReader.dataAsStringBuilder.setLength(0);
                    sendQueue.clear();
                    return;
                } else if (Objects.equals(currentReceiverValue, "-")) {
                    try {
                        closeCurrentPortIfOpenedOrNull(receiver);
                    } catch (SerialPortException e) {
                        logger.appendText("Unable to close " + receiver.getSerialPort().getPortName());
                    }
                    Optional.ofNullable(previousDeletedSenderValue).ifPresent(x -> {
                        comSenderComboBox.getItems().add(x);
                        sortComPorts(comSenderComboBox.getItems());
                        previousDeletedSenderValue = null;
                        flag = false;
                        PortReader.dataAsStringBuilder.setLength(0);
                        sendQueue.clear();
                    });

                    output.clear();
                    previousReceiverValue = currentReceiverValue;

                } else {
                    try {

                        closeCurrentPortIfOpenedOrNull(receiver);
                        receiver = new PortWithTextArea(PortCreator.createSerialPort(currentReceiverValue), output);
                        logger.appendText(receiver.getPortName() + " opened successfully ... SUCCESS\n");

                        Optional.ofNullable(previousDeletedSenderValue).ifPresent(x -> {
                            comSenderComboBox.getItems().add(x);
                            sortComPorts(comSenderComboBox.getItems());
                            previousDeletedSenderValue = null;
                        });

                        output.clear();


                        previousDeletedSenderValue = "COM" + (extractNumber(currentReceiverValue) - 1);
                        comSenderComboBox.getItems().remove(previousDeletedSenderValue);
                        sortComPorts(comSenderComboBox.getItems());


                        PortCreator.setLogger(logger);
                        PortCreator.setParams(Collections.singletonList(receiver));
                        PortCreator.setEventListener(receiver);

                        previousReceiverValue = currentReceiverValue;

                    } catch (SerialPortException e) {

                        comReceiverComboBox.setValue(previousReceiverValue);

                        if (previousReceiverValue != null && !previousReceiverValue.equals("-")) {
                            try {
                                receiver.getSerialPort().openPort();
                            } catch (SerialPortException ex) {
                                throw new RuntimeException(ex);
                            }
                            PortCreator.setParams(Collections.singletonList(receiver));
                            PortCreator.setLogger(logger);
                        }
                        displayErrorPopup("Error", """
                                Unable to open COM-ports.
                                One of them used in other application!
                                """, input.getParent().getParent());

                        flag = false;
                        sendQueue.clear();
                        PortReader.dataAsStringBuilder.setLength(0);
                    }
                }


                input.requestFocus();
                flag = false;
                PortReader.dataAsStringBuilder.setLength(0);
                sendQueue.clear();
            }

        });
    }


    private void prepareInputTextArea() {


        input.setDisable(true);

        input.setOnKeyPressed(event -> {
            input.positionCaret(input.getText().length());
        });

        input.setOnKeyTyped(keyEvent -> {
            if (Objects.equals(keyEvent.getCharacter(), "1") || Objects.equals(keyEvent.getCharacter(), "0")
                    || Objects.equals(keyEvent.getCharacter(), "\r")) {
                inputCounter++;
            }

            if (inputCounter == 11) {
                SendEvent sendEvent = new SendEvent(logger, writer, counter, packetLabel, csmaCdLabel, input.getText());
                sendQueue.add(sendEvent);
                inputCounter = 0;
                if (sendQueue.size() == 1) {
                    processNextSendEvent();
                }
            }
        });

        TextFormatter<String> inputFormatter = new TextFormatter<>(
                change -> {
                    if (isClearing) {
                        return change;
                    }

                    if (change.isDeleted() && change.getText().length() == 1) {
                        return change;
                    }

                    if (!change.getText().isEmpty() &&
                            !change.getText().equals("1") &&
                            !change.getText().equals("0") &&
                            !change.getText().equals("\n")) {
                        return null;
                    }

                    if (!change.getText().isEmpty()) {
                        return change;
                    }

                    return null;
                }
        );

        input.setTextFormatter(inputFormatter);

    }

    private void processNextSendEvent() {
        if (!sendQueue.isEmpty()) {

            SendEvent sendEvent = sendQueue.poll();

            executorService.submit(() -> {
                try {
                    sendEvent.sendEvent();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (!sendQueue.isEmpty()) {
                    processNextSendEvent();
                }
            });

        }
    }

    private void prepareSenderAndReceiverComboBoxes() {
        String[] portNames = SerialPortList.getPortNames();
        List<String> portNamesList = Arrays.stream(portNames)
                .filter(port -> extractNumber(port) <= 16)
                .collect(Collectors.toList());
        sortComPorts(portNamesList);
        portNames = portNamesList.toArray(new String[0]);


        String[] portSenderNames = new String[portNames.length / 2];
        String[] portReceiverNames = new String[portNames.length / 2];

        for (int i = 0, j = 0; i < portNames.length - 1; j++, i += 2) {
            if (extractNumber(portNames[i]) == extractNumber(portNames[i + 1]) - 1) {
                portSenderNames[j] = portNames[i];
            }
        }
        for (int i = 0, j = 0; i < portNames.length - 1; j++, i += 2) {
            if (extractNumber(portNames[i]) == extractNumber(portNames[i + 1]) - 1) {
                portReceiverNames[j] = portNames[i + 1];
            }
        }

        comSenderComboBox.getItems().add("-");
        comReceiverComboBox.getItems().add("-");

        comSenderComboBox.getItems().addAll(portSenderNames);
        comReceiverComboBox.getItems().addAll(portReceiverNames);

        comSenderComboBox.getSelectionModel().select(0);
        comReceiverComboBox.getSelectionModel().select(0);
    }

    public void sortComPorts(List<String> comPorts) {

        comPorts.sort((port1, port2) -> {
            if (port1.equals("-")) return -1;
            if (port2.equals("-")) return 1;
            int num1 = Integer.parseInt(port1.replaceAll("COM", ""));
            int num2 = Integer.parseInt(port2.replaceAll("COM", ""));
            return Integer.compare(num1, num2);
        });
    }

    private void closeCurrentPortIfOpenedOrNull(PortWithTextArea port) throws SerialPortException {

        if (port != null)
            if (port.getSerialPort().isOpened()) {
                port.getSerialPort().setRTS(false);
                port.getSerialPort().closePort();
            }

    }

    private void displayErrorPopup(String title, String message, Node owner) {
        Stage popupStage = new Stage();

        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle(title);
        popupStage.setWidth(400);
        popupStage.setHeight(200);
        popupStage.setResizable(false);

        Label label = new Label(message);
        label.setFont(Font.font("System", 14));
        label.setWrapText(true); // Enable text wrapping

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> popupStage.close());
        closeButton.setStyle("-fx-background-color: #e59cf5; -fx-text-fill: white;"); // Custom button style

        VBox layout = new VBox(15, label, closeButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout);
        popupStage.setScene(scene);

        // Center the popup relative to the owner node
        popupStage.initOwner(owner.getScene().getWindow());
        popupStage.setOnShown(e -> {
            Stage ownerStage = (Stage) owner.getScene().getWindow();
            popupStage.setX(ownerStage.getX() + (ownerStage.getWidth() - popupStage.getWidth()) / 2);
            popupStage.setY(ownerStage.getY() + (ownerStage.getHeight() - popupStage.getHeight()) / 2);
        });

        popupStage.showAndWait();
    }


    private int extractNumber(String str) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(str);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }

        return -1;
    }


    public void startCtsMonitoring() {
        Thread ctsMonitorThread = new Thread(() -> {
            try {
                if (writer != null && writer.getSerialPort() != null) {
                    if (!writer.getSerialPort().isCTS()) {
                        Platform.runLater(() ->
                                displayErrorPopup("Warning", "You need to turn on the receiver at the other station.",
                                        input.getParent().getParent()));
                    }
                }
                while (writer != null && writer.getSerialPort() != null) {

                    if (!writer.getSerialPort().isCTS()) {

                        Platform.runLater(() -> input.setDisable(true));
                    } else {

                        Platform.runLater(() -> input.setDisable(false));
                    }

                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {

            } catch (SerialPortException e) {
                throw new RuntimeException(e);
            }
        });

        ctsMonitorThread.setDaemon(true);
        ctsMonitorThread.start();
    }


}


