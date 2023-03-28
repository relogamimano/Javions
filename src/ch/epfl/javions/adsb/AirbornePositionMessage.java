package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;


import java.util.Objects;

public record AirbornePositionMessage(long timeStampNs, IcaoAddress icaoAddress, double altitude, int parity, double x, double y) {
    public AirbornePositionMessage {
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument(timeStampNs >= 0
                && (parity == 0 || parity == 1)
                && (0 <= x && x < 1)
                && (0 <= y && y < 1)
        );
    }
    public static AirbornePositionMessage of(RawMessage rawMessage) {
        // TODO: 23.03.23 magic numbers

        int altitude = Bits.extractUInt(rawMessage.payload(),36, 12);
        byte altBit4 = (byte) Bits.extractUInt(altitude, 4, 1);
        int format = Bits.extractUInt(rawMessage.payload(), 34, 1);
        int cprLatitude = Bits.extractUInt(rawMessage.payload(), 17, 17);
        int cprLongitude = Bits.extractUInt(rawMessage.payload(), 0, 17);
        double decodedAlt;
        if (altBit4 == 1) {
            decodedAlt = ( (altitude & 0xFE0) >>> 1 ) | (altitude & 0xF);
            decodedAlt = - 1000 + 25 * decodedAlt;
        } else {
            //  Left bit set (C & A)  ||     Right bit set (B & D)
            //  C1 A1 C2 A2 C4 A4     ||     B1 D1 B2 D2 B4 D4
            byte CA = (byte) (altitude >>> 6 & 0x3F);
            byte BD = (byte) (altitude & 0x3F);
            //  CA bit set            ||     BD bit set
            //  C1 C2 C4 A1 A2 A4     ||     B1 B2 B4 D1 D2 D4
            byte CABitStr = permute(CA);
            byte BDBitStr = permute(BD);
            // D set     ||  A set     ||  B set     ||  C set
            // D1 D2 D4  ||  A1 A2 A4  ||  B1 B2 B4  ||  C1 C2 C4
            byte D = (byte) (BDBitStr & 0b111);
            byte A = (byte) (CABitStr & 0b111);
            byte B = (byte) (BDBitStr >>> 3 & 0b111);
            byte C = (byte) (CABitStr >>> 3 & 0b111);
            int feet100Multiple = grayDecoder(C);
            int feet500Multiple = grayDecoder((D << 6) | (A << 3) | B);
            if (feet100Multiple == 0 || feet100Multiple == 5 || feet100Multiple == 6) {
                return null;
            }
            if (feet100Multiple == 7) {
                feet100Multiple = 5;
            }
            if (feet500Multiple % 2 != 0) {
                feet100Multiple = 6 - feet100Multiple;
            }
            decodedAlt = -1300 + feet100Multiple * 100 + feet500Multiple * 500;
        }
        decodedAlt = Units.convertFrom(decodedAlt, Units.Length.FOOT);

        double normalizedX = Math.scalb(cprLatitude, - 17);
        double normalizedY = Math.scalb(cprLongitude, - 17);

        return new AirbornePositionMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), decodedAlt, format, normalizedX, normalizedY);
    }

    private static int grayDecoder(int encodedGray) {
        int decodedGray = encodedGray;
        while (encodedGray > 0) {
            encodedGray >>= 1;
            decodedGray ^= encodedGray;
        }
        return decodedGray;
    }

    private static byte permute(byte b) {
        Preconditions.checkArgument(b >>> 6 == 0b00);
        byte newByte = 0;
        newByte |= b        & 0b1;
        newByte |= b >>> 1  & 0b10;
        newByte |= b >>> 2  & 0b100;
        newByte |= b <<  2  & 0b1000;
        newByte |= b <<  1  & 0b10000;
        newByte |= b        & 0b100000;
        return newByte;
    }



}
