package ch.epfl.javions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ByteStringTest {
    @Test
    void byteStringOfHexadecimalStringThrowsIfStringHasOddSize() {
        assertThrows(IllegalArgumentException.class, () -> ByteString.ofHexadecimalString("1"));
    }

    @Test
    void byteStringOfHexadecimalStringThrowsIfStringContainsInvalidCharacters() {
        assertThrows(NumberFormatException.class, () -> ByteString.ofHexadecimalString("1G"));
    }

    @Test
    void byteStringOfHexadecimalStringCorrectlyParsesString() {
        var byteString = ByteString.ofHexadecimalString("0123456776543210");
        var expectedBytes = new byte[]{0x01, 0x23, 0x45, 0x67, 0x76, 0x54, 0x32, 0x10};
        assertEquals(8, byteString.size());
        for (var i = 0; i < expectedBytes.length; i++)
            assertEquals(expectedBytes[i], byteString.byteAt(i));
    }

    @Test
    void byteStringConstructorCopiesGivenArray() {
        var bytes = new byte[]{0x12, 0x34, 0x56, 0x78, (byte) 0x9A, (byte) 0xBC, (byte) 0xDE};
        var byteString = new ByteString(bytes);
        bytes[0] = 0x00;
        assertEquals(0x12, byteString.byteAt(0));
    }

    @Test
    void byteStringSizeReturnsByteCount() {
        var bytes = new byte[]{0x12, 0x34, 0x56, 0x78, (byte) 0x9A, (byte) 0xBC, (byte) 0xDE};
        var byteString = new ByteString(bytes);
        assertEquals(bytes.length, byteString.size());

        var byteString2 = ByteString.ofHexadecimalString("0123456789ABCDEF");
        assertEquals(8, byteString2.size());
    }

    @Test
    void byteStringByteAtThrowsWithInvalidIndex() {
        byte[] bytes = new byte[0];
        var byteString = new ByteString(bytes);
        assertThrows(IndexOutOfBoundsException.class, () -> byteString.byteAt(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> byteString.byteAt(0));
    }

    @Test
    void byteStringByteAtReturnsUnsignedBytes() {
        var bytesCount = 1 << Byte.SIZE;

        var allBytes = new byte[bytesCount];
        for (var i = 0; i < allBytes.length; i++)
            allBytes[i] = (byte) i;

        var byteString = new ByteString(allBytes);
        for (var i = 0; i < allBytes.length; i++)
            assertEquals(i, byteString.byteAt(i));
    }

    @Test
    void byteStringBytesInRangeReadsBytesInCorrectOrder() {
        var bytes = new byte[]{0x12, 0x34, 0x56, 0x78, (byte) 0x9A, (byte) 0xBC, (byte) 0xDE};
        var byteString = new ByteString(bytes);
        assertEquals(0x123456789ABCDEL, byteString.bytesInRange(0, bytes.length));
    }

    @Test
    void byteStringEqualsDoesStructuralComparison() {
        var bs1 = ByteString.ofHexadecimalString("0123456789ABCDEF");
        var bs2 = ByteString.ofHexadecimalString("0123456789ABCDEF");
        assertEquals(bs1, bs2);
    }

    @Test
    void byteStringHashCodeDoesStructuralHashing() {
        var bs1 = ByteString.ofHexadecimalString("0123456789ABCDEF");
        var bs2 = ByteString.ofHexadecimalString("0123456789ABCDEF");
        var bs3 = ByteString.ofHexadecimalString("1111");
        assertEquals(bs1.hashCode(), bs2.hashCode());
        assertNotEquals(bs1.hashCode(), bs3.hashCode());
    }

    @Test
    void byteStringToStringUsesUppercaseHexadecimalCharacters() {
        var bs = ByteString.ofHexadecimalString("0123456789abcdef");
        assertEquals("0123456789ABCDEF", bs.toString());
    }
}