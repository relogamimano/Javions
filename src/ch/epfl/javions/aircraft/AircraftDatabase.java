package ch.epfl.javions.aircraft;

import java.io.*;

import java.util.zip.ZipFile;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * * @author: Sofia Henriques Garfo (346298)
 *  * @author: Romeo Maignal (360568)
 */
public final class AircraftDatabase {
    String fileName;

    public AircraftDatabase(String fileName){
            if (fileName.isEmpty())
                throw new NullPointerException();
            else {
                this.fileName = fileName;
            }
        }


    /**
     * Searches the data base for the aicraft information that matches the given Icao address
     * @param address       (IcaoAddress)
     *
     * @return              (AircraftData)
     * @throws IOException
     */
    public AircraftData get(IcaoAddress address) throws IOException {
        String adressString = address.string();
        String line;
        String fileCSV = adressString.substring(adressString.length()-2)+".csv";
        try (ZipFile dataBase = new ZipFile(fileName);
             InputStream stream = dataBase.getInputStream(dataBase.getEntry(fileCSV));
             Reader reader = new InputStreamReader(stream, UTF_8);
             BufferedReader buffer = new BufferedReader(reader)){

            while ( (line= buffer.readLine()) != null){
                if (adressString.compareTo(line) < 0) {
                    break;
                }
            }
            assert line != null;
            if (line.startsWith(adressString)) {
                String[] data = line.split(",",-1);
                AircraftData aircraftData = new AircraftData(new AircraftRegistration(data[1]),
                        new AircraftTypeDesignator(data[2]), data[3], new AircraftDescription(data[4]),
                        WakeTurbulenceCategory.of(data[5]));
                return aircraftData;
            } else return null;
        }
    }


}
