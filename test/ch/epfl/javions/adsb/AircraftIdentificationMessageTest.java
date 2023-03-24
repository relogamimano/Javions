package ch.epfl.javions.adsb;

import ch.epfl.javions.aircraft.IcaoAddress;
import org.junit.jupiter.api.Test;

import java.sql.SQLOutput;
import java.util.BitSet;
import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AircraftIdentificationMessageTest {
    @Test
    void AircraftIdentificationMessageConstructorWorksWithTrivialValues() {
        assertThrows(IllegalArgumentException.class, () -> new AircraftIdentificationMessage(-1, new IcaoAddress("3E9A0B"), 163, new CallSign("HBPRO")));
        assertThrows(NullPointerException.class, () -> new AircraftIdentificationMessage(1, null, 163, new CallSign("HAPCD")));
        assertThrows(NullPointerException.class, () -> new AircraftIdentificationMessage(2, new IcaoAddress("RYR7JD"), 163, null));
    }


    @Test
    void onTestTasVu() {
        BitSet b = BitSet.valueOf(new byte[] {0b010101});
        System.out.println(permute1((byte) 0b010101));
        System.out.println(grayDecoder((int)0b110, 3));

    }
    private int grayDecoder(int encodedGray, int length) {
        // TODO: 24.03.23 should i use entropy
        int decodedGray = encodedGray;
        while (encodedGray > 0) {
            encodedGray >>= 1;
            decodedGray ^= encodedGray;
        }
        return decodedGray;
    }
    byte permute1(byte b) {
        byte newByte = 0;
        newByte |= b        & 0b1;
        newByte |= b >>> 1  & 0b10;
        newByte |= b >>> 2  & 0b100;
        newByte |= b <<  2  & 0b1000;
        newByte |= b <<  1  & 0b10000;
        newByte |= b        & 0b100000;
        return newByte;
    }

    BitSet permute2(BitSet bitSet) {
        BitSet bSet = new BitSet(6);
        bSet.set(0, bitSet.get(0));
        bSet.set(1, bitSet.get(2));
        bSet.set(2, bitSet.get(4));
        bSet.set(3, bitSet.get(1));
        bSet.set(4, bitSet.get(3));
        bSet.set(5, bitSet.get(5));

        return bSet;
    }
}
