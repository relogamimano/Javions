package ch.epfl.javions;

public class WebMercator {
    public static double x(int zoomLevel, double longitude) {
        return Math.scalb(Units.convertTo(longitude, Units.Angle.TURN) + 0.5, 8 + zoomLevel);
    }
    public static double y(int zoomLevel, double latitude) {
        return Math.scalb(-Units.convertTo(Math.asin(Math.tan(latitude)), Units.Angle.TURN) + 0.5, 8 + zoomLevel);
    }
}
