package ch.epfl.javions;

import java.util.Objects;

public class Bits {
    private Bits() {}

    public static int extractUInt(long value, int start, int size) {

        if ((size <= 0) || (size >= 32)) {
            throw new IllegalArgumentException();
        }
        Objects.checkFromIndexSize(start,size,Long.SIZE);

        value = value >>> start;

        long mask = (1 << size) - 1;
        long valueFinal = value & mask;

        return (int) valueFinal;

    }

    public static boolean testBit(long value, int index) {
        Objects.checkIndex(index, Long.SIZE);
        return ((value >>> index) & 1) == 1;
    }

}
