package by.lab1.event;

import by.lab1.model.PortWithTextArea;
import javafx.scene.control.TextArea;
import jssc.SerialPortException;
import jssc.SerialPortList;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SendEvent {
    private final TextArea input;
    private final TextArea debug;
    private final PortWithTextArea port;

    private final byte[] MODIFIED_FLAG = {'$', ('a' + 11)};


    public SendEvent(TextArea input, TextArea debug, PortWithTextArea port) {
        this.input = input;
        this.debug = debug;
        this.port = port;
    }

    public void mouseClickedEvent() {

        byte[] packetInfo = new byte[6];
        packetInfo[0] = MODIFIED_FLAG[0];
        packetInfo[1] = MODIFIED_FLAG[1];

        byte DESTINATION_ADDRESS = 0;
        packetInfo[2] = DESTINATION_ADDRESS;

        byte SOURCE_ADDRESS = port.getPortName().getBytes()[3];

        packetInfo[3] = SOURCE_ADDRESS;

        byte LENGTH_OF_DATA = 12;
        packetInfo[4] = LENGTH_OF_DATA;

        byte FCS = 0;
        packetInfo[5] = FCS;


        try {
            if (isPortAvailable()) {
                //converting bytes into byteList BEGIN
                byte[] message = (input.getText()).getBytes(StandardCharsets.UTF_8);
                List<Byte> messageList = new ArrayList<>();

                for (byte b : message) {
                    messageList.add(b);
                }
                //converting bytes into byteList END
                byte[] data = new byte[LENGTH_OF_DATA];
                while(!messageList.isEmpty()) {
                    if (messageList.size() >= LENGTH_OF_DATA) {

                        for (int i = 0; i < LENGTH_OF_DATA; i++) {
                            data[i] = (messageList.get(i));
                        }
                        if (data[data.length - 1] == 10)
                            data[data.length - 1] = 0;
                        //
                        port.getSerialPort().writeBytes(data);
                        messageList.subList(0, LENGTH_OF_DATA).clear();
                        debug.appendText(createPacketInfo(
                                packetInfo[0],
                                packetInfo[1],
                                packetInfo[2],
                                packetInfo[3],
                                data,
                                packetInfo[5]
                        ));

                    } else {

                        int n = messageList.size() % LENGTH_OF_DATA;

                        if ( messageList.getFirst() == 10)
                            break;

                        for (int i = 0; i < n; i++) {
                            data[i] = (messageList.get(i));
                        }
                        for (int i = n - 1; i < LENGTH_OF_DATA; i++) {
                            data[i] = 0;
                        }
                        port.getSerialPort().writeBytes(data);
                        messageList.subList(0, n).clear();
                        debug.appendText(createPacketInfo(
                                packetInfo[0],
                                packetInfo[1],
                                packetInfo[2],
                                packetInfo[3],
                                data,
                                packetInfo[5]
                        ));
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

    private String createPacketInfo(byte firstByteFlag,
                                    byte secondByteFlag,
                                    byte destAddr,
                                    byte sourceAddr,
                                    byte[] data,
                                    byte FCS) {
        String hexPacket = createHexPacket(firstByteFlag, secondByteFlag, destAddr, sourceAddr, data, FCS);
        return String.format(
                """
                        -------------------------------------------------------------------------------
                        Before byte staffing: %s
                        After byte staffing: %s
                        ------------------------------------------------
                        Packet Information:
                        Flag: %s %s
                        Destination Address: %02X
                        Source Address: %02X
                        Data: %s
                        FCS: %02X
                        ------------------------------------------------
                        -------------------------------------------------------------------------------
                        """,
                hexPacket,
                hexPacket,
                (char)firstByteFlag,
                (char)secondByteFlag,
                destAddr,
                sourceAddr,
                bytesToString(data),
                FCS
        );
    }

    private String bytesToString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            stringBuilder.append((char) b);
        }
        return stringBuilder.toString();
    }

    private String createHexPacket(byte firstByteFlag,
                                   byte secondByteFlag,
                                   byte destAddr,
                                   byte sourceAddr,
                                   byte[] data,
                                   byte FCS) {
        StringBuilder hexString = new StringBuilder();


        hexString.append(String.format("%02X ", firstByteFlag));
        hexString.append(String.format("%02X ", secondByteFlag));
        hexString.append(String.format("%02X ", destAddr));
        hexString.append(String.format("%02X ", sourceAddr));


        for (byte b : data) {
            hexString.append(String.format("%02X ", b));
        }


        hexString.append(String.format("%02X", FCS));

        return hexString.toString().trim();
    }
}
