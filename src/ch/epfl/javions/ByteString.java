package ch.epfl.javions;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;
/** ByteString public and final class, represents a string of bytes.
 * @author Sofia Henriques Garfo (346298)
 * @author Romeo Maignal (360568)
 */
public final class ByteString {
    private final byte[] byteString;
    private static final HexFormat hf = HexFormat.of().withUpperCase();
    public ByteString(byte[] bytes) {
        this.byteString = bytes.clone();
    }
    /**
     *
     * Public static methode which returns the byte string whose hexadecimal is the representation of the string passed as argument.
     * @throws NumberFormatException if the given string is not of even length, or if it contains a character that is not a hexadecimal digit.
     *
     * @param hexString     (String)
     * @return              (ByteString)
     */
    public static ByteString ofHexadecimalString(String hexString) {
        Preconditions.checkArgument(hexString.length() % 2 == 0);
        byte[] bytes = hf.parseHex(hexString);
        return new ByteString(bytes);
    }

    /**
     * Return the size of the byteString
     * @return the size of the byteString
     */
    public int size() {
        return byteString.length;
    }

    /**
     * Getter method that return a specific byte at a given index
     * @param index given index
     * @return the specific byte at a given index
     */
    public int byteAt(int index) {
        return Byte.toUnsignedInt(byteString[index]);
    }

    /**
     * Getter methode that return a string of bytes (long) in a specific range in the byteString
     * @param fromIndex start of the range
     * @param toIndex   end og the range
     * @return the string of bytes (long)
     */
    public long bytesInRange(int fromIndex, int toIndex) {
        Objects.checkFromToIndex(fromIndex, toIndex, this.size());
        Preconditions.checkArgument(toIndex - fromIndex < Long.BYTES);
        long result = 0;
        for (var i = fromIndex; i < toIndex; i++) {
            result = result << 8;
            result += byteString[i] & 0xff;

        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof ByteString) && Arrays.equals(((ByteString) obj).byteString, this.byteString);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(byteString);
    }

    @Override
    public String toString() {
        return hf.formatHex(byteString);
    }
}
