package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author: Sofia Henriques Garfo sciper:
 * @author: Roméo Maignal sciper:
 */
public class SamplesDecoder {
    private byte[] sampleTab;
    private InputStream sampleStream;
    private int byteNumber;

    public SamplesDecoder(InputStream stream, int batchSize){
        Preconditions.checkArgument(batchSize > 0);
        if ( stream == null) {
            throw new NullPointerException();
        }
        byteNumber = batchSize * 2;
        sampleTab = new byte[byteNumber];
        sampleStream = stream;

    }

    public int readBatch(short[] batch) throws IOException {
        // TODO: 08.03.23 voir powercomputer pour l'emplacement de l'appel a un flot 
        int readBytes = sampleStream.readNBytes(sampleTab, 0, byteNumber);
        Preconditions.checkArgument(batch.length == byteNumber/2);
        if ( readBytes != byteNumber ){
            byteNumber = (int) Math.floor( readBytes/2);
        }

        for (int i = 0; i < batch.length; i++ ) {
            int j = i*2;
            short mostSignificantBits = (short)(sampleTab[j + 1] & 0xf);
            short leastSignificantBits = sampleTab[j] ;
            short uncenteredSample = (short) ((mostSignificantBits << 8)  + (leastSignificantBits & 0xff));
            batch[i] = (short)(uncenteredSample - (short)2048);
        }
        return readBytes/2;
    }
}
