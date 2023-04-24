package ch.epfl.javions.aircraft;

import java.io.*;

import java.util.Objects;
import java.util.zip.ZipFile;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Reprensents the database for the different aircrafts information
 * @author: Sofia Henriques Garfo (346298)
 * @author: Romeo Maignal (360568)
 */
public final class AircraftDatabase {
    private static final int REGIS_INDEX = 1;
    private static final int TYPE_DESI_INDEX = 2;
    private static final int MODEL_INDEX = 3;
    private static final int DESCR_INDEX = 4;
    private static final int WAKE_TUR_INDEX = 5;
    final private String fileName;

    /**
     * Constructor that returns an object representing the mictronics database, stored in the file of given name
     * @param fileName source file name
     * @throws NullPointerException if file is null.
     */
    public AircraftDatabase(String fileName) throws NullPointerException {
        Objects.requireNonNull(fileName);
        this.fileName = fileName;

    }


    /**
     * Searches the database for the aicraft information that matches the given Icao address
     *
     * @param address (IcaoAddress)
     * @return (AircraftData)
     * @throws IOException if input/output error occurs
     */
    public AircraftData get(IcaoAddress address) throws IOException {
        String adressString = address.string();
        String line;
        String fileCSV = adressString.substring(adressString.length() - 2) + ".csv";
        try (ZipFile dataBase = new ZipFile(fileName);
             InputStream stream = dataBase.getInputStream(dataBase.getEntry(fileCSV));
             Reader reader = new InputStreamReader(stream, UTF_8);
             BufferedReader buffer = new BufferedReader(reader)) {

            while ( (line= buffer.readLine()) != null){
                if (adressString.compareTo(line) < 0) {
                    if (line.startsWith(adressString)) {
                        String[] data = line.split(",", -1);
                        return new AircraftData(
                                new AircraftRegistration(data[REGIS_INDEX]),
                                new AircraftTypeDesignator(data[TYPE_DESI_INDEX]),
                                data[MODEL_INDEX],
                                new AircraftDescription(data[DESCR_INDEX]),
                                WakeTurbulenceCategory.of(data[WAKE_TUR_INDEX]));
                    } else return null;
                }

            } return null;

        }
    }


}



