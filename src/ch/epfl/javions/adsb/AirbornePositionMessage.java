package ch.epfl.javions.adsb;

import ch.epfl.javions.Preconditions;
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
    public AirbornePositionMessage of(RawMessage rawMessage) {
        // TODO: 23.03.23 magic numbers
        int altitude = (int) (rawMessage.payload() >>> 36 & 0xFFF);
        byte altBit4 = (byte) (altitude >>> 5 & 0x1);
        int format = (int) (rawMessage.payload() >>> 34 & 0x1);
        int cprLatitude = (int) (rawMessage.payload() >>> 17 & 0x1FFFF);
        int cprLongitude = (int) (rawMessage.payload() & 0x1FFFF);
        int decodedAlt;
        if (altBit4 == 1) {
            decodedAlt = ( (altitude & 0xFE0) >>> 1 ) | (altitude & 0xF);
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


        int x = (int) Math.scalb((double)cprLatitude, - 17);
        int y = (int) Math.scalb((double)cprLongitude, - 17);

        return new AirbornePositionMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), decodedAlt, format, x, y);
    }

    private int grayDecoder(int encodedGray) {
        int decodedGray = encodedGray;
        while (encodedGray > 0) {
            encodedGray >>= 1;
            decodedGray ^= encodedGray;
        }
        return decodedGray;
    }

    private byte permute(byte b) {
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
