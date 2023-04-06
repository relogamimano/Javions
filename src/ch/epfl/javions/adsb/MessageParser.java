package ch.epfl.javions.adsb;

import java.util.List;

/**
 * Decodes the ADS-B messages and returns the corresponding velocity, position or identification message
 * @author: Sofia Henriques Garfo (346298)
 * @author: Romeo Maignal (360568)
 */
public class MessageParser {
    private static final int VEL_TYPE_CODE = 19;
    private static final List<Integer> ID_TYPE_CODES = List.of(1,2, 3, 4);
    private static final int FROM = 0;
    private static final int TO = 1;
    private static final int[] POS_CODES1 = new int[]{9, 18};
    private static final int[] POS_CODES2 = new int[]{20, 22};
    private MessageParser(){}

    /**
     * decodes the raw message
     * @param rawMessage raw message
     * @return a velocity, position or identification message 
     */
    static public Message parse(RawMessage rawMessage){

        int typeCode = rawMessage.typeCode();

        if ( typeCode == VEL_TYPE_CODE){
            return AirborneVelocityMessage.of(rawMessage);

        } else if (ID_TYPE_CODES.contains(typeCode)){
            return AircraftIdentificationMessage.of(rawMessage);

        } else if ( ( typeCode >= POS_CODES1[FROM] && typeCode <= POS_CODES1[TO] )
                || (typeCode >= POS_CODES2[FROM] && typeCode <= POS_CODES2[TO])){
            return AirbornePositionMessage.of(rawMessage);
        }
        return null;
    }
}
