package by.lab1.util;

import javafx.scene.control.TextArea;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HammingCodeUtil {

    public static String encodeHamming(String packet) {

        StringBuilder FCS = new StringBuilder();

        final int FLAG_LENGTH = 8;
        final int DESTINATION_ADDRESS_LENGTH = 4;
        final int SOURCE_ADDRESS_LENGTH = 4;


        String data = packet.substring(
                FLAG_LENGTH + DESTINATION_ADDRESS_LENGTH + SOURCE_ADDRESS_LENGTH
        );

        int m = data.length();
        int r = 0;

        // Count amount of control bits
        while ((1 << r) < (m + r + 1)) {
            r++;
        }

        // Вычисляем контрольные биты
        for (int i = 0; i < r; i++) {
            int parityIndex = (1 << i);
            int parityValue = 0;
            for (int x = 1; x <= data.length(); x++) {
                if ((x & parityIndex) == parityIndex) {
                    parityValue ^= data.charAt(x - 1) == '1' ? 1 : 0;
                }
            }
            FCS.append(parityValue);
        }
        return packet + FCS;
    }

    public static String decodeHamming(String packet, TextArea logger) {

        final int FLAG_LENGTH = 8;
        final int DESTINATION_ADDRESS_LENGTH = 4;
        final int SOURCE_ADDRESS_LENGTH = 4;


        String data = packet.substring(
                FLAG_LENGTH + DESTINATION_ADDRESS_LENGTH + SOURCE_ADDRESS_LENGTH,
                packet.length() - 4);

        String FCS = packet.substring(packet.length() - 4);


        logger.appendText("\nReceived data from packet: ");
        logger.appendText(data.replace("\n", "\\n"));
        // Очищаем строку, оставляя только 1 и 0, и запоминаем позиции \n

        data = data;
        int r = 0;
        int m = data.length();
        while ((1 << r) < m) {
            r++;
        }
        int errorPosition = 0;

        // Проверяем контрольные биты
        for (int i = 0; i < r; i++) {
            int parityIndex = (1 << i);
            int parityValue = 0;
            for (int x = 1; x <= m; x++) {
                if ((x & parityIndex) == parityIndex) {
                    parityValue ^= data.charAt(x - 1) == '1' ? 1 : 0;
                }
            }
            parityValue ^= FCS.charAt(i) - '0';
            if (parityValue != 0) {
                errorPosition += parityIndex;
            }
        }

        // Если есть ошибка, исправляем
        if (errorPosition != 0) {
            logger.appendText("\nError at index: " + (errorPosition - 1));
            char[] codeArray = data.toCharArray();
            codeArray[errorPosition - 1] = (codeArray[errorPosition - 1] == '1') ? '0' : '1'; // Исправляем ошибку
            data = new String(codeArray);
        }


        return packet.substring(0, FLAG_LENGTH + DESTINATION_ADDRESS_LENGTH + SOURCE_ADDRESS_LENGTH) + data + FCS;

    }


    public static String modifyStringWithProbability(String input, double probability) {
        Random random = new Random();
        // Генерируем случайное число от 0 до 1
        if (random.nextDouble() < probability) {
            StringBuilder result = new StringBuilder(input.substring(16, input.length()-4));
            int indexToModify = random.nextInt(result.length()); // Выбираем случайный индекс

            char c = result.charAt(indexToModify);
            // Проверяем, является ли символ '0' или '1'
            if (c == '0' || c == '1') {
                // Меняем '0' на '1' или '1' на '0'
                result.setCharAt(indexToModify, (c == '0') ? '1' : '0');
            }
            return input.substring(0, 16) + result + input.substring(input.length() - 4);
        }
        // Если вероятность не сработала, возвращаем исходную строку
        return input;
    }

    public static String extractControlBits(String hammingCode) {
        StringBuilder controlBits = new StringBuilder();

        // Определяем позиции контрольных бит
        int length = hammingCode.length();
        int i = 1;

        while (i <= length) {
            // Извлекаем контрольный бит из позиции 2^(k-1)
            controlBits.append(hammingCode.charAt(i - 1));
            i *= 2;
        }

        return controlBits.toString();
    }

    private static void cleanStringFromEnter(String data, StringBuilder cleanedInput, List<Integer> newlinePositions) {
        for (int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);
            if (c == '0' || c == '1') {
                cleanedInput.append(c);
            } else if (c == '\n') {
                newlinePositions.add(i); // Запоминаем позицию
            }
        }
    }

    private static boolean isPowerOfTwo(int n) {
        return (n & (n - 1)) == 0;
    }

}
