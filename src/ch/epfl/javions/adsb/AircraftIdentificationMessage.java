package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;


public record AircraftIdentificationMessage(long timeStampNs, IcaoAddress icaoAddress, int category, CallSign callSign) {
    private static final int IDENTIFIER_LENGTH = 8;
    private static final int IDENTIFIER_CHAR_LENGTH = 6;
    public AircraftIdentificationMessage {
        Objects.requireNonNull(callSign);
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument(timeStampNs >= 0);
    }
    static String data = "?ABCDEFGHIJKLMNOPQRSTUVWXYZ????? ???????????????0123456789???????????????????";
    public static AircraftIdentificationMessage of(RawMessage rawMessage) {
        StringBuilder string = new StringBuilder();

        for (int i = 0; i < IDENTIFIER_LENGTH; i++) {

            char identifierCharacter = data.charAt(Bits.extractUInt(rawMessage.payload(), i * IDENTIFIER_CHAR_LENGTH, IDENTIFIER_CHAR_LENGTH));
            string.append(identifierCharacter);
        }
        if (string.toString().contains("?")) {
            return null;
        }

        int category =( ((14 - rawMessage.typeCode()) << 4) | Bits.extractUInt(rawMessage.payload(), 48, 3) ) & 0xff;
        if (category == 177) {
            return null;
        }
        return new AircraftIdentificationMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), category, new CallSign(string.reverse().toString().trim()));
    }

}
