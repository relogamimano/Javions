package ch.epfl.javions.demodulation;

import ch.epfl.javions.demodulation.SamplesDecoder;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.Assert.*;

public class SamplesDecoderTest {


    @Test
    void readBatchWorksWithTrivialValues() throws IOException {
            DataInputStream stream = new DataInputStream(new BufferedInputStream(
            new FileInputStream(new File("resources/samples.bin"))));
            SamplesDecoder samplesDecoder = new SamplesDecoder(stream, 10);
            short [] expected = {-3, 8, -9, -8, -5, -8, -12, -16, -23, -9};
            short [] batch = new short[10];
            samplesDecoder.readBatch(batch);
            assertArrayEquals(expected, batch);
            }

@Test
    void constructorThrowsNullPointerException() {
            InputStream stream = null;
            assertThrows(NullPointerException.class, () -> new SamplesDecoder(stream, 1));
        }

@Test
    void constructorThrowsIllegalArgumentException() throws FileNotFoundException {
            DataInputStream stream = new DataInputStream(new BufferedInputStream(
            new FileInputStream(new File("resources/samples.bin"))));
            assertThrows(IllegalArgumentException.class, () -> new SamplesDecoder(stream, -1));
        }
@Test
    void readBatchThrowsIllegalArgumentException() throws FileNotFoundException {
            DataInputStream stream = new DataInputStream(new BufferedInputStream(
            new FileInputStream(new File("resources/samples.bin"))));
            SamplesDecoder samplesDecoder = new SamplesDecoder(stream, 10);
            short [] batch = new short[11];
            assertThrows(IllegalArgumentException.class, () -> samplesDecoder.readBatch(batch));
       }
        }