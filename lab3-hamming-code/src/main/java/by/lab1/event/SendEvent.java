package by.lab1.event;

import by.lab1.model.PortWithTextArea;
import by.lab1.util.BitStuffingUtil;
import by.lab1.util.HammingCodeUtil;
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

public class SendEvent {
    private final TextArea input;
    private final TextArea debug;
    private final PortWithTextArea port;
    private final Label counter;
    private final Label packetLabel;


    public SendEvent(TextArea input, TextArea debug, PortWithTextArea port, Label counter, Label packetLabel) {
        this.input = input;
        this.debug = debug;
        this.port = port;
        this.counter = counter;
        this.packetLabel = packetLabel;
    }

    public void sendEvent() {
        try {
            if (isPortAvailable()) {

                String text = input.getText();
                String dataText = text.substring(text.length() - 11);
                if (dataText.length() == 11) {
                    String hammingDataText = HammingCodeUtil.encodeHamming(dataText);
                    if (port.getSerialPort().isCTS()) {
                        String packet = BitStuffingUtil.createPacket(hammingDataText, port.getPortName());
                        String packetAfterBitStuffing = BitStuffingUtil.bitStuffing(packet);
                        addPacketString(packetAfterBitStuffing.replace("\n", "\\n"));
                        port.getSerialPort().writeBytes(packetAfterBitStuffing.getBytes(StandardCharsets.UTF_8));
                        port.addBytes(hammingDataText.length());

                        counter.setText(String.valueOf(port.getSendBytes()));
                        port.setOldTextFromTextArea(text);
                    }

                }


            } else {
                debug.appendText("FAILURE!!! Unavailable send data to port!" + "\n");
            }
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
    }


    private void addPacketString(String packet) {

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
                }
            }


            index = patternIndex + 8;
        }

        packetLabel.setGraphic(textFlow);
        packetLabel.setMinHeight(Region.USE_PREF_SIZE);
    }


    private boolean isPortAvailable() {
        String[] portNames = SerialPortList.getPortNames();
        return Arrays.asList(portNames).contains(port.getPortName());
    }


}
