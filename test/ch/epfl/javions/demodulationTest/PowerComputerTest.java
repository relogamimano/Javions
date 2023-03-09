package ch.epfl.javions.demodulationTest;

import ch.epfl.javions.demodulation.PowerComputer;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;

import static org.junit.Assert.*;

public class PowerComputerTest {
    @Test
    void readBatchWorksWithTrivialValues() throws IOException {
        DataInputStream stream = new DataInputStream(new BufferedInputStream(
                new FileInputStream(new File("resources/samples.bin"))));
        // TODO: 09.03.23 batchSize c'est la taille du nb de byte qu'il faut sortir du stream ou c'est ce meme flot divisé par 4 (le nb de puissance que l'on pourra compooser) ? 
        PowerComputer powerComputer = new PowerComputer(stream, 80);
        int [] expected = {73, 292, 65, 745, 98, 4226, 12244, 25722, 36818, 23825};
        int [] batch = new int[80];
        powerComputer.readBatch(batch);
        assertArrayEquals(expected, Arrays.copyOf(batch, 10));
    }
    @Test
    void constructorThrowsIllegalArgumentException() throws IOException {
        DataInputStream stream = new DataInputStream(new BufferedInputStream(
                new FileInputStream(new File("resources/samples.bin"))));
        assertThrows(IllegalArgumentException.class, () -> new PowerComputer(stream, 79));
    }
    @Test
    void readBatchThrowsIllegalArgumentException() throws IOException {
        DataInputStream stream = new DataInputStream(new BufferedInputStream(
                new FileInputStream(new File("resources/samples.bin"))));
        int [] batch = new int[41];
        assertThrows(IllegalArgumentException.class, () -> new PowerComputer(stream, 80).readBatch(batch));


    }
}
