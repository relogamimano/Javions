package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;

import static ch.epfl.javions.GeoPos.isValidLatitudeT32;
import static ch.epfl.javions.Units.convertFrom;
import static java.lang.Double.isNaN;
import static java.lang.Math.*;

/**
 * Decodes the CPR position
 *
 * @author: Sofia Henriques Garfo (346298)
 * @author: Romeo Maignal (360568)
 */
public class CprDecoder {
    private final static double LAT_ODD_ZONES = 59;
    private final static double LAT_EVEN_ZONES = 60;
    private final static double WIDTH_LAT_EVEN = 1. / LAT_EVEN_ZONES;
    private final static double WIDTH_LAT_ODD = 1. / LAT_ODD_ZONES;

    private CprDecoder() {
    }

    /**
     * Calculates the geographic position of the aircraft
     *
     * @param x0 normalized longitude of an even message
     * @param y0 normalized latitude of an even message
     * @param x1 normalized longitude of an odd message
     * @param y1 normalized latitude of an odd message
     * @param mostRecent 0 if the most recent position was even or 1 if it was odd
     * @return GeoPos geographic position
     * or null if the position isn't valid
     * @throws IllegalArgumentException if most recent isn't 0 or 1
     */
    public static GeoPos decodePosition(double x0, double y0, double x1, double y1, int mostRecent) {
        Preconditions.checkArgument(mostRecent == 0 || mostRecent == 1);

        double zOdd, zEven;
        double xOdd, xEven;
        double lat0, lat1;
        double long0, long1;

        double latZones = rint(y0 * LAT_ODD_ZONES - y1 * LAT_EVEN_ZONES);
        if (latZones < 0) {
            zOdd = latZones + LAT_ODD_ZONES;
            zEven = latZones + LAT_EVEN_ZONES;
        } else {
            zOdd = zEven = latZones;
        }

        lat0 = (WIDTH_LAT_EVEN) * (zEven + y0);
        lat1 = (WIDTH_LAT_ODD) * (zOdd + y1);
        double longZonesEven0, longZonesEven1;

        double acosEven = a(lat0);
        longZonesEven0 = isNaN(acosEven) ? 1 : floor( 2 * PI / acosEven);

        double acosOdd = a(lat1);
        longZonesEven1 = isNaN(acosEven) ? 1 : floor( 2 * PI/ acosOdd);


        if (longZonesEven1 != longZonesEven0) {
            return null;

        } else {
            if (longZonesEven0 == 1) {
                long0 = x0;
                long1 = x1;
            } else {
                double longZonesOdd = longZonesEven0 - 1;
                double longZones = rint(x0 * longZonesOdd - x1 * longZonesEven0);
                if (longZones < 0) {
                    xEven = longZones + longZonesEven0;
                    xOdd = longZones + longZonesOdd;
                } else {
                    xEven = xOdd = longZones;
                }

                long1 = 1 / longZonesOdd * (xOdd + x1);
                long0 = 1 / longZonesEven0 * (xEven + x0);

            }
            return getGeoPos(mostRecent, lat0, lat1, long0, long1);
        }

    }

    private static GeoPos getGeoPos(int mostRecent, double lat0, double lat1, double long0, double long1) {
        int lat = (int) rint(Units.convert(recenter(mostRecent == 1 ? lat1 : lat0), Units.Angle.TURN, Units.Angle.T32));
        if (isValidLatitudeT32(lat)) {
            return new GeoPos((int) rint(Units.convert(recenter(mostRecent == 1 ? long1 : long0), Units.Angle.TURN,
                    Units.Angle.T32)), lat);
        } else return null;

    }

    //to recenter the angles
    private static double recenter(double i) {
        return i>= 0.5 ?  i-1 :  i;
    }

    // acos calculation
    private static double a(double lat) {
        lat = convertFrom(lat, Units.Angle.TURN);
        return Math.acos( 1 - ((1 - cos( ( 2 * Math.PI ) * ( WIDTH_LAT_EVEN ))) / Math.pow(cos(lat), 2)));
    }


}
