package ch.epfl.javions.demodulationTest;

import ch.epfl.javions.demodulation.PowerComputer;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class PowerComputerTest {
    //<editor-fold desc="Test data">
    // The given samples.bin file, base64-encoded
    static final String SAMPLES_BIN_BASE64 = """
            /QcICPcH+Af7B/gH9AfwB+kH9wfvB74HBgglCOsHvwf/BxMI+gfiB9sH9QfzBwII8wcBCPsHBAjuB/YH
            +gf+B/AH7wfuB/EH/gfwB/UH+wfqB/MH7wfyB/UH7QfwB/YH8Af6B/oH9gfwB+sH+wf0B/IH9AfzB/EH
            CAjzB70HAAgdCPgHxwf8Bw8IDQjvB+4H7AfnB+sH8gf1B/MH9AcFCAgI1gfWBxgIGQjbB9MHEggVCPcH
            7AfnB+AH9Af7B/YH+Af0B/cH8wf2B+kH6wfyBwEI9Af0B/YH8wfxB/YH+wf3B/kH/Qf1B/cH8Qf+B+8H
            9gftB+sH9gf0B/IH+AfzB/gH9wfzB/cH7gfsB/cH6wfwB/4H8wf0B/AH+QfyB+4H9Af1B/oH9QfuB+4H
            CwjbB8IHEggjCPgHzwcACBIIBgjpB+IH7AftB+0H9Qf1B/oH9gfvB/kH9gf1B/QH7QftB/YH9wf8B/IH
            2gftBzAI+gfKB+AHGAgCCNoH2gf0B/oH+wcACP0H/wf8B/IH6QfxBwAI8Ae+B/oHHwj8B8UH5QcVCAwI
            /gfoB/IH4gftB+8H5gf7B/MH7QcWCO4HwQf5By0I8QfQB/QHLwgACMgH8gcUCAIIwQf2ByYIAgjKB9oH
            8Af2B/sH+Af2B/cH9Qf1B/YH/AfoB80H9AchCP0Hxwf6BxwICwjcB9wH8AfyB/8H+wf6B/MH9wf+B/sH
            9gf/B+sH/QcCCPcH7wfuB/AH9wf5B8wH3gcgCAcIyAffBxsIBAjXB9kHHQgLCMQH2AccCAsI3AffB/0H
            /Af/B/YH/wfzB/UH9Af2B/MH9Qf1B/8H6wfsBwAI9Qf1B/YH9QcBCOYHyAcGCCsI4QfBB/8HJQjgB70H
            AAgqCO0Hxgf9ByEI4wfJB+wHCggRCAQI/gfzB/AH8gfxB+UH7wf1B/0H7wf9B/oH+QfzB+gH+Af3B/oH
            AQjfB9kHDwgVCNMH2AcICBYI/wfiB9sH7Qf2B/kH7Af8B+4H+Af2B+0HDwgaCNcH1QcBCBsI5QfLB+gH
            AggACP8H+wfxB/wH7gf3B/kH3wfhBx4ICwjCB9YHHAgICMgH4QcbCBMIxQfdByMICAjMB+YHIggGCO4H
            4wfxB+8H+Af0B/cH9gfuB/wH8gf9B/oH9AftB+MH+AcACPkH7gftBwUICAjIB+QHIAgJCMUH1AcMCAUI
            9gfvB/EH6gfxB+kH+Af0B/gH9AflB+QHHwgVCMkH4gcmCA0I0gfpBxwI/wfFB+QHHAgECMMH5wcOCBgI
            8AfsB+cH6gf0B+oH+wf4B/kH6wf/Bx8I3AfPBwgIHwjlB+EHAQgICAQI+Qf3B+gH9AfrB/EH6QfvB/IH
            9gfsB/sH7Af6B/MH9gf8B/oH7QfsBx0I7AfBB/oHHAjzB8sH8AcQCA4I7wfvB/IH9wfzB+cH5gftB/4H
            9AfMB/cHLQgBCM0H7QclCPYHxQf7ByYI7gfCB/0HHgjzB8YH+AcNCAsI8Qf3B+oH6gfyB+8H+Af0B+gH
            4AcFCB4IyQfRBxUIBwjYB9sHCQgOCA4I/Qf/B/EH7wfyB/kH9QfzB98HFAj8B7oH1gcVCA8I0gfeBwEI
            CQj+B/4H9wf0B/AH8gfzB/QH8QfxB/AH7QftB/sH/Af4B/sH8wf7B9sH6wcwCAMIvAfcBywI+gfSB94H
            +AcBCPUHAQj9B/YH8Qf2B+gH6wcHCPoHvgfnBxUIAgjKB+wHJgj4B8YH8wcjCPwHzQf9BxsI9wfTB+kH
            9Qf+B/YH+QfyB/UHAQj9B+4H9gfkB8IHEAgiCNYH1gcLCBoI8gfSB9sH9gf6B/sH/wftB/kH+QcDCPQH
            9wfuB/8H7Qf2B/AH8Af7B+8H+Qf+B8cH2QcwCAUIygfkByEI9gfEB98HKggACMYH7gcmCPIH1AfnBwsI
            Dwj1B/oH+Qf5B/EH9wfqB+0H+gfxBwAI8wf2B+4H+Qf3B/cH9AftBwoI3QfNBxYIIQjeB80HDQgaCPYH
            6AfnB+wH8gfwB+4H/wf3B/sH+QfeBxQIHgjYB9MHDggcCNgH0gfzBwQIAAj8B/gH7QftB+0H8QfpB/QH
            3AcICB4I1AfNBxAIFQjdB9wH9Af/BwEIBAj3B/EH8wfzB+kH8gftB9QHFwgYCNAHzQcOCA4I0AfaBx4I
            EAjOB9MHIggRCMwH4QcXCAoI5wfrB+4H+QfrB/MH+wfmB/YH+AcECPoH/QcACPEH/QfzB/IH6QfxB+MH
            BwgGCL8H4gcjCAQI0gflByMIAQi/B+4HGAj6B8sH8gcWCPgH0gfgB/MHBggOCPYH/QcBCPUH9Qf+B+wH
            6AccCPYHwAfqBxEI7AfSB+IHBggNCO4H+Af6B+0H8QfxB+YH8AfrB/YH/wf1B/UH/Qf6B/cH9wf0B+8H
            7QcdCA0ItgfjByEI/wfOB+MHAggFCPMH/gfsB/EH/QfzB+oH9gf2B+8HvAf0ByEI6gfLB/EHHwj9B+MH
            4Af5B/QHAQgACAII9wf2B/MHBAjgB9UHGwgVCNkH1gcXCBEIvwfYBxwIFAjMB9QHIAgMCMMH1AcNCBcI
            +wfvB+4H/AflB+0H/QfvB/MH7wf1B/kH+Qf/B/kH/QfyB/0H+wfyB/gHFggCCLkH5QcfCOgHuQfrBw8I
            7QfGB/sHGwjtB8AH/QceCPcH3gfhB/gH9wf4B/kH/QcACPYHAgj7BwEI9Qf5B/cH5Qf0B/YH8Qf3B+sH
            5wcYCAAIuAfdBxoIAwjHB+gH+gcJCPgH+Af0B/QH+gf8B+8H+gf8B/8HywfnBy8I6wfJB/sHGwgECOAH
            1QfjB/cHAQjoB/QHAwj5B/YHCAjrB8kHDAg1CPEH0QcECB4I0gfLBwoIGQjhB8YHEAgWCNQHwwcBCA4I
            Awj3B+kH6AfxB+0H9wf0B/4H9Af0BxQIEQjOB9EHHwgaCNoH2Af0Bw0I9AcDCPgH9AcBCPIH7Qf2B+sH
            9wceCNgHuQcACBsI4wfCB/0HCQgACO8H9gf8B/sH6gfyB/IH8QfoB/sH+gf1B+4H+Qf3B/0H7wfzB/EH
            8gcfCAMIugfoByAI/QfJB94HBAgACAII8gf+B/IH+AfxB/AH6gfxBwQI1wfOBxUIHwjPB9AHFggQCPQH
            4AfoB+oHAQj8B/IH9Af8B/QHBQgQCOUH2gceCBMI0gfVBw8IDgjqB9sH6AfsB+8H+wf2B+sH8gf2B+8H
            CgjTB9QHGAgYCNUH2QckCA4IyAfYBygIEQjQB94HKAj6B80H4Af4BxII9Qf8B/YH8wfxB/UH9wf0B/cH
            2wfjByMIBQjAB+UHJwgBCNkH3wf1B/YHBwgDCO8H9gfxB/sH8gf6B/IH8gfoB/QH+QftB/cH8Af1BwAI
            8wfLB/0HLAj0B8QHBQgcCPcH2gflB+8HBAj0B/oH+gf4B/8H+AfrB/MHGQjhB8UHBggfCNsH0Af7Bw0I
            /gf6B+8H9Qf5B+wH8QfsB/IH6wcICAQIygfSBxoI/wfYB+cHGQgLCP8H5QfvB+4H9QftB/AH7QfxB+oH
            AggMCMoH2AcXCAoI0gfcBxIICggCCO0H8wfuB+4H7gfxB/cH8QfpB/wHGAjSB8MHGggjCN4H1QcaCCUI
            0QfQBx0IFAjSB9MHEQgMCN8H3QfrBwEI8QfxB/kH7QfyB/YH+gfqB/cH9wfqB/cH8QfxB/oH9wfxB/QH
            BgjZB88HHggNCNIHzQcXCAsI2AfeByEICgjNB+AHGwgRCMwH7Qf3BwIIBQj4B/cH9gf0B/oH4gfqB+QH
            CwjoB7wH9AcfCPQHwgfxBxYICAjpB+kH8AfvB/QH9QfxB/UH+wf9B/4H8gf5B/8HAgj8B/IH9gftB+gH
            AQj0B8MH4gcmCP8HugfuBwsIDAjuB/gH5wfyB/MH9AfwB/MH8gfzB9wH+gcmCOsH0QcGCBsI5QfdB/IH
            BAj8B/kH/wf2B/kH6wf1B/MH6wfdBw8IEgjPB8gHEggGCNYH2Af0Bw0IAAgCCPkHAAjzB+EH8gf0B/YH
            2gcACCMIyQfLBwUIEQjQB9cHIQgUCM4H1AcnCAsI0gfaBx0ICAjyB+oH5gfvB/gH9gf0B/YH+Qf1B+sH
            9AfrB/cH7Qf7B/kH+Af1B+0H6QcDCAcIvwfbByMIDgjOB+AHCwgVCAUI5gfpB+sH+wfrB/kH7AfsB/QH
            5gfhByMIAgjMB+UHIgj9B8gH+QchCAAIyQf6Bx4I9QfNB/QHEAgDCOEH7AftB+0H7wfnB/IH8Af9B/MH
            +gf5B/cH8wfvB/QH+gf4B/8H7gcFCAgIvwfdBygICQjOB90HIQgLCMkH5gcVCAYIygflBxAIAQjVB+IH
            9QcFCP8H+Qf4B/wHBgjmB/cH8Qf4B+8H9AfyB/QH7wfrB/oH9wfwBwoIBgi8B+oHHAgVCMUH4gcVCBAI
            9AfqB+0H8QfzB/IH8gf5B/sH7gfhB+4HLQj7B8UH/QceCOkH3gfbB/YHBQj2B/0H9wcDCAAI8wfzB+sH
            4AcICBYI0ge/BxgIHgjKB88HEggYCNYHzAceCBcI2gfSBw8IBQj2B98H8QflB/gH7gfyB/0HAAjqB+0H
            AwgXCM0HzgccCA8I1AfSBw8IAgj7B/EH7AfsB/EH8AfuB/EH8gfwB/IH8gf5B/wH8wfvBwMIAwj2B+8H
            +QcXCNgHygcZCBMI1gfZBwEIEAjxB/EH9AfqB/YH4wfuB/UH+gfxB9IH8wcgCPwHuwf2Bx4I8AfFB/gH
            Kwj0B8YH9QcjCO4Hywf9BxgI/wfoB+4H6wfwB/AH9gf5B/EH6AfyB/kH/AfyB/YH6wf3B/AH+gf7B+wH
            CAgdCM0H2QcYCBEIzQfiByEIEgjJB+MHFQgKCMgH2QcpCAkI2gfcB+8H+gf+B/EH9Qf9B/YH9gf3B+0H
            EAgCCM8H3gcYCBgIyAfjBwUIEQj8B/UH7QfvB/EH9AfpB+wH7Af8B/UH9gf5B/kH8gftB/UH/Af1B/QH
            +wcXCOsHuAf8Bx8I+AfFB/gHEgjtB8kH/QcmCOcHzAcGCCAI9QfaB90H9Af3B/IH9QcACPgH+wf4B/gH
            8QceCAEIxAfkBx4IAAjHB+YH/gcICPUH+wfyB/cH4wfnBwMI+gfwB/kH7gf/B/cH/Af9B+oH/wfwB+wH
            5gcYCAQIygfjBxwIAwjOB+AH+wcOCAMI9gfyB/kH5wfuB+kH7gcFCPcHzgf3BysI8gfIB/sHKAjnB9AH
            9wchCOQHxQf6ByII9AfIB/UHCggMCPcH9Qf5B/cH/AfrB+4H9gfzB9IH/wclCNQHuwf6BxcI7QfVB+oH
            Awj6BwYIBAj4B/sH/wf9B/AH5wfmByQI9AerB+0HIwjmB8gH9Qf/BwgIAgj2B/MH7wfsB+wH8Af3B/cH
            6QccCAMItwfrBxsIBgjHB+QH9gcLCAAI/AfxBwEI/gfwB+4H7gf0B/QH8wf0B/AH9Af1B/IH/Qf6B/YH
            2wfuBy0I9wfAB/IHKAjqB8MH+wcfCO0HyQcECCII6AfIBwAIGAgQCPMH1wfyB+wH7gfvB/MH9wf5B/IH
            8wf5B/gH9Qf2B/EH9AcICPcH5wfdB+0HNwjgB8cH+AceCOcH2wfrB/sH/wfyB/oH/Af5B/QH8AfsB/QH
            AwjpB8kHBQgYCOQHyAcQCCMI4wfEBxYIHAjjB8oHDwgeCN8H2gfsB/gH/wcDCPIH9gf1B/QH8wf2B/YH
            2wfmBzII+gfCB+sHKQj9B9cH1AfsB/0H9AcECPsHAQjyB/gH8wfxB/sH7AfvB+0H/gfsB/IH7gfrB/cH
            5gfXBywICgjXB9cHGQgaCMMH5wcTCA4I0gfdBxMIEgi9B+IHAAgECPwH8wf6B/wH9AfjB+8H5wfuB/YH
            7gfjBxIIFgjaB9MHGQgbCOAH1QfjB/YH9wfzB/sH9Qf5BwEI7wfyB+wH9AfxB/wH8QfqB+8H8wfwBwII
            4wfPBxsIFgjZB8sHFQgRCNAH1AcVCA8I2QfVBxEIFAjQB9cHBwgYCAII+wfvB+wH+gf0B/EH9gf1B/0H
            5Qf4B+oH6wfwB+oH/wfzB/kH+AfjB+QHJggOCNgH1AcXCBEI3gfiB+8H/wcACPsH9gf7B/EH9Af3B/MH
            AggICLgH8gchCOoHxgf6BxwI8AfDB/sHHQjsB8wH9gciCPYH2gfpB/gH/wf7B/wH9wcBCP8H7Af1B+8H
            9QfuB/8H8gfxB/YH7wf4B+UH8gfdB/QHHAjyB8MH9gcfCO4HxwfzByMI/AfKB/kHLAj5B7sH9QcXCAcI
            9QfsB+sH6wftB+kH5gf3B+UHBgjbB9UHHAgcCNIHzgcPCBsI5wfaB+oH8gf4B/kHAAj6B/YH+QfxB+sH
            AQjtBwQI4wfxB/IH6gf4B/cHAQjlB94HKggHCLkH5AcQCBMI2QfcB/wH8wcMCP8H+gcFCPwH8wf0B/UH
            7wfvBw==
            """.replace("\n", "");

    // The first 1200 power samples corresponding to samples.bin
    static final int[] POWER_SAMPLES = new int[]{73, 292, 65,
            745, 98, 4226, 12244, 25722, 36818, 23825, 10730, 1657, 1285,
            1280, 394, 521, 1370, 200, 292, 290, 106, 116, 194, 64, 37, 50,
            149, 466, 482, 180, 148, 5576, 13725, 26210, 28305, 14653, 4861,
            1489, 85, 845, 3016, 9657, 19233, 29041, 25433, 13842, 3112,
            392, 346, 677, 160, 208, 505, 697, 450, 244, 49, 117, 61, 205,
            232, 65, 37, 149, 81, 2, 74, 17, 208, 265, 676, 466, 145, 185,
            100, 1586, 9529, 17901, 28618, 27296, 16409, 5189, 2384, 377,
            13, 265, 178, 25, 89, 148, 650, 8528, 19457, 29105, 31252,
            15172, 4181, 1745, 85, 293, 680, 7306, 14401, 24505, 33010,
            16250, 5713, 1313, 397, 65, 953, 5193, 20498, 31880, 41225,
            38537, 30025, 35272, 32113, 33329, 20921, 8005, 1818, 100, 52,
            2626, 8450, 18794, 28642, 24392, 13525, 3922, 1669, 340, 401,
            257, 4, 229, 1021, 585, 2804, 9325, 19661, 32378, 30420, 30298,
            32218, 33800, 33610, 23666, 10244, 3589, 740, 26, 137, 130, 521,
            890, 765, 841, 3812, 13124, 29273, 43445, 48724, 47525, 44116,
            42304, 37369, 24961, 8957, 2404, 578, 1224, 481, 586, 733, 269,
            545, 146, 533, 3125, 12469, 20402, 19645, 13058, 5653, 3716,
            1037, 68, 793, 3985, 10889, 20281, 24226, 15641, 6893, 2306,
            136, 538, 4093, 16265, 29338, 39440, 38393, 38900, 39978, 38578,
            40144, 24946, 12682, 3961, 904, 200, 130, 10, 90, 617, 544,
            1226, 3488, 8545, 16417, 31025, 32677, 18394, 6292, 1013, 40, 1,
            169, 5765, 15514, 31265, 36469, 32114, 33210, 28837, 34112,
            32805, 19490, 6565, 2225, 148, 290, 4525, 10018, 19924, 25064,
            19345, 6953, 709, 144, 617, 169, 80, 90, 37, 82, 73, 3573,
            11465, 19321, 29978, 25721, 13525, 6340, 2468, 2309, 821, 4050,
            14722, 21605, 34514, 37225, 34450, 38432, 36121, 35873, 26969,
            13537, 4321, 436, 226, 128, 1450, 10265, 20434, 28097, 30266,
            14161, 4877, 1642, 281, 565, 2290, 10240, 18976, 32045, 26613,
            14900, 5200, 1189, 185, 145, 29, 29, 317, 65, 306, 1018, 6976,
            22625, 41725, 46925, 26725, 9433, 2692, 225, 485, 586, 7624,
            15688, 23585, 33562, 29970, 34666, 33140, 29250, 25133, 12769,
            5402, 1189, 265, 650, 1961, 8546, 21008, 36305, 28793, 11716,
            4525, 808, 914, 586, 360, 360, 450, 104, 101, 1602, 12053,
            26585, 41764, 41869, 38857, 39850, 40885, 34493, 23642, 12752,
            4505, 2421, 538, 185, 5, 100, 82, 265, 113, 1285, 6305, 16420,
            31841, 32162, 18980, 6705, 2477, 761, 4, 1492, 6964, 17377,
            31765, 28762, 16810, 7085, 1229, 80, 425, 745, 6245, 15236,
            29653, 25933, 12893, 2873, 337, 449, 400, 1808, 8978, 21841,
            35378, 35729, 34325, 39013, 41012, 37162, 28565, 12965, 5626,
            1898, 65, 1044, 265, 580, 26, 509, 32, 746, 7093, 19645, 27898,
            36605, 35261, 30500, 32058, 27220, 22049, 13600, 3281, 2657,
            1145, 68, 2196, 10730, 17797, 24281, 18173, 9594, 5057, 2113,
            290, 160, 181, 29, 821, 218, 34, 2018, 13162, 25810, 38196,
            30181, 13448, 3250, 445, 701, 941, 5954, 12833, 21025, 34369,
            21320, 11720, 3728, 818, 137, 289, 4265, 9109, 19325, 29770,
            38116, 38186, 42865, 44785, 44329, 40093, 21537, 10121, 3754,
            346, 586, 820, 72, 68, 45, 250, 1396, 11177, 20068, 38194,
            36020, 30650, 29530, 27040, 32941, 24425, 14436, 4352, 1361,
            890, 116, 205, 85, 522, 82, 442, 1765, 11080, 23050, 36010,
            26045, 12266, 4145, 857, 306, 397, 3769, 13445, 22882, 33188,
            26090, 8186, 2690, 3944, 4946, 788, 3620, 15529, 28013, 36929,
            39418, 29650, 35837, 35573, 37757, 32489, 14600, 3961, 424, 464,
            250, 1537, 6196, 18245, 29725, 28349, 20637, 6676, 1525, 400,
            170, 962, 11338, 23882, 39700, 32917, 13901, 7312, 2000, 802,
            800, 677, 533, 925, 404, 157, 1453, 9850, 22688, 36788, 29285,
            10946, 5050, 1220, 369, 74, 1781, 7690, 20665, 35378, 31841,
            19610, 5557, 1629, 1229, 360, 884, 7272, 18889, 27380, 25765,
            16250, 6376, 1321, 10, 692, 1040, 6205, 16769, 35593, 39922,
            44314, 44500, 44881, 39266, 23090, 14920, 4240, 1745, 449, 125,
            725, 6197, 18080, 34469, 35685, 19762, 4041, 116, 724, 725, 13,
            82, 514, 169, 306, 2516, 12833, 24770, 34954, 29429, 11882,
            4010, 1513, 941, 296, 2650, 11140, 19520, 30290, 27562, 11204,
            3425, 953, 554, 221, 821, 6322, 15304, 21860, 27586, 12308,
            4426, 1921, 522, 65, 1300, 6050, 15892, 25229, 29097, 12850,
            4610, 1282, 244, 221, 3332, 11565, 27338, 36973, 48400, 44258,
            40400, 44404, 34946, 28169, 14274, 5905, 1424, 50, 512, 584,
            169, 65, 52, 85, 1341, 8452, 17498, 33205, 34697, 32045, 32845,
            30370, 35218, 20725, 9125, 2957, 482, 1060, 325, 1565, 9221,
            17440, 29665, 34018, 20200, 9122, 2306, 52, 392, 121, 466, 340,
            490, 305, 52, 5513, 15842, 30125, 35881, 20138, 6052, 613, 149,
            200, 661, 6001, 12605, 24208, 23209, 13298, 5585, 1312, 485,
            281, 2113, 9197, 19825, 28601, 23809, 15529, 3778, 2637, 1828,
            293, 3330, 11065, 17896, 32113, 41197, 35657, 43784, 41680,
            39665, 26585, 10210, 3226, 785, 554, 360, 26, 148, 153, 4, 49,
            1037, 9377, 21445, 31509, 36737, 16930, 4453, 2353, 936, 89,
            388, 3985, 10953, 24858, 32418, 31586, 31784, 30280, 28610,
            23204, 16960, 6497, 2866, 325, 178, 225, 73, 170, 68, 90, 1189,
            7034, 17221, 33169, 43444, 38245, 31365, 31469, 24916, 22052,
            14373, 4250, 2069, 1732, 680, 801, 557, 26, 365, 313, 1189,
            5800, 17450, 33466, 38897, 22321, 8138, 2098, 260, 325, 778,
            7229, 16904, 27274, 28368, 13253, 4580, 2929, 657, 148, 269,
            5525, 21037, 39625, 47720, 47268, 44761, 38288, 36650, 24525,
            13753, 3573, 962, 882, 725, 4736, 13793, 22265, 33832, 31813,
            14548, 3785, 394, 340, 90, 18, 202, 157, 1429, 1450, 3961,
            11525, 17393, 28225, 28048, 14810, 4420, 1640, 146, 317, 2704,
            8125, 19897, 31338, 36104, 40501, 36181, 38146, 35977, 28925,
            18500, 6416, 1970, 153, 193, 701, 601, 221, 13, 185, 3028, 9028,
            18769, 31181, 36097, 35405, 33169, 36441, 37498, 33172, 20008,
            7012, 1908, 512, 73, 872, 6121, 15845, 28946, 28772, 14482,
            3716, 554, 725, 490, 197, 410, 328, 373, 449, 2581, 10865,
            19573, 34625, 32689, 26794, 29156, 27658, 33538, 27121, 12149,
            5265, 2276, 890, 125, 1105, 8905, 17680, 33265, 24050, 13060,
            4225, 1476, 1682, 1381, 1189, 725, 32, 265, 178, 2900, 10705,
            17725, 28180, 20753, 10984, 4265, 2410, 1450, 298, 2813, 14641,
            23732, 35802, 35594, 31954, 33921, 31720, 33749, 25810, 12170,
            5108, 881, 1369, 233, 2308, 10280, 22664, 33761, 30809, 19053,
            4018, 1073, 244, 1802, 3978, 20450, 33050, 44993, 30689, 7888,
            1609, 160, 562, 16, 1970, 10205, 20213, 37034, 24973, 9832,
            3425, 2465, 1985, 370, 410, 13, 52, 29, 68, 1089, 8104, 19769,
            34597, 44244, 38900, 36569, 34525, 33185, 28705, 17218, 7610,
            4597, 857, 8, 226, 202, 73, 370, 1370, 1625, 10765, 19944,
            32229, 32825, 14170, 5777, 2125, 485, 13, 625, 5380, 12340,
            24228, 34984, 39650, 41257, 40354, 39385, 25749, 13837, 3249,
            773, 306, 157, 1165, 8450, 20282, 37544, 40772, 18857, 6632,
            2228, 5, 164, 197, 157, 565, 585, 128, 1189, 7569, 14989, 32420,
            39645, 32552, 32801, 29489, 32210, 24993, 12325, 6074, 1864,
            218, 149, 250, 5581, 10532, 24930, 31538, 15381, 6698, 1274,
            405, 205, 221, 74, 265, 298, 10, 1989, 10645, 22117, 33337,
            36721, 33282, 30802, 30509, 29786, 28705, 13000, 3796, 477, 953,
            450, 634, 117, 136, 416, 16, 170, 5930, 16084, 27509, 29250,
            14500, 4201, 890, 265, 17, 232, 5153, 13625, 27261, 36805,
            33129, 31601, 29341, 31201, 23418, 13940, 5017, 1405, 697, 260,
            548, 49, 205, 100, 580, 116, 2873, 9445, 24125, 31410, 33857,
            31333, 36181, 40853, 36424, 22570, 7013, 1865, 458, 170, 1514,
            11492, 23185, 37780, 33445, 14297, 4040, 746, 52, 178, 37, 580,
            298, 32, 612, 1466, 9224, 22061, 32080, 36324, 19784, 3370,
            1130, 360, 740};
    //</editor-fold>

    static final Base64.Decoder B64_DECODER = Base64.getDecoder();

    static InputStream getSamplesStream() {
        return new ByteArrayInputStream(B64_DECODER.decode(SAMPLES_BIN_BASE64));
    }

    @Test
    void powerComputerConstructorThrowsOnZeroBatchSize() {
        assertThrows(IllegalArgumentException.class, () -> {
            try (var s = new ByteArrayInputStream(new byte[0])) {
                new PowerComputer(s, 0);
            }
        });
    }

    @Test
    void powerComputerConstructorThrowsOnInvalidBatchSize() {
        assertThrows(IllegalArgumentException.class, () -> {
            try (var s = new ByteArrayInputStream(new byte[0])) {
                new PowerComputer(s, 7);
            }
        });
    }

    @Test
    void powerComputerReadBatchWorksOnGivenSamples() throws IOException {
        try (var samplesStream = getSamplesStream()) {
            var batch = new int[1200];
            var powerComputer = new PowerComputer(samplesStream, batch.length);
            var read = powerComputer.readBatch(batch);
            assertEquals(batch.length, read);
            assertArrayEquals(POWER_SAMPLES, batch);
        }
    }

    @Test
    void powerComputerReadBatchWorksWithAnyBatchSize() throws IOException {
        var maxBatchSize = 1024;
        var expectedSamples = Arrays.copyOf(POWER_SAMPLES, maxBatchSize);
        for (int batchSize = 8; batchSize <= maxBatchSize; batchSize <<= 1) {
            try (var samplesStream = getSamplesStream()) {
                var actualSamples = new int[maxBatchSize];
                var batch = new int[batchSize];
                var powerComputer = new PowerComputer(samplesStream, batchSize);
                for (int i = 0; i < maxBatchSize / batchSize; i += 1) {
                    var read = powerComputer.readBatch(batch);
                    assertEquals(batchSize, read);
                    System.arraycopy(batch, 0, actualSamples, i * batchSize, batchSize);
                }
                assertArrayEquals(expectedSamples, actualSamples);
            }
        }
    }
}
