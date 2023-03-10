package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;

public class PowerWindow {
    private final PowerComputer powerComputer;
    private int[] firstBatch;
    private int[] secondBatch;
    private final int BATCHSIZE = (int) Math.scalb(1,16); // TAILLE DES LOTS ENCHANTILLON DE PUISSANCE
    private final int windowSize;
    private int windowPostion;

    private int currentBatch= 0;
    private int availableSamples;

    private int positionInTab;


    public PowerWindow(InputStream stream, int windowSize) throws IOException {
        Preconditions.checkArgument(0 < windowSize && windowSize <= BATCHSIZE);
        powerComputer = new PowerComputer(stream,BATCHSIZE);
        firstBatch = new int[BATCHSIZE];
        secondBatch = new int[BATCHSIZE];
        availableSamples  = powerComputer.readBatch(firstBatch);
        this.currentBatch ++;
        this.windowSize= windowSize;
        windowPostion = 0;
        positionInTab = 0;
    }

    public int size() {
        return windowSize;
    }

    public long position(){
        return windowPostion;
    }

    public boolean isFull() {
        if ( availableSamples < windowSize){
            return false;
        } else return true;
    }

    public int get(int i) {
        if ((i < 0) || (i >= windowSize)) {
            throw new IndexOutOfBoundsException();
        }
        if ((positionInTab + i) > firstBatch.length) { // position i is not on main tab
            return secondBatch[i - (firstBatch.length - positionInTab)];

        } else { return firstBatch[i + positionInTab];
        }
    }


    public void advance() throws IOException{ // handle exception??
        windowPostion ++;
        availableSamples --;
        positionInTab ++;
        int [ ] tempBatch;

        if ( ((windowPostion + windowSize - 1 )/currentBatch) > firstBatch.length ){  // si la fenetre chevauche le prochain lot
            availableSamples += powerComputer.readBatch(secondBatch);
        }



        if( positionInTab > firstBatch.length ) {// si le premier echantillon appartiens au deuxieme tableau on le set comme main
            positionInTab = 0;
            tempBatch = firstBatch;
            firstBatch = secondBatch;
            secondBatch = firstBatch;


        }}


    public void advanceBy( int offset) throws IOException{
        Preconditions.checkArgument(offset >= 0);
        for(int i = 0; i< offset; i++)
            this.advance();
    }

}
