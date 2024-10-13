package by.lab1.util;

import javafx.scene.control.TextArea;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HammingCodeUtil {

    public static String encodeHamming(String data) {
        StringBuilder cleanedInput = new StringBuilder();
        List<Integer> newlinePositions = new ArrayList<>();
        // Clean String from \n and remember it positions
        cleanStringFromEnter(data, cleanedInput, newlinePositions);
        data = cleanedInput.toString();
        int m = data.length();
        int r = 0;
        // Count amount of control bits
        while ((1 << r) < (m + r + 1)) {
            r++;
        }
        // Create array for coding with Hamming string
        char[] hammingCode = new char[m + r];
        // Fill the array and inserting control bits
        int j = 0, k = 0;
        for (int i = 0; i < hammingCode.length; i++) {
            // Если позиция - степень двойки, вставляем контрольный бит
            if (isPowerOfTwo(i + 1)) {
                hammingCode[i] = 'X'; // Место для контрольного бита
            } else {
                hammingCode[i] = data.charAt(k++);
            }
        }
        // Вычисляем контрольные биты
        for (int i = 0; i < r; i++) {
            int parityIndex = (1 << i);
            int parityValue = 0;
            for (int x = 1; x <= hammingCode.length; x++) {
                if ((x & parityIndex) == parityIndex) {
                    parityValue ^= hammingCode[x - 1] == '1' ? 1 : 0;
                }
            }

            hammingCode[parityIndex - 1] = (parityValue == 1) ? '1' : '0';
        }
        StringBuilder finalCode = new StringBuilder(new String(hammingCode));
        for (int pos : newlinePositions) {
            finalCode.insert(pos, '\n');
        }
        return finalCode.toString();
    }

    public static DecodedStringDto decodeHamming(String data, TextArea logger) {
        StringBuilder cleanedInput = new StringBuilder();
        List<Integer> newlinePositions = new ArrayList<>();

        logger.appendText("\nReceived data from packet: ");
        logger.appendText(data.replace("\n", "\\n"));
        // Очищаем строку, оставляя только 1 и 0, и запоминаем позиции \n
        cleanStringFromEnter(data, cleanedInput, newlinePositions);
        data = cleanedInput.toString();
        int r = 0;
        int m = data.length();
        // Определяем количество контрольных битов
        while ((1 << r) < m) {
            r++;
        }
        int errorPosition = 0;

        // Проверяем контрольные биты
        for (int i = 0; i < r; i++)  {
            int parityIndex = (1 << i);
            int parityValue = 0;
            for (int x = 1; x <= m; x++) {
                if ((x & parityIndex) == parityIndex) {
                    parityValue ^= data.charAt(x - 1) == '1' ? 1 : 0;
                }
            }
            if (parityValue != 0) {
                errorPosition += parityIndex;
            }
        }

        // Если есть ошибка, исправляем
        if (errorPosition != 0) {
            logger.appendText("\nError in position: " + errorPosition);
            char[] codeArray = data.toCharArray();
            codeArray[errorPosition - 1] = (codeArray[errorPosition - 1] == '1') ? '0' : '1'; // Исправляем ошибку
            data = new String(codeArray);
        }
        // Извлекаем данные (исключая контрольные биты)
        StringBuilder decodedData = new StringBuilder();
        for (int i = 0; i < m; i++) {
            if (!isPowerOfTwo(i + 1)) {
                decodedData.append(data.charAt(i));
            }
        }
        // Вставляем символы \n в исходные позиции
        for (int pos : newlinePositions) {
            decodedData.insert(pos, '\n');
        }

        return new DecodedStringDto(decodedData.toString(), errorPosition);
    }


    public static String modifyStringWithProbability(String input, double probability) {
        Random random = new Random();
        // Генерируем случайное число от 0 до 1
        if (random.nextDouble() < probability) {
            StringBuilder result = new StringBuilder(input);
            int indexToModify = random.nextInt(result.length()); // Выбираем случайный индекс

            char c = result.charAt(indexToModify);
            // Проверяем, является ли символ '0' или '1'
            if (c == '0' || c == '1') {
                // Меняем '0' на '1' или '1' на '0'
                result.setCharAt(indexToModify, (c == '0') ? '1' : '0');
            }
            return result.toString();
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
            if (i <= length) {
                controlBits.append(hammingCode.charAt(i - 1));
            }
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
