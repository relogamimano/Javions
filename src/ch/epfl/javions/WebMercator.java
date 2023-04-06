package ch.epfl.javions;

/**
 * Projections of the geographical coordinates according to the WebMercator projection
 * @author: Sofia Henriques Garfo (346298)
 * @author: Romeo Maignal (360568)
 */
public class WebMercator {

    private WebMercator() {}

    /**
     * Returns the x coordinate at the given longitude according to the zoom level
     * @param zoomLevel zoom level
     * @param longitude in radians
     * @return x coordinate
     */
    public static double x(int zoomLevel, double longitude) {
        return Math.scalb(Units.convertTo(longitude, Units.Angle.TURN) + 0.5, 8 + zoomLevel);
    }

    /**
     * returns the y coordinate at the given latitude according to the zoom level
     * @param zoomLevel zoom level 
     * @param latitude in radians
     * @return y coordinate
     */
    public static double y(int zoomLevel, double latitude) {
        return Math.scalb(-Units.convertTo(Math2.asinh(Math.tan(latitude)), Units.Angle.TURN) + 0.5, 8 + zoomLevel);
    }
}
