package by.lab1.event;

import by.lab1.model.PortWithTextArea;
import by.lab1.util.BitStuffingUtil;
import by.lab1.util.HammingCodeUtil;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import jssc.SerialPortException;
import jssc.SerialPortList;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

public class SendEvent {

    private final TextArea input;
    private final TextArea debug;
    private final PortWithTextArea port;
    private final Label counter;
    private final Label packetLabel;
    private final Label csmaCdLabel;

    private static final double CHANNEL_BUSY_PROBABILITY = 0.7;
    private static final double COLLISION_PROBABILITY = 0.3;
    private static final byte JAM_SIGNAL = '!';
    private static final int MAX_AMOUNT_OF_ATTEMPTS = 10;

    public SendEvent(TextArea input, TextArea debug,
                     PortWithTextArea port, Label counter,
                     Label packetLabel, Label casmaCdLabel) {
        this.input = input;
        this.debug = debug;
        this.port = port;
        this.counter = counter;
        this.packetLabel = packetLabel;
        this.csmaCdLabel = casmaCdLabel;
    }

    // <-- в отчет изменения с HammingCodeUtil -->
    public void sendEvent() throws Exception {
        try {
            if (isPortAvailable() && port.getSerialPort().isCTS()) {

                String text = input.getText();
                String dataText = text.substring(text.length() - 11);

                if (dataText.length() == 11) {

                    String packet = BitStuffingUtil.createPacket(dataText, port.getPortName());
                    String hammingCodePacket = HammingCodeUtil.encodeHamming(packet);
                    String packetAfterBitStuffing = BitStuffingUtil.bitStuffing(hammingCodePacket);
                    Platform.runLater(() -> addPacketString(packetAfterBitStuffing
                            .replace("\n", "\\n")));
                    sendBytesToPortWithCsmaCd(packetAfterBitStuffing.getBytes(StandardCharsets.UTF_8));
                    port.addBytes(dataText.length());
                    Platform.runLater(() -> counter.setText(String.valueOf(port.getSendBytes())));
                    port.setOldTextFromTextArea(text);
                }
            }

        } catch (SerialPortException e) {
            e.printStackTrace();
        }
    }

    private void sendBytesToPortWithCsmaCd(byte[] bytes) throws Exception {
        Random random = new Random();
        StringBuilder currentSendingStatus = new StringBuilder();
        for (byte b : bytes) {
            boolean sent = false;
            int attempt = 0;

            while (!sent) {

                double currentChannelBusyProbability = random.nextDouble();

                if (!(currentChannelBusyProbability < CHANNEL_BUSY_PROBABILITY)) {

                    double currentCollisionProbability = random.nextDouble();

                    if (currentCollisionProbability < COLLISION_PROBABILITY) {
                        port.getSerialPort().writeBytes(new byte[]{b});
                        port.getSerialPort().writeBytes(new byte[]{JAM_SIGNAL});
                        currentSendingStatus.append("!");
                        Platform.runLater(() -> csmaCdLabel.setText(currentSendingStatus.toString()));

                        if(attempt == MAX_AMOUNT_OF_ATTEMPTS) continue;

                        // 0 ≤ r ≤ 2^k, k = min(attemptCount, 10)
                        int delay = random.nextInt((int) Math.pow(2, Math.min(attempt, 10)) + 1);
                        Thread.sleep(delay * 100L);
                        attempt++;
                    } else {
                        port.getSerialPort().writeBytes(new byte[]{b});
                        currentSendingStatus.append(". ");
                        sent = true;

                        Platform.runLater(() -> csmaCdLabel.setText(currentSendingStatus.toString()));
                    }

                }
            }
        }
        port.getSerialPort().writeBytes(new byte[]{'\u0004'});
    }

    private void addPacketString(String packet) {

        StringBuilder stringBuilder = new StringBuilder(packet);
        stringBuilder.insert(packet.length() - 4, ' ');
        packet = stringBuilder.toString();
        TextFlow textFlow = new TextFlow();

        int startIndex = 0;
        String data = packet.substring(startIndex);

        int index = 0;
        while (index < data.length()) {
            int patternIndex = data.indexOf("1000101", index);
            if (patternIndex == -1) {
                textFlow.getChildren().add(new Text(data.substring(index)));
                break;
            }

            if (patternIndex > index) {
                textFlow.getChildren().add(new Text(data.substring(index, patternIndex)));
            }

            textFlow.getChildren().add(new Text("1000101"));

            if (patternIndex + 7 < data.length()) {
                char nextChar = data.charAt(patternIndex + 7);
                if (nextChar == '0') {
                    Text coloredText = new Text("0");
                    coloredText.setFill(Color.RED);
                    textFlow.getChildren().add(coloredText);
                } else {

                    textFlow.getChildren().add(new Text(String.valueOf(nextChar)));
                    System.out.println(index);
                }
            }


            index = patternIndex + 8;


            packetLabel.setGraphic(textFlow);
            packetLabel.setMinHeight(Region.USE_PREF_SIZE);

        }
    }

    private boolean isPortAvailable() {
        String[] portNames = SerialPortList.getPortNames();
        return Arrays.asList(portNames).contains(port.getPortName());
    }

}

