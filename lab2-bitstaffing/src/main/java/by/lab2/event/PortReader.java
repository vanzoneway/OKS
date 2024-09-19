package by.lab2.event;

import by.lab2.model.PortWithTextArea;
import by.lab2.utils.PacketUtils;
import javafx.scene.control.TextArea;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import java.nio.charset.StandardCharsets;

public class PortReader implements SerialPortEventListener {
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
                byte[] dataFromPacket = PacketUtils.getDataFromPacket(dataByteFormat);
                String outputDataBeforeDestuffing = new String(dataByteFormat, StandardCharsets.UTF_8);
                String outputDataAfterDestuffing = new String(PacketUtils.byteDestuffing(dataFromPacket), StandardCharsets.UTF_8);
                logger.appendText("\n"+ "[" + outputDataBeforeDestuffing.length() + "]" + " raw bytes received" + "\n");
                output.appendText(outputDataAfterDestuffing + "\n");
            } catch (SerialPortException e) {
                e.printStackTrace();
            }
        }
    }
}
