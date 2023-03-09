package ch.epfl.javions.demodulation;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;

public final class PowerComputer {
    //stocker un tbableau de type short[] issue de sampleDecoder, il est deux fois plus grand que le tableau suivant
    private short[] sample;
    private int[] queue = new int[Bits.LENTGH];
    private final int sampleNumber;
    private InputStream sampleStream;
    private int size;
    private int headPos;
    // TODO: 08.03.23 readSample comme ca ou plutot sur le modele de readByte comme pour SampleDecoder ?
    SamplesDecoder samplesDecoder;
    public PowerComputer(InputStream stream, int batchSize) throws IOException {
        Preconditions.checkArgument((batchSize % Bits.LENTGH == 0) && (batchSize > 0));
        sampleNumber = batchSize * 2;
        sampleStream = stream;
        headPos = Bits.LENTGH;
        size = Bits.LENTGH;
        sample = new short[sampleNumber];

    }
    public int readBatch(int[] batch) throws IOException{// le tembleau passé en argument est fait pour etre remplit, il ne doit pas etre lu
        samplesDecoder = new SamplesDecoder(sampleStream, sampleNumber);
        Preconditions.checkArgument(batch.length == sampleNumber/2);
        int readSample = samplesDecoder.readBatch(sample);


        for (int i = 0; i < batch.length; i++) {
            queue = removeFirstTwoAddTwo(queue, sample[i*2], sample[i*2 + 1]);
            int I = 0;
            int Q = 0;
            for (int j = 0,  k = 0; j < queue.length-1 || k < queue.length / 2; j+=2, k++) {
                I = I + (int)( Math.pow( -1, k + 1) * queue[j] );
                Q = Q + (int)( Math.pow( -1, k + 1) * queue[j+1] );
            }
            int power = (int) (Math.pow(I, 2) + Math.pow(Q, 2));
            batch[i] = power;

        }
        return readSample/2;//nb d'echantillions de puissance
    }

    private int[] removeFirstTwoAddTwo(int[] tab, int v1, int v2) {
        int[] shiftedTab = new int[Bits.LENTGH];
        System.arraycopy(tab, 2, shiftedTab, 0, shiftedTab.length - 2);
        shiftedTab[6] = v1;
        shiftedTab[7] = v2;
        return shiftedTab;
    }
}
