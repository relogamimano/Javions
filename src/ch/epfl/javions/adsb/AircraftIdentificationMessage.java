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

    private static final int ID_SIZE = 8;
    private static final int ID_CHAR_SIZE = 6;
    private static final int CA_SIZE = 3;
    private static final int CA_START = 48;
    //Re-arranged ASCII table containing only the Capital Alphabet and every digit
    private static final String data = "?ABCDEFGHIJKLMNOPQRSTUVWXYZ????? ???????????????0123456789???????????????????";

    /**
     * Constructor that  checks if the preconditions are verified.
     * @param timeStampNs time stamp
     * @param icaoAddress ICAO address
     * @param category category
     * @param callSign call sign
     * @throws NullPointerException if the ICAO address or the call sign is null
     * @throws IllegalArgumentException if time stamps are negative
     */
    public AircraftIdentificationMessage {
        Objects.requireNonNull(callSign);
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument(timeStampNs >= 0);
    }

    /**
     * Returns the identification message corresponding to the given raw message,
     * @param rawMessage Raw message
     * @return Identification message  or null if at least one of the callsign characters it contains is invalid
     */
    public static AircraftIdentificationMessage of(RawMessage rawMessage) {
        StringBuilder string = new StringBuilder();
        for (int i = ID_SIZE - 1; i >= 0 ; i--) {
            char identifierCharacter = data.charAt(extractUInt(rawMessage.payload(), i * ID_CHAR_SIZE, ID_CHAR_SIZE));
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
        int category =( ((14 - rawMessage.typeCode()) << CA_SIZE + 1)
                | extractUInt(rawMessage.payload(), CA_START, CA_SIZE) ) & 0xff;
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
