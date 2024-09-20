package by.lab1.event;

import by.lab1.model.PortWithTextArea;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import jssc.SerialPortException;
import jssc.SerialPortList;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class SendEvent {
    private final TextArea input;
    private final TextArea debug;
    private final PortWithTextArea port;
    private final Label counter;


    public SendEvent(TextArea input, TextArea debug, PortWithTextArea port, Label counter) {
        this.input = input;
        this.debug = debug;
        this.port = port;
        this.counter = counter;
    }

    public void mouseClickedEvent() {
        try {
            if (isPortAvailable()) {

                String text = input.getText();
                if (text.length() > port.getOldTextFromTextArea().length()) {
                    String lastCharString = text.substring(text.length() - 1); // Получаем последний символ как строку
                    byte[] lastCharBytes = lastCharString.getBytes(StandardCharsets.UTF_8); // Преобразуем в байты

                    port.getSerialPort().writeBytes(lastCharBytes);

                    port.addBytes(lastCharBytes.length);

                    counter.setText(String.valueOf(port.getSendBytes()));
                    port.setOldTextFromTextArea(text);
                } else {
                    port.setOldTextFromTextArea(text);
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
