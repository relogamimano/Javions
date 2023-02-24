package ch.epfl.javions;

import java.util.Arrays;
import java.util.HexFormat;

public final class ByteString {
    private final byte[] byteString;
    public ByteString(byte[] bytes) {
        this.byteString = bytes.clone();
    }
    public static ByteString ofHexadecimalString(String hexString) {
        if (hexString.length() % 2 == 1) {
            throw new IllegalArgumentException();
        }
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
    public int size() {return byteString.length;}
    public int byteAt(int index) {
        if (index >= byteString.length || index < 0) {
            throw new IndexOutOfBoundsException();
        }
        return Byte.toUnsignedInt(byteString[index]);
    }

    public long bytesInRange(int fromIndex, int toIndex) {
        if(fromIndex < 0 || toIndex < 0 || (toIndex > byteString.length)) {
            throw new IndexOutOfBoundsException();
        }
        if( toIndex - fromIndex >= 8 ) {
            throw new IllegalArgumentException();
        }
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
