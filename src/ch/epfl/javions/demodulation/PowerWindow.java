package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;

public class PowerWindow {
    private PowerComputer powerComputer;
    private int[] evenBatch;
    private int[] oddBatch;
    private final int BATCHSIZE = (int) Math.scalb(1,16); // TAILLE DES LOTS ENCHANTILLON DE PUISSANCE
    private int windowSize;
    private int windowPostion;
    private int[] mainBatch;
    private int currentBatch= 0;
    private int availableSamples;

    public PowerWindow(InputStream stream, int windowSize, int batchSize) throws IOException {
        Preconditions.checkArgument((0 < windowSize) && (windowSize <= Math.pow(2, 16)));


        this.windowSize = windowSize;
        powerComputer = new PowerComputer(stream, batchSize);
        evenBatch = new int[batchSize];
        oddBatch = new int[batchSize];
        windowPostion = 0;
        this.currentBatch ++;


        availableSamples = powerComputer.readBatch(evenBatch);


    }
/*    public PowerWindow(InputStream stream, int windowSize) throws IOException {
        Preconditions.checkArgument(0 < windowSize && windowSize <= BATCHSIZE);
        this.stream = stream; // TODO: check if use this outside of the constructe
        powerComputer = new PowerComputer(stream,BATCHSIZE);
        powerComputer.readBatch(evenBatch);
        mainBatch = evenBatch;
        availableSamples = powerComputer.readBatch(evenBatch);
        this.currentBatch ++;
        this.windowSize= windowSize;
        windowPostion = 0;
    }*/

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

 /*   public int get(int i){
        if((i <0 ) || ( i >= windowSize)){
            throw new IndexOutOfBoundsException();
        }

    }*/

    public void advance() throws IOException{ // handle exception??
        windowPostion ++;
        availableSamples --;

        if ( ((windowPostion + windowSize - 1 )/currentBatch) > mainBatch.length ){  // si la fenetre chevauche le prochain lot
                if (mainBatch == evenBatch){
                    powerComputer.readBatch(oddBatch);
                    availableSamples += oddBatch.length;
                } else if (mainBatch == oddBatch) {
                    powerComputer.readBatch(evenBatch);
                    availableSamples += evenBatch.length;
                }
        }

        // si le premier echantillon appartiens au deuxieme tableau on le set comme premier

    if((windowPostion/currentBatch) > mainBatch.length ) { // si le premier echantillon appartiens au deuxieme tableau on le set comme main
        if (mainBatch == evenBatch) {
            mainBatch = oddBatch;
            currentBatch ++;
        } else if (mainBatch == oddBatch) {
            mainBatch = evenBatch;
            currentBatch ++;

        }

    }}


    public void advanceBy( int offset) throws IOException{
        Preconditions.checkArgument(offset >= 0);
        for(int i = 0; i< offset; i++)
            this.advance();
        }

    }

/*

        package ch.epfl.javions.demodulation;

        import ch.epfl.javions.Preconditions;

        import java.io.IOException;
        import java.io.InputStream;

public final class PowerWindow {
    private int windowSize = 0;
    private int powerBatchSize = (1 << 16); //2^16 //todo: PAS OUBLIER DE LE REMETTRE
    //private int powerBatchSize = 16; //todo: à enlever : demander powercomputer
    private int[] powerTableOne = new int[this.powerBatchSize]; //contiendra le premier lot (index 0), le trosième (index 2), etc... donc les index pairs
    private int[] powerTableTwo = new int[this.powerBatchSize]; //contiendra le deuxième lot (index 1), le quatrième (index 3), etc... donc les index impairs
    private int powerRead = 0;
    private int currentBatch = 1; //on ajoute un à chaque fois que l'on passe au lot suivant

    private boolean isTableOnePrimary = true; //permet de savoir quel table contient les plus anciens échantillons de puissance (bool puisque que 2 val possible)
    private long windowPosition = 0L;
    private PowerComputer computer;
    public PowerWindow(InputStream stream, int windowSize) throws IOException {
        Preconditions.checkArgument(!(windowSize <= 0 || windowSize > this.powerBatchSize));
        this.windowSize = windowSize;
        this.computer = new PowerComputer(stream, this.powerBatchSize);
        this.powerRead = this.computer.readBatch(powerTableOne); //todo: error handling
    }
    //todo: demander si faut implémenter les cas limites avec powerRead ??

    public int size() { return this.windowSize; }
    public long position() { return this.windowPosition; }
    public boolean isFull() { return (this.windowPosition % this.powerBatchSize) + this.windowSize < this.powerRead; } //todo: demander si fin du lot = fin d'array on fin de powerRead
    public int get(int i) { //todo: demander si on vérif avec isFull aussi?? ou on peut juste retourner 0 + vérif avec powerRead ou pas besoin??
        if (i < 0 || i >= this.windowSize)
            throw new IndexOutOfBoundsException("L'index précisé dans les paramètres n'est pas compris dans la fenêtre");

        boolean isTableOnePrimaryTemp = this.isTableOnePrimary; //on stock la valeur dans une valeur temp, évite de revoir remettre la valeur à la fin de la fonction
        if ((this.windowPosition % this.powerBatchSize) + i >= this.powerBatchSize)
            isTableOnePrimaryTemp = !isTableOnePrimaryTemp; //le get s'étend sur le deuxième tableau, on 'swap' le tableau principal pour le getter et reset après, évite d'avoir un grand nombre de if else

        int power = 0;
        int index = (int)(this.windowPosition % this.powerBatchSize) + i; //index si on ne doit pas accéder au tableau secondaire //todo: voir si ça peut poser problème le cast, mais devrait pas

        if (isTableOnePrimaryTemp != this.isTableOnePrimary) //on a swap les deux tables juste pour le getter, on ajuste donc l'index
            index -= this.powerBatchSize; //on ajuste l'index pour pouvoir accéder au bon élément dans le tableau secondaire

        if (isTableOnePrimaryTemp)
            power = this.powerTableOne[index];
        else
            power = this.powerTableTwo[index];

        return power;
    }

    public void advance() throws IOException { //todo: error handling?
        this.windowPosition++;

        if ((this.windowPosition + this.windowSize) % this.powerBatchSize == 0) //la fin de la fenêtre a atteint le début du nouveau lot, il faut donc le lire
            if (this.isTableOnePrimary)
                this.powerRead = this.computer.readBatch(this.powerTableTwo);
            else
                this.powerRead = this.computer.readBatch(this.powerTableOne);


        if (this.windowPosition % this.powerBatchSize == 0)//toute la fenêtre est sortie du lot principal
            this.isTableOnePrimary = !this.isTableOnePrimary; //on change de tableau "primaire"

    }
    public void advanceBy(int offset) throws IOException {
        Preconditions.checkArgument(!(offset <= 0));
        for(int i = 0; i< offset; i++)
        this.advance(); //d'après le prof sur ED pas besoin de faire une version optimisée,
    }

}
*/
