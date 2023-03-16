package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.Crc24;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

/**
 * 
 * @param timeStampNs
 * @param bytes
 */
public record RawMessage(long timeStampNs, ByteString bytes) {
    public static final int LENGTH = 14;

    public void RawMessage(){

        Preconditions.checkArgument(timeStampNs>= 0 && bytes.size()==LENGTH);
    }


    public static RawMessage of(long timeStamps, byte[] bytes){
        Crc24 crc = new Crc24(Crc24.GENERATOR);
        if ( crc.crc(bytes) != 0  ){
            return null;
        } else return new RawMessage(timeStamps, new ByteString(bytes));
    }

    public static int size(byte byte0){
        int a = Byte.toUnsignedInt(byte0);
        String b = Byte.toString(byte0);
        System.out.println(Integer.toBinaryString(a));
        System.out.println(byte0 + b);
        int df = Bits.extractUInt(byte0, 3, 5);
    if ( df == 17 ){
        return LENGTH;
    } else return 0;
    }

    public static int typeCode(long payload){
        return Bits.extractUInt(payload, 51, 5);
    }


    public int downLinkFormat(){
        int df = Bits.extractUInt(bytes.byteAt(0),3,5);
        return df;

    }

    public IcaoAddress icaoAddress(){
        String address = Long.toHexString(bytes.bytesInRange(1,4)).toUpperCase();
        return new IcaoAddress(address);
    }


    public long payload(){
        return bytes.bytesInRange(4,11);
    }


    public int typeCode(){
        return Bits.extractUInt(payload(), 51, 5);
    }

}
