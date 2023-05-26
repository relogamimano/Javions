package ch.epfl.javions;

import static ch.epfl.javions.Bits.extractUInt;

/**
 * Crc24 class, public, final and immutable, represents a 24-bit CRC calculator.
 * * @author Sofia Henriques Garfo (346298)
 * * @author Romeo Maignal (360568)
 */
final public class Crc24 {
    private static final int N = 24;
    private static final int BYTE_CAPACITY = 1 << Byte.SIZE;
    /**
     * GENERATOR which contains the 24 least significant bits of the generator used to calculate the CRC24 of ADS-B messages
     */
    public static final int GENERATOR = 0xFFF409;
    final private int[] intTable;


    /**
     * Return a CRC24 calculator using the generator of which the 24 LSB are the ones from the generator parameter
     * @param generator generator
     */

    public Crc24(int generator) {
        this.intTable = builtTable(generator);
    }

    static private int[] builtTable(int generator) {
        int[] tab = new int[BYTE_CAPACITY];
        for (int i = 0; i < tab.length; i++) {
            tab[i] = crc_bitwise(generator, new byte[]{(byte)i});
        }
        return tab;
    }

    /**
     * Return the CRC24 of the given table
     * @param bytes table parameter
     * @return the CRC24 of the given table
     */
    public int crc(byte[] bytes) {
        int crc = 0;
        for (byte b: bytes) {
            crc = ((crc << Byte.SIZE) | Byte.toUnsignedInt(b))
                    ^ intTable[extractUInt(crc, N-Byte.SIZE,Byte.SIZE)];
        }
        for (int i = 0; i < N/Byte.SIZE; i++) {
            crc = (crc << Byte.SIZE) ^ intTable[extractUInt(crc, N- Byte.SIZE, Byte.SIZE)];
        }
        return extractUInt(crc, 0, N);
    }


    private static int crc_bitwise(int generator, byte[] message) {
        int[] table = new int[]{0, generator};
        int crc = 0;
        for (byte b : message) {
            for (int j = Byte.SIZE - 1; j >= 0; j--) {
                crc = ((crc << 1) | extractUInt(b, j, 1)) ^ table[extractUInt(crc, N - 1, 1)];
            }
        }
        for (int i = 0; i < N; i++) {
            crc = (crc << 1) ^ table[extractUInt(crc, N-1, 1)];
        }
        return extractUInt(crc,0,N);
    }
}
