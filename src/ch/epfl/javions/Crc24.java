package ch.epfl.javions;
final public class Crc24 {
    private static final int N = 24;
    private static final int byteLength = 8;
    public static final int GENERATOR = 0xFFF409;
    int[] intTable;

    public Crc24(int generator) {
        this.intTable = builtTable(generator);
    }

    static private int[] builtTable(int generator) {
        int[] tab = new int[256];
        for (int i = 0; i < tab.length; i++) {
            tab[i] = crc_bitwise(generator, new byte[]{(byte)i});
        }
        return tab;
    }

    public int crc(byte[] bytes) {
        int crc = 0;

        for (byte b: bytes) {
            int j = Bits.extractUInt(crc, N-8,8);
            crc = ((crc << byteLength) | Byte.toUnsignedInt(b)) ^ intTable[j];
        }
        for (int i = 0; i < N/8; i++) {// erreur sur le nb d'iterations
            int j = Bits.extractUInt(crc, N-8, 8);
            crc = (crc << byteLength) ^ intTable[j];
        }
        return Bits.extractUInt(crc, 0, N-1); // erreur la mais jsp quoi
    }


    private static int crc_bitwise(int generator, byte[] message) {
        int[] table = new int[]{0, generator};
        int crc = 0;
        for (byte b : message) {
            for (int j = byteLength - 1; j >= 0; j--) {
                crc = ((crc << 1) | Bits.extractUInt(b, j, 1)) ^ table[Bits.extractUInt(crc, N - 1, 1)];
            }
        }
        for (int i = 0; i < N; i++) {
            crc = (crc << 1) ^ table[Bits.extractUInt(crc, N-1, 1)];
        }
        return Bits.extractUInt(crc,0,N);
    }
}
