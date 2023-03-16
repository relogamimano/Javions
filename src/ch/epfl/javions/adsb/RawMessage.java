package ch.epfl.javions.adsb;

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

    // TODO: est ce qu'on fait quelque chose avec timestamps ou pas
    public static RawMessage of(long timeStamps, byte[] bytes){
        Crc24 crc = new Crc24(Crc24.GENERATOR);
        if ( crc.crc(bytes) != 0  ){
            return null;
        } else return new RawMessage(timeStamps, new ByteString(bytes));
    }

    public static int size(byte byte0){
    int df = byte0 & 0x1f;
    if ( df == 17){
        return LENGTH;
    } else return 0;
    }

    public static int typeCode(long payload){
        return (int) ( payload << 8 ) & 0x1f;
    }


    public int downLinkFormat(){
        int df = bytes.byteAt(0) & 0x1f;
        return df;

    }
    // TODO: idk si la conversion fonctionne
    public IcaoAddress icaoAddress(){
        String address = bytes.bytesInRange(1,4)+"";
        return new IcaoAddress(address);
    }


    public long payload(){
        return bytes.bytesInRange(4,11);
    }



    public int typeCode(){
        return (int) payload() & 0x1f;
    }

}
