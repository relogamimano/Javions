package ch.epfl.javions.adsb;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

import static ch.epfl.javions.Bits.extractUInt;

/**
 * AircraftIdentificationMessage public, represents an ADS-B identification message
 * @author: Sofia Henriques Garfo (346298)
 * @author: Romeo Maignal (360568)
 */
public record AircraftIdentificationMessage(long timeStampNs, IcaoAddress icaoAddress, int category, CallSign callSign) implements Message{

    private static final int ID_LEN = 8;
    private static final int ID_CHAR_LEN = 6;
    private static final int CA_LEN = 3;
    private static final int ME_LEN = 4;
    private static final int CA_START = 48;
    //Re-arranged ASCII table containing only the Capital Alphabet and every digit
    private static final String data = "?ABCDEFGHIJKLMNOPQRSTUVWXYZ????? ???????????????0123456789???????????????????";

    /**
     * Constructor that throws NullPointerException if icaoAddress or callSign is null,
     * and IllegalArgumentException if timeStampNs is strictly less than 0.
     *
     * @param timeStampNs time stamp
     * @param icaoAddress ICAO address
     * @param category category
     * @param callSign call sign
     */
    public AircraftIdentificationMessage {
        Objects.requireNonNull(callSign);
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument(timeStampNs >= 0);
    }

    /**
     * Returns the identification message corresponding to the given raw message,
     * or null if at least one of the callsign characters it contains is invalid
     * @param rawMessage Raw message
     * @return Identification message
     */
    public static AircraftIdentificationMessage of(RawMessage rawMessage) {
        StringBuilder string = new StringBuilder();
        for (int i = ID_LEN - 1; i >= 0 ; i--) {
            char identifierCharacter = data.charAt(extractUInt(rawMessage.payload(), i * ID_CHAR_LEN, ID_CHAR_LEN));
            string.append(identifierCharacter);
        }
        if (string.toString().contains("?")) {
            return null;
        }
        //method trim() used to remove string of zeros at the end and the beginning of instance string
        String callSignString = string.toString().trim();
        //The category is obtained by combining the 3 bits of the CA field with the type code.
        // These two values are combined into a single 8-bit value,
        // of which the 4 MSB are 14 minus the type code, and the 4 LSB are the CA field.
        int category =( ((14 - rawMessage.typeCode()) << ME_LEN) | extractUInt(rawMessage.payload(), CA_START, CA_LEN) ) & 0xff;
        return new AircraftIdentificationMessage(
                rawMessage.timeStampNs(),
                rawMessage.icaoAddress(),
                category,
                new CallSign(callSignString));
    }

    @Override
    public long timeStampNs() {
        return timeStampNs;
    }

}
