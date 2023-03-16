package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.Crc24;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

/**
 * Raw ADS-B message
 * @param timeStampNs time at wich the message was received, expressed in nanoseconds
 * @param bytes bytes of the message
 */
public record RawMessage(long timeStampNs, ByteString bytes) {
    /**
     * Size of the byte ADS-B message
     */
    public static final int LENGTH = 14;
    public static ByteString byteString;

    /**
     * Checks that the given time stamp is valid (positive) and checks that the byte size is valid
     *
     */
    public  RawMessage{
        Preconditions.checkArgument((timeStampNs>= 0) && (bytes.size()==LENGTH));
    }


    /**
     * Returns the Raw message for the given time stamp and bytes
     * @param timeStamps
     * @param bytes
     * @return 0 if the CRC24 isn't 0
     *         else returns the RawMesseage
     */
    public static RawMessage of(long timeStamps, byte[] bytes){
        Crc24 crc = new Crc24(Crc24.GENERATOR);
        if ( crc.crc(bytes) != 0  ){
            return null;
        } else return new RawMessage(timeStamps, new ByteString(bytes));
    }


    public static int size(byte byte0) {
        int df = Bits.extractUInt(byte0, 3, 5);
        if (df == 17) {
            return LENGTH;
        } else return 0;
    }

    /**
     * Returns the type code ( 5 most significant bits of the ME) for the given ME ( bytes 4 to 10 of the raw message)
     * @param payload bytes 4 to 10 of the raw message
     * @return type code
     */
    public static int typeCode(long payload){
        return Bits.extractUInt(payload, 51, 5);
    }

    /**
     * Gets link format ( 5 most significant bits of the byte at index 0 of the raw message)
     * @return link format
     */
    public int downLinkFormat(){
        int df = Bits.extractUInt(bytes.byteAt(0),3,5);
        return df;

    }

    /**
     * Gets the Icao Address (  byte 1 to 3 of the raw message)
     * @return Icao Address
     */
    public IcaoAddress icaoAddress(){
        String address = Long.toHexString(bytes.bytesInRange(1,4)).toUpperCase();
        return new IcaoAddress(address);
    }

    /**
     * Returns payload ( bytes 4 to 10 of the raw message ) expressed in long
     * @return payload (long)
     */
    public long payload(){
        return bytes.bytesInRange(4,11);
    }

    /**
     * Gives the type code ( 5 most significant of the type load)
     * @return type code
     */
    public int typeCode(){
        return typeCode(payload());
    }

}
