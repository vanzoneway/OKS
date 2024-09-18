package by.lab1.event;

import by.lab1.model.PortWithTextArea;
import javafx.scene.control.TextArea;
import jssc.SerialPortException;
import jssc.SerialPortList;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class SendEvent {
    private final TextArea input;
    private final TextArea debug;
    private final PortWithTextArea port;

    public SendEvent(TextArea input, TextArea debug, PortWithTextArea port) {
        this.input = input;
        this.debug = debug;
        this.port = port;
    }

    public void mouseClickedEvent() {
        try {
            if (isPortAvailable()) {
                byte[] message = (input.getText()).getBytes(StandardCharsets.UTF_8);
                if (message.length != 0) {
                    if (message[message.length - 1] == 10) {
                        message = Arrays.copyOf(message, message.length - 1);
                    }
                    port.getSerialPort().writeBytes(message);
                    input.clear();
                }
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
}
