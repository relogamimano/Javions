package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;

/**
 * * @author: Sofia Henriques Garfo (346298)
 *  * @author: Romeo Maignal (360568)
 */
public final class PowerComputer {
    private final short[] sample;
    private int[] queue = new int[Byte.SIZE];

    private final int sampleNumber;
    private final InputStream sampleStream;
    // TODO: 08.03.23 readSample comme ca ou plutot sur le modele de readByte comme pour SampleDecoder ?
    SamplesDecoder samplesDecoder;

    /**
     * Construct a Computer Object giving access to a method that calculate power samples using a byte stream
     *
     * @param stream    stream of byte
     * @param batchSize length that determine how many bytes, from the input stream, will be used
     */
    public PowerComputer(InputStream stream, int batchSize) {
        Preconditions.checkArgument((batchSize % Byte.SIZE == 0) && (batchSize > 0));
        sampleNumber = batchSize * 2;
        sampleStream = stream;
        sample = new short[sampleNumber];
    }

    /**
     * File the int array given in parameter with power samples made from every two samples from the InputStream
     *
     * @param batch        int array filled with power samples
     * @return             the length of the power sample array
     * @throws IOException if the length of batch isn't equal to the number of samples divided by2
     */
    public int readBatch(int[] batch) throws IOException{// le tembleau passé en argument est fait pour etre remplit, il ne doit pas etre lu
        samplesDecoder = new SamplesDecoder(sampleStream, sampleNumber);
        Preconditions.checkArgument(batch.length == sampleNumber/2);
        int readSample = samplesDecoder.readBatch(sample);

        for (int i = 0; i < readSample / 2; i++) {
            queue = removeFirstTwoAddTwo(queue, sample[i*2], sample[i*2 + 1]);
            int I = 0;
            int Q = 0;
            for (int j = 0,  k = 0; j < queue.length-1 || k < queue.length / 2; j+=2, k++) {
                // TODO: 10.03.23 changer pow
                I = I + (int)( Math.pow( -1, k + 1) * queue[j] );
                Q = Q + (int)( Math.pow( -1, k + 1) * queue[j+1] );
            }
            int power = (int) (Math.pow(I, 2) + Math.pow(Q, 2));
            batch[i] = power;

        }
        return readSample/2;
    }
    // methode used to shift the queue to the left two times and add the two next samples at the end
    private int[] removeFirstTwoAddTwo(int[] tab, int v1, int v2) {
        int[] shiftedTab = new int[Byte.SIZE];
        System.arraycopy(tab, 2, shiftedTab, 0, shiftedTab.length - 2);
        shiftedTab[6] = v1;
        shiftedTab[7] = v2;
        return shiftedTab;
    }
}
