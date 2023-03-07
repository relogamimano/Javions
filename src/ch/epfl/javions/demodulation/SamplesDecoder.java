package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author: Sofia Henriques Garfo sciper:
 */
public class SamplesDecoder {
    private byte[] sample;
    private InputStream sampleStream;
    private int byteNumber;


    public SamplesDecoder(InputStream stream, int batchSize){
        Preconditions.checkArgument(batchSize > 0);
        if ( stream == null) {
            throw new NullPointerException();
        }
        sample = new byte[batchSize*2];
        byteNumber = batchSize*2;
        sampleStream = stream;

    }

    public int readBatch(short[] batch) throws IOException {
        int readBytes = sampleStream.readNBytes(sample, 0,byteNumber);
        Preconditions.checkArgument(batch.length == byteNumber );
        if ( readBytes != byteNumber ){
            byteNumber = (int) Math.floor( readBytes/2);
        }

        for (int i=0;i<=(byteNumber-1); i++ ){
            int j = i+1;
            int mostSignificantBits = (sample[j] & 0xf);
            int leastSignificantBits = sample[0];
            batch[i] = (short) (leastSignificantBits + ( mostSignificantBits << 8 ));

        }
        return readBytes/2;
    }
}
