package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;


public record AircraftIdentificationMessage(long timeStampNs, IcaoAddress icaoAddress, int category, CallSign callSign) implements Message {
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
        for (int i = IDENTIFIER_LENGTH - 1; i >= 0 ; i--) {
            char identifierCharacter = data.charAt(Bits.extractUInt(rawMessage.payload(), i * IDENTIFIER_CHAR_LENGTH, IDENTIFIER_CHAR_LENGTH));
            string.append(identifierCharacter);
        }
        if (string.toString().contains("?")) {
            return null;
        }
        //method trim() used to remove string of zeros at the end and the beginning of instance string
        String callSignString = string.toString().trim();
        int category =( ((14 - rawMessage.typeCode()) << 4) | Bits.extractUInt(rawMessage.payload(), 48, 3) ) & 0xff;
        return new AircraftIdentificationMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), category, new CallSign(callSignString));
    }

    @Override
    public long timeStampsNs() {
        return timeStampNs;
    }
}
