package ch.epfl.javions.aircraft;

public enum WakeTurbulenceCategory {
    LIGHT, MEDIUM , HEAVY, UNKNOWN;

    public static WakeTurbulenceCategory of(String s){
        switch (s){
            case "L":
                return LIGHT;
            case "M":
                return MEDIUM;
            case "H":
                return HEAVY;
            default:
                return UNKNOWN;
        }
    }
}
