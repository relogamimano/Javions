package ch.epfl.javions;

import java.util.Objects;

/**
 * @author: Sofia Henriques Garfo (346298)
 * @author: Romeo Maignal (360568)
 */
public class Bits {

    private Bits() {}
    /**
     *
     * Public static methode which extracts from the 64-bit vector value the range of size bits starting at the index
     * bit start, which it interprets as an unsigned value
     * @throws IllegalArgumentException if the size is not strictly greater than 0 and strictly less than 32
     * @throws IndexOutOfBoundsException if the range described by start and size is not completely between 0 (inclusive)
     * and 64 (exclusive),
     *
     * @param value     (long)
     * @param start     (int)
     * @param size      (int)
     * @return          (int) Specific integer made out of selected bits from the 64bits value
     */
    public static int extractUInt(long value, int start, int size) {
        Preconditions.checkArgument(size>0 && size<Integer.SIZE);
        Objects.checkFromIndexSize(start,size,Long.SIZE);

        value = value >>> start;

        long mask = (1 << size) - 1;
        long valueFinal = value & mask;

        return (int) valueFinal;

    }

    /**
     * Test the value of a given bit
     * @throws IllegalArgumentException if the size is not strictly greater than 0 and strictly less than 64
     * @param value value that contains the bit to be tested (long)
     * @param index index of the bit in the value (int)
     * @return true if the bit is 1
     */
    public static boolean testBit(long value, int index) {
        Objects.checkIndex(index, Long.SIZE);
        return ((value >>> index) & 1) == 1;
    }

}
