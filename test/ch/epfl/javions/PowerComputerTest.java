package ch.epfl.javions.demodulationTest;

import ch.epfl.javions.demodulation.PowerComputer;
import ch.epfl.javions.demodulation.SamplesDecoder;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;

public class PowerComputerTest {
    @Test
    void readBatchWorksWithTrivialValues() throws IOException {
        DataInputStream stream = new DataInputStream(new BufferedInputStream(
                new FileInputStream(new File("resources/samples.bin"))));
        PowerComputer powerComputer = new PowerComputer(stream, 10);
        int [] expected = {73, 292, 65, 745, 98, 4226, 12244, 25722, 36818, 23825};
        int [] batch = new int[10];
        powerComputer.readBatch(batch);
        assertEquals(expected,batch);
    }
}
