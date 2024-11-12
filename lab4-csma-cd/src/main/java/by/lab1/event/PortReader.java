package by.lab1.event;

import by.lab1.model.PortWithTextArea;
import by.lab1.util.BitStuffingUtil;
import by.lab1.util.HammingCodeUtil;
import javafx.scene.control.TextArea;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import java.nio.charset.StandardCharsets;


public class PortReader implements SerialPortEventListener {
    private final PortWithTextArea port;
    private final TextArea output;
    private final TextArea logger;

    private static final byte JAM_SIGNAL = '!';
    public static final StringBuilder dataAsStringBuilder = new StringBuilder();

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

                dataAsStringBuilder.append(new String(dataByteFormat, StandardCharsets.UTF_8));

                if (dataAsStringBuilder.charAt(dataAsStringBuilder.length() - 1) == '\u0004') {
                    String proceedReceivedData = processJamSignals(dataAsStringBuilder.toString());
                    String decodedHamming = HammingCodeUtil.
                            decodeHamming(HammingCodeUtil.modifyStringWithProbability(BitStuffingUtil
                                    .deBitStuffing(proceedReceivedData), 0.3), logger);
                    if (!proceedReceivedData.isEmpty()) {
                        output.appendText(BitStuffingUtil.extractDataFromPacket(decodedHamming));
                    }
                    dataAsStringBuilder.setLength(0);
                }
            } catch (Exception e) {
                logger.appendText(port.getPortName() + " lost this accepted packet ... WARNING\n");
                dataAsStringBuilder.setLength(0);
            }
        }
    }

    private String processJamSignals(String receivedData) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < receivedData.length() - 1; i++) {
            if (receivedData.charAt(i) == JAM_SIGNAL && i > 0) {
                result.deleteCharAt(result.length() - 1);
                continue;
            }
            result.append(receivedData.charAt(i));
        }
        return result.toString();
    }

}
