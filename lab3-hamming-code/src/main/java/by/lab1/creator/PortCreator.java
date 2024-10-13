package by.lab1.creator;

import by.lab1.event.PortReader;
import by.lab1.model.PortWithTextArea;
import javafx.scene.control.TextArea;
import jssc.SerialPort;
import jssc.SerialPortException;

import java.util.List;

public class PortCreator {
    private static TextArea logger;

    public static SerialPort createSerialPort(String portName) throws SerialPortException {
        SerialPort port = new SerialPort(portName);
        port.openPort();
        return port;
    }

    public static void setEventListener(PortWithTextArea receiver) {
        try {
            receiver.getSerialPort().addEventListener(new PortReader(receiver, receiver.getArea(), logger),
                    SerialPort.MASK_RXCHAR);
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
    }

    public static void setParams(List<PortWithTextArea> ports) {
        for (PortWithTextArea port : ports) {
            try {
                port.getSerialPort().setParams(
                        SerialPort.BAUDRATE_9600,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE
                );
            } catch (SerialPortException e) {
                logger.appendText("Error to set params in COM port ... ERROR");
            }
        }
    }

    public static void setLogger(TextArea logger) {
        PortCreator.logger = logger;
    }
}

