package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;

/**
 * The public and final SamplesDecoder class represents an object capable
 * of transforming bytes coming from the AirSpy into signed 12-bit samples.
 *
 * @author: Sofia Henriques Garfo (346298)
 * @author: Romeo Maignal (360568)
 */
public class SamplesDecoder {
    private static final int BIAS = 2048;
    private final byte[] sampleTab;
    private final InputStream sampleStream;
    private int byteNumber;
    /**
     * Construct a Decoder Object giving access to a method that calculate short samples using a byte stream
     *
     * @param stream    stream of byte
     * @param batchSize length that determine how many bytes, from the input stream, will be used
     */
    public SamplesDecoder(InputStream stream, int batchSize){
        Preconditions.checkArgument(batchSize > 0);
        if ( stream == null) {
            throw new NullPointerException();
        }
        byteNumber = batchSize * 2;
        sampleTab = new byte[byteNumber];
        sampleStream = stream;

    }
    /**
     * File the int array given in parameter with samples made from every two bytes from the InputStream
     *
     * @param batch        short array filled with samples of combined bytes
     * @return             the length of the sample array
     * @throws IOException if the length of batch isn't equal to the number of bytes divided by 2
     */
    public int readBatch(short[] batch) throws IOException {
        int readBytes = sampleStream.readNBytes(sampleTab, 0, byteNumber);
        Preconditions.checkArgument(batch.length == byteNumber/2);
        if ( readBytes != byteNumber ){
            byteNumber = (int) Math.floor( ( (double)readBytes )/2);
        }

        for (int i = 0; i < batch.length; i++ ) {
            int j = i*2;
            short mostSignificantBits = (short)(sampleTab[j + 1] & 0xf);
            short leastSignificantBits = sampleTab[j] ;
            short uncenteredSample = (short) ((mostSignificantBits << Byte.SIZE)  + (leastSignificantBits & 0xff));
            batch[i] = (short)(uncenteredSample - (short)BIAS);
        }
        return readBytes/2;
    }
}
