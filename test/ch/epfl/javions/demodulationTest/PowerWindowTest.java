package ch.epfl.javions.demodulationTest;

import ch.epfl.javions.demodulation.PowerWindow;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class PowerWindowTest {
    private static final int BATCH_SIZE = 1 << 16;
    private static final int BATCH_SIZE_BYTES = bytesForPowerSamples(BATCH_SIZE);
    private static final int STANDARD_WINDOW_SIZE = 1200;
    private static final int BIAS = 1 << 11;

    private static int bytesForPowerSamples(int powerSamplesCount) {
        return powerSamplesCount * 2 * Short.BYTES;
    }

    @Test
    void powerWindowConstructorThrowsWithInvalidWindowSize() throws IOException {
        try (var s = InputStream.nullInputStream()) {
            assertThrows(IllegalArgumentException.class, () -> new PowerWindow(s, 0));
            assertThrows(IllegalArgumentException.class, () -> new PowerWindow(s, -1));
            assertThrows(IllegalArgumentException.class, () -> new PowerWindow(s, (1 << 16) + 1));
        }
    }

    @Test
    void powerWindowSizeReturnsWindowSize() throws IOException {
        try (var s = InputStream.nullInputStream()) {
            for (var i = 1; i <= 1 << 16; i <<= 1) {
                var w = new PowerWindow(s, i);
                assertEquals(i, w.size());
            }
        }
    }

    @Test
    void powerWindowPositionIsCorrectlyUpdatedByAdvance() throws IOException {
        var batches16 = new byte[BATCH_SIZE_BYTES * 16];
        try (var s = new ByteArrayInputStream(batches16)) {
            var w = new PowerWindow(s, STANDARD_WINDOW_SIZE);
            var expectedPos = 0L;

            assertEquals(expectedPos, w.position());

            w.advance();
            expectedPos += 1;
            assertEquals(expectedPos, w.position());

            w.advanceBy(BATCH_SIZE);
            expectedPos += BATCH_SIZE;
            assertEquals(expectedPos, w.position());

            w.advanceBy(BATCH_SIZE - 1);
            expectedPos += BATCH_SIZE - 1;
            assertEquals(expectedPos, w.position());

            w.advance();
            expectedPos += 1;
            assertEquals(expectedPos, w.position());
        }
    }

    @Test
    void powerWindowAdvanceByCanAdvanceOverSeveralBatches() throws IOException {
        var bytes = bytesForZeroSamples(16);

        var batchesToSkipOver = 2;
        var inBatchOffset = 37;
        var offset = batchesToSkipOver * BATCH_SIZE + inBatchOffset;
        var sampleBytes = Base64.getDecoder().decode(PowerComputerTest.SAMPLES_BIN_BASE64);
        System.arraycopy(sampleBytes, 0, bytes, bytesForPowerSamples(offset), sampleBytes.length);

        try (var s = new ByteArrayInputStream(bytes)) {
            var w = new PowerWindow(s, STANDARD_WINDOW_SIZE);
            w.advanceBy(inBatchOffset);
            w.advanceBy(batchesToSkipOver * BATCH_SIZE);
            var expected = Arrays.copyOfRange(PowerComputerTest.POWER_SAMPLES, 0, STANDARD_WINDOW_SIZE);
            var actual = new int[STANDARD_WINDOW_SIZE];
            for (var i = 0; i < STANDARD_WINDOW_SIZE; i += 1) actual[i] = w.get(i);
            assertArrayEquals(expected, actual);
        }
    }

    @Test
    void powerWindowIsFullWorks() throws IOException {
        var twoBatchesPlusOneWindowBytes =
                bytesForPowerSamples(BATCH_SIZE * 2 + STANDARD_WINDOW_SIZE);
        var twoBatchesPlusOneWindow = new byte[twoBatchesPlusOneWindowBytes];
        try (var s = new ByteArrayInputStream(twoBatchesPlusOneWindow)) {
            var w = new PowerWindow(s, STANDARD_WINDOW_SIZE);
            assertTrue(w.isFull());

            w.advanceBy(BATCH_SIZE);
            assertTrue(w.isFull());

            w.advanceBy(BATCH_SIZE);
            assertTrue(w.isFull());

            w.advance();
            assertFalse(w.isFull());
        }
    }

    @Test
    void powerWindowGetWorksOnGivenSamples() throws IOException {
        try (var sampleStream = PowerComputerTest.getSamplesStream()) {
            var windowSize = 100;
            var w = new PowerWindow(sampleStream, windowSize);
            for (var offset = 0; offset < 100; offset += 1) {
                var expected = Arrays.copyOfRange(PowerComputerTest.POWER_SAMPLES, offset, offset + windowSize);
                var actual = new int[windowSize];
                for (var i = 0; i < windowSize; i += 1) actual[i] = w.get(i);
                assertArrayEquals(expected, actual);
                w.advance();
            }
        }
    }

    @Test
    void powerWindowGetWorksAcrossBatches() throws IOException {
        byte[] bytes = bytesForZeroSamples(2);
        var firstBatchSamples = STANDARD_WINDOW_SIZE / 2 - 13;
        var offset = BATCH_SIZE_BYTES - bytesForPowerSamples(firstBatchSamples);
        var sampleBytes = Base64.getDecoder().decode(PowerComputerTest.SAMPLES_BIN_BASE64);
        System.arraycopy(sampleBytes, 0, bytes, offset, sampleBytes.length);
        try (var s = new ByteArrayInputStream(bytes)) {
            var w = new PowerWindow(s, STANDARD_WINDOW_SIZE);
            w.advanceBy(BATCH_SIZE - firstBatchSamples);
            for (int i = 0; i < STANDARD_WINDOW_SIZE; i += 1)
                assertEquals(PowerComputerTest.POWER_SAMPLES[i], w.get(i));
        }
    }

    private static byte[] bytesForZeroSamples(int batchesCount) {
        var bytes = new byte[BATCH_SIZE_BYTES * batchesCount];

        var msbBias = BIAS >> Byte.SIZE;
        var lsbBias = BIAS & ((1 << Byte.SIZE) - 1);
        for (var i = 0; i < bytes.length; i += 2) {
            bytes[i] = (byte) lsbBias;
            bytes[i + 1] = (byte) msbBias;
        }
        return bytes;
    }
}