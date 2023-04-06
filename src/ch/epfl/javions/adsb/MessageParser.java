package ch.epfl.javions.adsb;

import java.util.Arrays;

/**
 * Decodes the ADS-B messages and returns the corresponding velocity, position or identification message
 *@author: Sofia Henriques Garfo (346298)
 * @author: Romeo Maignal (360568)
 */
public class MessageParser {

    private MessageParser(){}

    /**
     * decodes the raw message
     * @param rawMessage
     * @return a velocity, position or identification message 
     */
    static public Message parse(RawMessage rawMessage){

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
