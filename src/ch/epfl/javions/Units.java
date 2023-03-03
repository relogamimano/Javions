package ch.epfl.javions;

/**
 * Définition des unités
 */
public final class Units {
    private Units() {}

    /**
     * Définition préfixe centi
     */
    public static final double CENTI = 1e-2;

    /**
     * défintion préfixe kilo
     */
    public static final double KILO = 1e3;
    // … autre(s) préfixe(s) SI utile(s) au projet

    /**
     * définition des unités d'angle
     */
    public static class Angle {
        /**
         * Définition d'un Radian
         */
        public static final double RADIAN = 1;
        /**
         * Définition d'un tour
         */
        public static final double TURN = 2*(Math.PI)*RADIAN;
        /**
         * définition d'un degré
         */
        public static final double DEGREE = TURN/360;
        /**
         * Définition d'un T32
         */
        public static final double T32 = TURN/(Math.pow(2,32));
    }

    /**
     * Défintion des unités de longeur
     */
    public static class Length {
        private Length() {}
        /**
         * Définition d'un mètre
         */
        public static final double METER = 1;
        /**
         * Définition d'un centimètre
         */
        public static final double CENTIMETER = CENTI * METER;
        /**
         * Définition d'un kilomètre
         */
        public static final double KILOMETER = KILO * METER;
        /**
         * défintion d'un pouce
         */
        public static final double INCH = 2.54 * CENTIMETER;
        /**
         * défintion d'un pied
         */
        public static final double FOOT = 12 * INCH;
        /**
         * défintion d'une mile nautique
         */
        public static final double NAUTICAL_MILE = 1852 * METER;
        // … autres unités de longueur
    }

    /**
     * déscription des unités de temps
     */
    public static class Time {
        private Time() {}

        /**
         * défintion d'un seconde
         */
        public static final double SECOND = 1;
        /**
         * défintion d'une minute
         */
        public static final double MINUTE = 60 * SECOND;
        /**
         * définition d'une heure
         */
        public static final double HOUR = 60 * MINUTE;
    }

    /**
     * définition des unités de vitesse
     */
    public static class Speed {
        private Speed() {}

        /**
         * défintion d'un noeud
         */
        public static final double KNOT = Length.NAUTICAL_MILE / Time.HOUR;
        /**
         * définition de kilomètre par heure
         */
        public static final double KILOMETER_PER_HOUR = Length.KILOMETER / Time.HOUR;
    }

    // … classes Time et Speed, méthodes de conversion


    /**
     * Conversion d'unités
     * @param value
     *          valeur
     * @param fromUnit
     *          unité de départ
     * @param toUnit
     *          unité d'arrivée
     * @return  valeur convertie
     *
     */
    public static double convert(double value, double fromUnit, double toUnit) {
        return value * (fromUnit / toUnit);
    }
    public static double convertFrom(double value, double fromUnit) {
        return value * fromUnit;
    }
    public static double convertTo(double value, double toUnit) {
        return value / toUnit;
    }
}
