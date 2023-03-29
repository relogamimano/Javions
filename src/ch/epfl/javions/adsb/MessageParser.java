package ch.epfl.javions.adsb;

import java.util.Arrays;

public class MessageParser {

    private MessageParser(){}

    public Message parse(RawMessage rawMessage){
        int typeCode = rawMessage.typeCode();

        if ( typeCode == 19 ){
            return AirborneVelocityMessage.of(rawMessage);

        } else if (Arrays.asList(1, 2, 3,4).contains(typeCode) ){
            return AircraftIdentificationMessage.of(rawMessage);

        } else if ( ( typeCode >= 9 && typeCode <= 18 ) || (typeCode >= 20 && typeCode <= 22)){
            return AirbornePositionMessage.of(rawMessage);
        }
        return null;
    }
}
