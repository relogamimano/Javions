package ch.epfl.javions.aircraft;

/**
 * Répresente la catégorie de turbulence d'un aéronef
 */
public enum WakeTurbulenceCategory {
    LIGHT, MEDIUM , HEAVY, UNKNOWN;

    /**
     * Converts les valeurs de turbulance de la base de données
     * @param s
     *          valeur dans la base de données
     * @return
     *          turbulence en type énuméré
     */
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
