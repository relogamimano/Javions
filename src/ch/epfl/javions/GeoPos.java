package ch.epfl.javions;

public record GeoPos(int longitudeT32, int latitudeT32) {

    public GeoPos {
        Preconditions.checkArgument(isValidLatitudeT32(latitudeT32));
    }

    public static boolean isValidLatitudeT32(int latitudeT32) {
        return (latitudeT32 >= (-Math.pow(2, 30))) && (latitudeT32 <= Math.pow(2, 30));
    }

    public double longitude() {
        return Units.convertFrom(longitudeT32, Units.Angle.T32);
    }

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