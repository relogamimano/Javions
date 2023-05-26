package ch.epfl.javions;

import static ch.epfl.javions.Units.Angle.DEGREE;
import static ch.epfl.javions.Units.Angle.T32;
import static ch.epfl.javions.Units.convert;
import static ch.epfl.javions.Units.convertFrom;

/**
 * Represent the geographical coordinates expressed in T32 and stocked as int
 * @param longitudeT32
 * @param latitudeT32
 * @author Sofia Henriques Garfo (346298)
 * @author Romeo Maignal (360568)
 */
public record GeoPos(int longitudeT32, int latitudeT32) {
    private static final int MAX_ABSOLUTE_LATITUDE_T32 = 1 << 30;
    /**
     * Checks if the given geographical coordinates are valid
     * @throws IllegalArgumentException if the coordinates aren't valid
     * @param longitudeT32 longitude expressed in T32
     * @param latitudeT32 latitude expressed in T32
     */
    public GeoPos {
        Preconditions.checkArgument(isValidLatitudeT32(latitudeT32));
    }

    /**
     * Checks if the latitude is expressed in T32, by verifying if it's greater than -2^30 and less than 2^30
     * @param latitudeT32 latitude expressed in T32
     * @return true if the latitude is valid
     */
    public static boolean isValidLatitudeT32(int latitudeT32) {
        return Math.abs(latitudeT32) <= MAX_ABSOLUTE_LATITUDE_T32;
    }

    /**
     * Converts the longitude to radians
     * @return longitude expressed in radians
     */
    public double longitude() {

        return convertFrom(longitudeT32, T32);
    }

    /**
     * Converts the latitude to radians
     * @return latitude expressed in radians
     */
    public double latitude () {

        return convertFrom(latitudeT32, T32);
    }


    @Override
    public String toString() {

        double longitudeDEG = convert(longitudeT32, T32, DEGREE);
        double latitudeDEG = convert(latitudeT32, T32, DEGREE);

        return "(" + longitudeDEG + "°, " + latitudeDEG + "°)";
    }
}
