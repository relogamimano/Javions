package ch.epfl.javions;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;

public final class ByteString {
    private final byte[] byteString;
    public ByteString(byte[] bytes) {
        this.byteString = bytes.clone();
    }
    public static ByteString ofHexadecimalString(String hexString) {
        Preconditions.checkArgument(hexString.length() % 2 == 0);
        for (int i = 0; i < hexString.length(); i++) {
            if(!HexFormat.isHexDigit(hexString.charAt(i))) {
                char ch = hexString.charAt(i);
                System.out.println(ch);
                throw new NumberFormatException();
            }
        }
        HexFormat hf = HexFormat.of().withUpperCase();
        byte[] bytes = hf.parseHex(hexString); // identique à bytes
        return new ByteString(bytes);
    }
    public int size() {
        return byteString.length;
    }
    public int byteAt(int index) {
        Objects.checkIndex(index, this.size());
        return Byte.toUnsignedInt(byteString[index]);
    }

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
        HexFormat hf = HexFormat.of().withUpperCase();
        return hf.formatHex(byteString);
    }
}
