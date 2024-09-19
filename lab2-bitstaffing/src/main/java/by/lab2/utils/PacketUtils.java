package by.lab2.utils;

public class PacketUtils {
    public static final int DATA_LENGTH = 12;
    private static final byte FIRST_BYTE_FLAG = '$';
    private static final byte SECOND_BYTE_FLAG = (byte) ('a' + DATA_LENGTH - 1);
    private static final byte FCS = 0;

    private static final byte ESC = 0x7D;
    private static final byte ESC_FIRST_BYTE_FLAG = 0x5E;
    private static final byte ESC_SECOND_BYTE_FLAG = 0x5F;
    private static final byte ESC_ESC = 0x5D;


    //NOTE: After byte stuffing creates new byte[] >= DATA_LENGTH
    //NOTE: After byte destuffing creates new byte[] with similar size of array
    //NOTE: in createPacket method we copy array with data from index 4 to DATA_LENGTH index ( not include )
    //NOTE: after that we have a packet with LENGTH of 5 + DATA_LENGTH
    public static byte[] byteStuffing(byte[] data) {
        int count = 0;

        for (int i = 0; i < data.length; i++) {
            byte b = data[i];

            if (b == FIRST_BYTE_FLAG && i + 1 < data.length && data[i + 1] == SECOND_BYTE_FLAG) {
                count += 2;
                i++;
            } else if (b == ESC) {
                count++;
            }
        }

        byte[] stuffedData = new byte[data.length + count];

        int j = 0;

        for (int i = 0; i < data.length; i++) {
            byte b = data[i];

            if (b == FIRST_BYTE_FLAG && i + 1 < data.length && data[i + 1] == SECOND_BYTE_FLAG) {
                stuffedData[j++] = ESC;
                stuffedData[j++] = ESC_FIRST_BYTE_FLAG;
                stuffedData[j++] = ESC;
                stuffedData[j++] = ESC_SECOND_BYTE_FLAG;
                i++;
            } else if (b == ESC) {
                stuffedData[j++] = ESC;
                stuffedData[j++] = ESC_ESC;
            } else {
                stuffedData[j++] = b;
            }
        }
        return stuffedData;
    }

    public static byte[] byteDestuffing(byte[] data) {
        int count = 0;

        for (int i = 0; i < data.length; i++) {
            byte b = data[i];
            if (b == ESC) {
                count++;
                i++;
            }
        }

        byte[] destuffedData = new byte[data.length];

        int j = 0;

        for (int i = 0; i < data.length; i++) {
            byte b = data[i];

            if (b == ESC) {
                i++;
                if (i < data.length) {
                    byte nextByte = data[i];
                    if (nextByte == ESC_FIRST_BYTE_FLAG) {
                        destuffedData[j++] = FIRST_BYTE_FLAG;
                    } else if (nextByte == ESC_SECOND_BYTE_FLAG) {
                        destuffedData[j++] = SECOND_BYTE_FLAG;
                    } else if (nextByte == ESC_ESC) {
                        destuffedData[j++] = ESC;
                    }
                }
            } else {
                destuffedData[j++] = b;
            }
        }

        return destuffedData;
    }

    public static byte[] createPacket(byte sourceAddress, byte[] data) {
        if (data.length != DATA_LENGTH) {
            throw new IllegalArgumentException("Data length must be " + DATA_LENGTH + " bytes");
        }

        // Flags(2 bytes), Destination Address (1 byte), Source Address(1 byte), Data(n bytes), FCS(1 byte)
        byte[] packet = new byte[DATA_LENGTH + 5];

        // Flags
        packet[0] = FIRST_BYTE_FLAG;
        packet[1] = SECOND_BYTE_FLAG;

        // Destination Address
        packet[2] = 0x00;

        // Source Address
        packet[3] = sourceAddress;

        // Data
        System.arraycopy(data, 0, packet, 4, DATA_LENGTH);

        // FCS
        packet[DATA_LENGTH + 4] = FCS;

        return packet;
    }

    public static String hexDisplayOfPacketAsString(byte[] packet) {
        StringBuilder hexString = new StringBuilder();


        hexString.append(String.format("%02X ", packet[0]));
        hexString.append(String.format("%02X ", packet[1]));
        hexString.append(String.format("%02X ", packet[2]));
        hexString.append(String.format("%02X ", packet[3]));


        for (int i = 4; i < 4 + DATA_LENGTH; i++) {
            hexString.append(String.format("%02X ", packet[i]));
        }

        hexString.append(String.format("%02X", FCS));

        return hexString.toString().trim();
    }

    public static String createPacketInfo(byte[] packet) {

        byte[] data = byteDestuffing(getDataFromPacket(packet));
        byte[] packetBeforeStuffing = createPacket(packet[3], data);
        return String.format(
                """
                        -------------------------------------------------------------------------------
                        Before byte staffing: %s
                        After byte staffing: %s
                        ------------------------------------------------
                        Packet Information:
                        Flag: %s %s
                        Destination Address: %d
                        Source Address: %s
                        Data: %s
                        FCS: %d
                        ------------------------------------------------
                        -------------------------------------------------------------------------------
                        """,
                PacketUtils.hexDisplayOfPacketAsString(packetBeforeStuffing),
                PacketUtils.hexDisplayOfPacketAsString(packet),
                (char)packet[0],
                (char)packet[1],
                (int)packet[2],
                (char)packet[3],
                bytesToString(data),
                (int)packet[16]
        );
    }

    public static byte[] getDataFromPacket(byte[] packet) {
        byte[] data = new byte[DATA_LENGTH];
        System.arraycopy(packet, 4, data, 0, DATA_LENGTH);
        return data;
    }

    private static String bytesToString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            stringBuilder.append((char) b);
        }
        return stringBuilder.toString();
    }
}
