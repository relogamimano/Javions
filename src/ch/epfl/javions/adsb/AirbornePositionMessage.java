package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;


import java.util.Objects;


public record AirbornePositionMessage(long timeStampNs, IcaoAddress icaoAddress, double altitude, int parity,
                                      double x, double y)  implements Message{
    private static final int COORD_SIZE = 17;
    private static final int FORMAT_SIZE = 1;
    private static final int TIME_SIZE = 1;
    private static final int ALT_SIZE = 12;
    static private final int LON_START = 0;
    private static final int LAT_START = LON_START + COORD_SIZE;
    private static final int FORMAT_START = LAT_START + COORD_SIZE;//                  (Q)
    private static final int TIME_START = FORMAT_START + FORMAT_SIZE;//                 |
    private static final int ALT_START = TIME_START + TIME_SIZE;//                      V
    private static final int Q_INDEX = 4;//         Alt :                 1 0 0 0 1 0 1 1 0 0 1 1
    private static final int[] INCORRECT_VALUES = new int[]{0, 5, 6};
    private static final int MAXIMUM_VALUE = 7;// maximum value (in base 10) we can represent with a 3 digit binary string (0b111 ~ 7)
    private static final int REPLACING_VALUE = 5;
    private static final int Q1_ALT_BASE = 1000;
    private static final int Q2_ALT_BASE = 1300;
    private static final int FOOT_FACTOR_25 = 25;
    private static final int FOOT_FACTOR_100 = 100;
    private static final int FOOT_FACTOR_500 = 500;

    public AirbornePositionMessage {
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument(timeStampNs >= 0
                && (parity == 0 || parity == 1)
                && (0 <= x && x < 1)
                && (0 <= y && y < 1)
        );
    }

    /**
     * Returns the in-flight positioning message corresponding to the given raw message,
     * or null if the altitude it contains is invalid.
     * @param rawMessage    Raw adsb-message
     * @return  Position message containing the coordinates and the altitude of the plane
     */
    public static AirbornePositionMessage of(RawMessage rawMessage) {
        int altitude = Bits.extractUInt(rawMessage.payload(),ALT_START, ALT_SIZE);
        byte altBit4 = (byte) Bits.extractUInt(altitude, Q_INDEX, 1);
        int format = Bits.extractUInt(rawMessage.payload(), FORMAT_START, FORMAT_SIZE);
        int cprLatitude = Bits.extractUInt(rawMessage.payload(), LAT_START, COORD_SIZE);
        int cprLongitude = Bits.extractUInt(rawMessage.payload(), LON_START, COORD_SIZE);
        double decodedAlt;
        if (altBit4 == 1) {

            decodedAlt = ( Bits.extractUInt(altitude, Q_INDEX + 1 , ALT_SIZE - Q_INDEX - 1) << Q_INDEX )
                    | (Bits.extractUInt(altitude, 0, Q_INDEX));
            decodedAlt = - Q1_ALT_BASE + FOOT_FACTOR_25 * decodedAlt;
        } else {
            //  Left bit set (C & A)  ||     Right bit set (B & D)
            //  C1 A1 C2 A2 C4 A4     ||     B1 D1 B2 D2 B4 D4
            byte CA = (byte) (altitude >>> ALT_SIZE/2 & 0x3F);
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
            int feetMultiple100 = grayDecoder(C);// multiple of 100 feet composed of the LSB  ( CCC )
            //  multiple of 500 feet composed of the MSB  ( DDD AAA BBB ),
            //  hence the shifting of the byte D by 6 (= 12 /2), and the shifting of the byte A by 3 (= 12 /2 /2 = 12/4)
            int feetMultiple500 = grayDecoder((D << 6) | (A << 3) | B);// multiple of 500 feet

            for (int i: INCORRECT_VALUES) {
                if (feetMultiple100 == i) return null;
            }

            //if LSB group (feetMultiple100) is equal to the maximum value ( 7 ), then the LSB group must be replaced by ( 5 )
            if (feetMultiple100 == MAXIMUM_VALUE) {
                feetMultiple100 = REPLACING_VALUE;
            }
            //if MSB group (feetMultiple500) is odd, then the LSB grp (feetMultiple100) is reflected.
            // i.e. replaced by 6 (= 7-1) minus its original value
            if (feetMultiple500 % 2 != 0) {
                feetMultiple100 = (MAXIMUM_VALUE - 1) - feetMultiple100;
            }

            decodedAlt = -Q2_ALT_BASE + feetMultiple100 * FOOT_FACTOR_100 + feetMultiple500 * FOOT_FACTOR_500;
        }
        decodedAlt = Units.convertFrom(decodedAlt, Units.Length.FOOT);

        double normalizedX = Math.scalb(cprLongitude, - COORD_SIZE);
        double normalizedY = Math.scalb(cprLatitude, - COORD_SIZE);

        return new AirbornePositionMessage(rawMessage.timeStampNs(),
                rawMessage.icaoAddress(), decodedAlt, format,
                normalizedX, normalizedY);
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
        Preconditions.checkArgument(b >>> ALT_SIZE/2 == 0b00);
        // { C1 A1 C2 A2 C4 A4 B1 D1 B2 D2 B4 D4 } ---> { D1 D2 D4 A1 A2 A4 B1 B2 B4 C1 C2 C4 }
        byte newByte = 0;
        newByte |= b        & 0b1;
        newByte |= b >>> 1  & 0b10;
        newByte |= b >>> 2  & 0b100;
        newByte |= b <<  2  & 0b1000;
        newByte |= b <<  1  & 0b10000;
        newByte |= b        & 0b100000;
        return newByte;
    }


    @Override
    public long timeStampNs() {return timeStampNs;}
    @Override
    public IcaoAddress icaoAddress(){
        return icaoAddress;
    }
}

