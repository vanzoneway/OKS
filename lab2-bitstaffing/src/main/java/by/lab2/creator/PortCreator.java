package by.lab2.creator;

import by.lab2.event.PortReader;
import by.lab2.model.PortWithTextArea;
import by.lab2.model.Speed;
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

    public static void setParams(List<PortWithTextArea> ports, Speed speed) {
        for (PortWithTextArea port : ports) {
            try {
                port.getSerialPort().setParams(
                        speed.getBaudRate(),
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE
                );
            } catch (SerialPortException e) {
                e.printStackTrace();
            }
        }
    }

    public static void setLogger(TextArea logger) {
        PortCreator.logger = logger;
    }
}

