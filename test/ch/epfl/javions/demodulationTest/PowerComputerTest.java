package ch.epfl.javions.demodulationTest;

import ch.epfl.javions.demodulation.PowerComputer;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;

import static org.junit.Assert.*;
/**
 * * @author: Sofia Henriques Garfo (346298)
 *  * @author: Romeo Maignal (360568)
 */
public class PowerComputerTest {
    @Test
    void readBatchWorksWithLastBytesOfInputStream() throws IOException {
        DataInputStream stream = new DataInputStream(new BufferedInputStream(
                new FileInputStream(new File("resources/samples.bin"))));
        PowerComputer powerComputer = new PowerComputer(stream, 1208);
        int [] batch = new int[1208];
        powerComputer.readBatch(batch);
        System.out.println(Arrays.toString(batch));
        //last values should be a sequence of 7 zeros
    }
    @Test
    void readBatchWorksWithTenFirstTrivialValues() throws IOException {
        DataInputStream stream = new DataInputStream(new BufferedInputStream(
                new FileInputStream(new File("resources/samples.bin"))));
        PowerComputer powerComputer = new PowerComputer(stream, 80);
        int [] expected = {73, 292, 65, 745, 98, 4226, 12244, 25722, 36818, 23825};
        int [] batch = new int[80];
        powerComputer.readBatch(batch);
        System.out.println(Arrays.toString(batch));
        assertArrayEquals(expected, Arrays.copyOf(batch, 10));
    }

    @Test
    void print160FirstValues() throws IOException {
        DataInputStream stream = new DataInputStream(new BufferedInputStream(
                new FileInputStream(new File("resources/samples.bin"))));
        PowerComputer powerComputer = new PowerComputer(stream, 160);
        int [] batch = new int[160];
        powerComputer.readBatch(batch);
        for (int i :
                batch) {
            System.out.println(i);
        }
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
