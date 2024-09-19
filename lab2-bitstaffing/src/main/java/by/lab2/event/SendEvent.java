package by.lab2.event;

import by.lab2.model.PortWithTextArea;
import by.lab2.utils.PacketUtils;
import javafx.scene.control.TextArea;
import jssc.SerialPortException;
import jssc.SerialPortList;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//NOTE: always add at data field 0's if we have message.size less than DATA_LEN
public class SendEvent {
    private final TextArea input;
    private final TextArea debug;
    private final PortWithTextArea port;


    byte SOURCE_ADDRESS;


    public SendEvent(TextArea input, TextArea debug, PortWithTextArea port) {
        this.input = input;
        this.debug = debug;
        this.port = port;
    }


    public void mouseClickedEvent() {

        SOURCE_ADDRESS = port.getPortName().getBytes()[3];

        byte LENGTH_OF_DATA = PacketUtils.DATA_LENGTH;

        try {
            if (isPortAvailable()) {

                byte[] message = (input.getText()).getBytes(StandardCharsets.UTF_8);
                List<Byte> messageList = new ArrayList<>();
                byte[] stuffedMessage = PacketUtils.byteStuffing(message);
                for (byte b : stuffedMessage) {
                    messageList.add(b);
                }

                byte[] data = new byte[LENGTH_OF_DATA];
                byte[] specialData = new byte[LENGTH_OF_DATA];
                while (!messageList.isEmpty()) {
                    if (messageList.size() >= LENGTH_OF_DATA) {

                        for (int i = 0; i < LENGTH_OF_DATA; i++) {
                            data[i] = (messageList.get(i));
                        }
                        if (data[data.length - 1] == 10)
                            data[data.length - 1] = 0;

                        //handling if 7D 5E combination and throw $l to the next packet
                        if (data[LENGTH_OF_DATA - 3] == 0x7D && data[LENGTH_OF_DATA - 2] == 0x5E) {

                            handleSpecial7D_5ECombination(LENGTH_OF_DATA - 3, specialData, data, messageList);

                            //handling if 7D 5E -> throw them to next packet
                        } else if (data[LENGTH_OF_DATA - 2] == 0x7D && data[LENGTH_OF_DATA - 1] == 0x5E) {

                            handleSpecial7D_5ECombination(LENGTH_OF_DATA - 2, specialData, data, messageList);

                            //handling 7D on the end -> throw them on the next package
                        } else if (data[LENGTH_OF_DATA - 1] == 0x7D) {

                            handleSpecial7D_5ECombination(LENGTH_OF_DATA - 1, specialData, data, messageList);

                        } else {
                            byte[] packet = PacketUtils.createPacket(SOURCE_ADDRESS, data);
                            port.getSerialPort().writeBytes(packet);

                            messageList.subList(0, LENGTH_OF_DATA).clear();
                            String formattedPacketInfo = formatPacketInfo(PacketUtils.createPacketInfo(packet));
                            debug.appendText(formattedPacketInfo);
                        }

                    } else {

                        int n = messageList.size() % LENGTH_OF_DATA;

                        if (messageList.getFirst() == 10)
                            break;

                        for (int i = 0; i < n; i++) {
                            data[i] = (messageList.get(i));
                        }
                        for (int i = n - 1; i < LENGTH_OF_DATA; i++) {
                            data[i] = 0;
                        }

                        byte[] packet = PacketUtils.createPacket(SOURCE_ADDRESS, data);
                        port.getSerialPort().writeBytes(packet);

                        messageList.subList(0, n).clear();
                        String formattedPacketInfo = formatPacketInfo(PacketUtils.createPacketInfo(packet));
                        debug.appendText(formattedPacketInfo);

                    }
                }
                input.clear();


            } else {
                debug.appendText("FAILURE!!! Unavailable send data to port!" + "\n");
            }
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
    }

    private boolean isPortAvailable() {
        String[] portNames = SerialPortList.getPortNames();
        return Arrays.asList(portNames).contains(port.getPortName());
    }

    public String formatPacketInfo(String input) {
        String[] lines = input.split("\n");
        StringBuilder formattedOutput = new StringBuilder();

        for (String line : lines) {
            if (line.startsWith("Before byte staffing:") || line.startsWith("After byte staffing:")) {
                String[] parts = line.split(": ");
                String bytesPart = parts[1];

                String formattedBytes = processBytes(bytesPart);
                formattedOutput.append(parts[0]).append(": ").append(formattedBytes).append("\n");
            } else {
                formattedOutput.append(line).append("\n");
            }
        }
        return formattedOutput.toString().trim();
    }

    private String processBytes(String bytes) {
        String[] tokens = bytes.split(" ");
        StringBuilder processedBytes = new StringBuilder();

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];

            if (i < tokens.length - 1 && token.equals("24") && tokens[i + 1].equals("6C")) {
                processedBytes.append("[").append(token).append(" ").append(tokens[i + 1]).append("] ");
                i++;
                continue;
            }

            if (i < tokens.length - 3 && token.equals("7D") && tokens[i + 1].equals("5E") &&
                    tokens[i + 2].equals("7D") && tokens[i + 3].equals("5F")) {
                processedBytes.append("[").append(token).append(" ").append(tokens[i + 1]).append(" ")
                        .append(tokens[i + 2]).append(" ").append(tokens[i + 3]).append("] ");
                i += 3;
                continue;
            }

            if (token.equals("7D") || token.equals("5D")) {
                processedBytes.append("[").append(token).append("] ");
                continue;
            }

            processedBytes.append(token).append(" ");
        }

        return processedBytes.toString().trim();
    }

    private void handleSpecial7D_5ECombination(int index,
                                               byte[] specialData,
                                               byte[] data,
                                               List<Byte> messageList) throws SerialPortException {
        System.arraycopy(data, 0, specialData, 0, index);
        for (int i = index; i < data.length; i++) {
            specialData[i] = 0;
        }
        byte[] packet = PacketUtils.createPacket(SOURCE_ADDRESS, specialData);
        port.getSerialPort().writeBytes(packet);
        messageList.subList(0, index).clear();
        String formattedPacketInfo = formatPacketInfo(PacketUtils.createPacketInfo(packet));
        debug.appendText(formattedPacketInfo);

    }

}
