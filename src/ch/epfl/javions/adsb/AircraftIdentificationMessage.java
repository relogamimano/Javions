package ch.epfl.javions.adsb;

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
    public AircraftIdentificationMessage of(RawMessage rawMessage) {
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < IDENTIFIER_LENGTH; i++) {
            char identifierCharacter = (char) (rawMessage.payload() >>> (i * IDENTIFIER_CHAR_LENGTH) & 0x3f);
            //todo how to remove those magic numbers ?
            if ( (27 <= identifierCharacter && identifierCharacter <= 31)
            || ( 33 <= identifierCharacter && identifierCharacter <= 47)
            || 58 <= identifierCharacter) {
                return null;
            } else {
                string.append(identifierCharacter);
            }
        }
        return new AircraftIdentificationMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), rawMessage.typeCode(), new CallSign(string.toString()));
    }

}
