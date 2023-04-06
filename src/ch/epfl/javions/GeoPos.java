package ch.epfl.javions;

/**
 * Represent the geographical coordinates expressed in T32 and stocked as int
 * @param longitudeT32
 * @param latitudeT32
 * @author: Sofia Henriques Garfo (346298)
 * @author: Romeo Maignal (360568)
 */
public record GeoPos(int longitudeT32, int latitudeT32) {

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
        return (latitudeT32 >= (-Math.pow(2, 30))) && (latitudeT32 <= Math.pow(2, 30));
    }

    /**
     * Converts the longitude to radians
     * @return longitude expressed in radians
     */
    public double longitude() {

        return Units.convertFrom(longitudeT32, Units.Angle.T32);
    }

    /**
     * Converts the latitude to radians
     * @return latitude expressed in radians
     */
    public double latitude () {

        return Units.convertFrom(latitudeT32, Units.Angle.T32);
    }


    @Override
    public String toString() {

        double longitudeDEG = Units.convert(longitudeT32, Units.Angle.T32, Units.Angle.DEGREE);
        double latitudeDEG = Units.convert(latitudeT32, Units.Angle.T32, Units.Angle.DEGREE);

        return "(" + longitudeDEG + "°, " + latitudeDEG + "°)";
    }
}
