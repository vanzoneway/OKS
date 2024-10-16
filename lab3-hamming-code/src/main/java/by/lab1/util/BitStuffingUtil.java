package by.lab1.util;

public class BitStuffingUtil {
    private static final String FLAG = "10001011";
    private static final String DESTINATION_ADDRESS = "0000";
    private static final String SPACE = " ";


    public static String createPacket(String data,String comPortName) {
        String SOURCE_ADDRESS = toBinaryString(extractNumber(comPortName));
        return FLAG + DESTINATION_ADDRESS + SOURCE_ADDRESS + data;
    }

    public static String bitStuffing(String data) {
        StringBuilder stuffedData = new StringBuilder();
        String pattern = "1000101";
        int patternLength = pattern.length();

        stuffedData.append(data, 0, 8);

        for (int i = 8; i < data.length(); i++) {
            stuffedData.append(data.charAt(i));

            if (i >= patternLength + 7) {
                String substring = data.substring(i - patternLength + 1, i + 1);
                if (substring.equals(pattern)) {
                    stuffedData.append('0');
                }
            }
        }

        return stuffedData.toString();
    }

    public static String deBitStuffing(String data) {
        StringBuilder unstuffedData = new StringBuilder();
        String pattern = "1000101";
        int patternLength = pattern.length();

        unstuffedData.append(data, 0, 8);

        for (int i = 8; i < data.length(); i++) {
            unstuffedData.append(data.charAt(i));

            if (i >= patternLength + 7) {
                String substring = data.substring(i - patternLength + 1, i + 1);
                if (substring.equals(pattern) && i + 1 < data.length() && data.charAt(i + 1) == '0') {
                    i++;
                }
            }
        }

        return unstuffedData.toString();
    }

    // <-- изменяем packet.length() - 4, так FCS поле для моего варианта по итогу будет занимать 4 бита -->
    public static String extractDataFromPacket(String packet) {
        return packet.substring(16, packet.length() - 4);
    }

    private static int extractNumber(String comPort) {
        return Integer.parseInt(comPort.replaceAll("COM", ""));
    }

    private static String toBinaryString(int number) {
        return String.format("%4s", Integer.toBinaryString(number)).replace(' ', '0');
    }


}
