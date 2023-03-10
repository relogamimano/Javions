package ch.epfl.javions.demodulationTest;



import ch.epfl.javions.demodulation.PowerWindow;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class PowerWindowTest {
    private String samplesFilePath = "resources/samples.bin";
    private InputStream samplesStream = new FileInputStream(samplesFilePath);

    PowerWindowTest() throws IOException {
    }

    @Test
    void isFullWorksWithSingleArray() throws IOException {
        int batchSize = 1208; // 7 échantillons ne pourront pas être fournis
        int[] powerSamples1 = new int[batchSize];
        int windowSize = 1201;
        PowerWindow pw = new PowerWindow(samplesStream, windowSize, batchSize);

        assertTrue(pw.isFull()); // la fenêtre doit contenir exactement les 1201 échantillons que le fichier peut fournir
        pw.advance();
        assertFalse(pw.isFull()); // la fenêtre ne doit plus être pleine

    }

    @Test
    void isFullWorksWithDualArrays() throws IOException {
        int batchSize = 608; // powerWindow utilise 2 tableaux de 608 échantillons, mais 15 ne pourront pas être fournis
        int[] powerSamples1 = new int[batchSize];
        int windowSize = 1;
        PowerWindow pw = new PowerWindow(samplesStream, windowSize, batchSize);

        while (pw.isFull()) {
            pw.advance();
//   System.out.println(pw.position());
        }
        System.out.println(pw.position()); // todo: le problème est que advance ne fonctionne pas correctement (if pas pris en compte)
    }




}

