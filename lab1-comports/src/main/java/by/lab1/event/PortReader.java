package by.lab1.event;

import by.lab1.model.PortWithTextArea;
import javafx.scene.control.TextArea;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import java.nio.charset.StandardCharsets;


public class  PortReader implements SerialPortEventListener {
    private final PortWithTextArea port;
    private final TextArea output;
    private final TextArea logger;


    public PortReader(PortWithTextArea port, TextArea output, TextArea logger) {
        this.port = port;
        this.output = output;
        this.logger = logger;
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        if (serialPortEvent.isRXCHAR() && serialPortEvent.getEventValue() > 0) {
            try {
                byte[] dataByteFormat = port.getSerialPort().readBytes(serialPortEvent.getEventValue());

                String receivedData = new String(dataByteFormat, StandardCharsets.UTF_8);

                if (!receivedData.isEmpty()) {
                    char lastChar = receivedData.charAt(receivedData.length() - 1);
                    output.appendText(String.valueOf(lastChar));
                }
            } catch (SerialPortException e) {
                e.printStackTrace();
            }
        }
    }
}
