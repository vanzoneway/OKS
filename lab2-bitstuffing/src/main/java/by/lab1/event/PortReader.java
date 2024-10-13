package by.lab1.event;

import by.lab1.model.PortWithTextArea;
import by.lab1.util.BitStuffingUtil;
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
                String data = BitStuffingUtil.extractDataFromPacket(BitStuffingUtil.deBitStuffing(receivedData));
                if (!receivedData.isEmpty()) {
                    output.appendText(data);
                }
            } catch (SerialPortException e) {
                e.printStackTrace();
            }
        }
    }
}
