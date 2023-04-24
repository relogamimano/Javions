package ch.epfl.javions.aircraft;

/**
 * Category of turbulence
 * * @author: Sofia Henriques Garfo (346298)
 *  * @author: Romeo Maignal (360568)
 */
public enum WakeTurbulenceCategory {
    LIGHT, MEDIUM , HEAVY, UNKNOWN;

    /**
     * Converts the turbulence  information of the data base
     * @param s value on the data base
     * @return enum turbulence
     *
     */
    public static WakeTurbulenceCategory of(String s){
        return switch (s) {
            case "L" -> LIGHT;
            case "M" -> MEDIUM;
            case "H" -> HEAVY;
            default -> UNKNOWN;
        };
    }
}
