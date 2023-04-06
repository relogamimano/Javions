package ch.epfl.javions.adsb;

import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

import static ch.epfl.javions.Bits.extractUInt;

/**
 * ABDS-Messages that contain information on the aircraft's airborne position
 * @param timeStampNs time stamp
 * @param icaoAddress ICAO address
 * @param altitude plain altitude in meters
 * @param parity parity of the message ( 0 even, 1 odd)
 * @param x local normalised longitude
 * @param y local normalised latitude
 * @author: Sofia Henriques Garfo (346298)
 * @author: Romeo Maignal (360568)
 */
public record AirbornePositionMessage(long timeStampNs, IcaoAddress icaoAddress, double altitude, int parity,
                                               double x, double y)  implements Message{
    private static final int COORD_SIZE = 17;
    private static final int FORMAT_SIZE = 1;
    private static final int TIME_SIZE = 1;
    private static final int ALT_SIZE = 12;
    private static final int ABCD_TRIO_SIZE = 3;// len( A A A ) --> 3
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

    /**
     * AirbornePositionMessage's compact constructor that verifies that all preconditions are met.
     *
     * @param timeStampNs Message's time stamp
     * @param icaoAddress Airplane's ICAO address
     * @param altitude Airplane's altitude
     * @param parity Message's parity ( 0 odd, 1 even)
     * @param x Airplane's normalized longitude
     * @param y Airplane's normalized latitude
     * @throws IllegalArgumentException if time same are negative, parity isn't valid, or x and y aren't greater
     * than 0 (included or strictly smaller than (1)
     * @throws
     */
    public AirbornePositionMessage {
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument(timeStampNs >= 0
                && (parity == 0 || parity == 1)
                && (0 <= x && x < 1)
                && (0 <= y && y < 1)
        );
    }

    /**
     * Returns the in-flight positioning message corresponding to the given raw message
     * @param rawMessage    Raw adsb-message
     * @return  Position message containing the coordinates and the altitude of the plane, null if the altitude
     * is invalid
     */
    public static AirbornePositionMessage of(RawMessage rawMessage) {
        int altitude = extractUInt(rawMessage.payload(),ALT_START, ALT_SIZE);
        byte altBit4 = (byte) extractUInt(altitude, Q_INDEX, 1);
        int format = extractUInt(rawMessage.payload(), FORMAT_START, FORMAT_SIZE);
        int cprLatitude = extractUInt(rawMessage.payload(), LAT_START, COORD_SIZE);
        int cprLongitude = extractUInt(rawMessage.payload(), LON_START, COORD_SIZE);
        double decodedAlt;
        if (altBit4 == 1) {

            decodedAlt = ( extractUInt(altitude, Q_INDEX + 1 , ALT_SIZE - Q_INDEX - 1) << Q_INDEX )
                    | (extractUInt(altitude, 0, Q_INDEX));
            decodedAlt = - Q1_ALT_BASE + FOOT_FACTOR_25 * decodedAlt;
        } else {
            //  Left bit set (C & A)  ||     Right bit set (B & D)
            //  C1 A1 C2 A2 C4 A4     ||     B1 D1 B2 D2 B4 D4
            byte CACACA = (byte) extractUInt(altitude, ALT_SIZE/2, ALT_SIZE/2); // ALT_SIZE = 12
            byte BDBDBD = (byte) extractUInt(altitude, 0, ALT_SIZE/2);
            //  CA bit set            ||     BD bit set
            //  C1 C2 C4 A1 A2 A4     ||     B1 B2 B4 D1 D2 D4
            byte CCCAAA = permute(CACACA);
            byte BBBDDD = permute(BDBDBD);
            // D set     ||  A set     ||  B set     ||  C set
            // D1 D2 D4  ||  A1 A2 A4  ||  B1 B2 B4  ||  C1 C2 C4
            byte D = (byte) extractUInt(BBBDDD, 0, ABCD_TRIO_SIZE);
            byte A = (byte) extractUInt(CCCAAA, 0, ABCD_TRIO_SIZE);
            byte B = (byte) extractUInt(BBBDDD, ABCD_TRIO_SIZE, ABCD_TRIO_SIZE);
            byte C = (byte) extractUInt(CCCAAA, ABCD_TRIO_SIZE, ABCD_TRIO_SIZE); // ABCD_TRIO_LEN = 3

            int feetMultiple100 = grayDecoder(C);// multiple of 100 feet composed of the LSB  ( CCC )
            //  multiple of 500 feet composed of the MSB  ( DDD AAA BBB ),
            //  hence the shifting of the byte D by 6, and the shifting of the byte A by 3
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

            decodedAlt = - Q2_ALT_BASE + feetMultiple100 * FOOT_FACTOR_100 + feetMultiple500 * FOOT_FACTOR_500;
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

