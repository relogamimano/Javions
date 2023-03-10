package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;

/**
 * * @author: Sofia Henriques Garfo (346298)
 *  * @author: Romeo Maignal (360568)
 */
public class SamplesDecoder {
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