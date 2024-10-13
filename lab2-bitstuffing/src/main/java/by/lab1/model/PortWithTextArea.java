package by.lab1.model;

import jssc.SerialPort;
import javafx.scene.control.TextArea;



public class PortWithTextArea {
    private final SerialPort serialPort;
    private final TextArea area;
    private int sendBytes;
    private String oldTextFromTextArea = "";

    public PortWithTextArea(SerialPort serialPort, TextArea area) {
        this.serialPort = serialPort;
        this.area = area;
    }

//    00000011111 --> 000000111110 --> 011111001111100
    public SerialPort getSerialPort() {
        return serialPort;
    }
    public String getPortName(){
        return serialPort.getPortName();
    }
    public TextArea getArea() {
        return area;
    }
    public int getSendBytes() {
        return sendBytes;
    }
    public void addBytes(int amount) {
        sendBytes += amount;
    }

    public void setOldTextFromTextArea(String oldTextFromTextArea) {
        this.oldTextFromTextArea = oldTextFromTextArea;
    }
    public String getOldTextFromTextArea() {
        return oldTextFromTextArea;
    }
}
