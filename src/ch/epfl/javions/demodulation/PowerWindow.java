package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author: Sofia Henriques Garfo (346298)
 * @author: Romeo Maignal (360568)
 */
public class PowerWindow {
    private PowerComputer powerComputer;
    private int[] firstBatch;
    private int[] secondBatch;
    private final int BATCHSIZE = (int) Math.scalb(1,16); // TAILLE DES LOTS ENCHANTILLON DE PUISSANCE
    private int windowSize; // position pr au debut du flot
    private int windowPosition;

    private int currentBatch= 0;
    private int availableSamples;

    private int positionInTab;

    /**
     * Returns a window with the given size over the power samples
     * @param stream stream of bytes
     * @param windowSize
     * @throws IOException if the window size is greater than 2^16, negative or null
     */
    public PowerWindow(InputStream stream, int windowSize) throws IOException {
        Preconditions.checkArgument(0 < windowSize && windowSize <= BATCHSIZE);
        powerComputer = new PowerComputer(stream,BATCHSIZE);
        firstBatch = new int[BATCHSIZE];
        secondBatch = new int[BATCHSIZE];
        availableSamples  = powerComputer.readBatch(firstBatch);
        this.currentBatch ++;
        this.windowSize= windowSize;
        windowPosition = 0;
        positionInTab = 0;
    }

    /**
     * @return window size
     */
    public int size() {
        return windowSize;
    }

    /**
     * Current position relative to the beginning of the input stream
     * @return window postion
     */

    public long position(){
        return windowPosition;
    }

    /**
     * Verifies if the window is full
     * @return true if there are as many samples as the window's size
     */
    public boolean isFull() {
        if ( availableSamples < windowSize){
            return false;
        } else return true;
         }

    /**
     * Gives the sample located in the window at index i
     * @param i index
     * @return sample
     * @throws IndexOutOfBoundsException if the index is negative or greater than the window size
     */
    public int get(int i) {
        if ((i < 0) || (i >= windowSize)) {
            throw new IndexOutOfBoundsException();
        }
        if ((positionInTab + i) >= firstBatch.length) { // position  is not on main tab
            return secondBatch[i - (firstBatch.length - positionInTab)];

        } else { return firstBatch[i + positionInTab];
            }
        }
    // i:587  position in tab:64949
    /**
     * Advances the window 1 position
     * @throws IOException
     */
    public void advance() throws IOException{ // handle exception??
        windowPosition ++;
        availableSamples --;
        positionInTab ++;
        int [ ] tempBatch;

        if ( (this.windowPosition + this.windowSize) % BATCHSIZE == 0){  // si la fenetre chevauche le prochain lot
                availableSamples += powerComputer.readBatch(secondBatch);
        }



    if( this.windowPosition % BATCHSIZE == 0 ) {// si le premier echantillon appartiens au deuxieme tableau on le set comme main
        positionInTab = 0;
        tempBatch = firstBatch;
        firstBatch = secondBatch;
        secondBatch = tempBatch;


    }}

    /**
     * Advances the window position several times
     * @param offset number of positions to advance
     * @throws IOException
     */
    public void advanceBy( int offset) throws IOException{
        Preconditions.checkArgument(offset > 0);
        for(int i = 0; i< offset; i++)
            this.advance();
        }

    }
