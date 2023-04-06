package ch.epfl.javions.adsb;

import ch.epfl.javions.*;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.HexFormat;

import static ch.epfl.javions.Bits.extractUInt;
import static ch.epfl.javions.Units.convertFrom;

/**
 *
 * @author: Sofia Henriques Garfo (346298)
 * @author: Romeo Maignal (360568)
 *
 * The RawMessage public record represents a raw ADS-B message
 * @param timeStampNs time at wich the message was received, expressed in nanoseconds
 * @param bytes bytes of the message
 */
public record RawMessage(long timeStampNs, ByteString bytes) {

    /**
     * Size of the byte ADS-B message
     */
    // length in bytes
    private static final int START = 0;
    private static final int DFCA_BYTE_SIZE = 1;
    private static final int ICAO_START = START + DFCA_BYTE_SIZE;
    private static final int ICAO_BYTE_SIZE = 3;
    private static final int ME_START = ICAO_START + ICAO_BYTE_SIZE;
    private static final int ME_BYTE_SIZE = 7;
    private static final int CRC_START = ME_START + ME_BYTE_SIZE;
    private static final int CRC_BYTE_SIZE = 3;

    public static final int LENGTH = CRC_START + CRC_BYTE_SIZE;
    private static final int HEX_ICAO_SIZE = 6;

    // length in bits
    private static final int CA_START = START;
    private static final int CA_BIT_SIZE = 3;
    private static final int DF_START = CA_START + CA_BIT_SIZE;
    private static final int DF_BIT_SIZE = 5;



    /**
     * Checks that the given time stamp is valid (positive) and checks that the byte size is valid
     *
     */
    public  RawMessage{
        Preconditions.checkArgument((timeStampNs>= 0) && (bytes.size()==LENGTH));
    }


    /**
     * Returns the Raw message for the given time stamp and bytes
     * @param timeStamps time stamp
     * @param bytes bytes
     * @return 0 if the CRC24 isn't 0
     *         else returns the RawMesseage
     */
    public static RawMessage of(long timeStamps, byte[] bytes){
        Crc24 crc = new Crc24(Crc24.GENERATOR);
        if ( crc.crc(bytes) != 0  ){
            return null;
        } else return new RawMessage(timeStamps, new ByteString(bytes));
    }

    /**
     * Method which returns the size of a message whose first byte is the given one,
     * and which is LENGTH if the DF attribute contained in this first byte is 17, and 0 otherwise
     * @param byte0 first byte
     * @return size
     */
    public static int size(byte byte0) {
        int df = extractUInt(byte0, DF_START, DF_BIT_SIZE);
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
        return extractUInt(payload,
                (int) (convertFrom(ME_BYTE_SIZE, Byte.SIZE) - DF_BIT_SIZE),
                DF_BIT_SIZE);
    }

    /**
     * Gets link format ( 5 most significant bits of the byte at index 0 of the raw message)
     * @return link format
     */
    public int downLinkFormat(){
        return extractUInt(
                bytes.byteAt(START),
                DF_START, DF_BIT_SIZE);

    }

    /**
     * Gets the Icao Address (  byte 1 to 3 of the raw message)
     * @return Icao Address
     */
    public IcaoAddress icaoAddress(){
        String address = HexFormat.of()
                .withUpperCase().
                toHexDigits(
                        bytes.bytesInRange(ICAO_START, ME_START), HEX_ICAO_SIZE);
        return new IcaoAddress(address);
    }

    /**
     * Returns payload ( bytes 4 to 10 of the raw message ) expressed in long
     * @return payload (long)
     */
    public long payload(){
        return bytes.bytesInRange(ME_START, CRC_START);
    }

    /**
     * Gives the type code ( 5 most significant of the type load)
     * @return type code
     */
    public int typeCode(){
        return typeCode(payload());
    }

}
