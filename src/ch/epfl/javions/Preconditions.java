package ch.epfl.javions;
/**
 * Préconditions nécessaires avant l'appel d'une méthode
 */
public class Preconditions {
    private Preconditions() {}

    /**
     * Vérifie que les préconditions sont valides
     * @param shouldBeTrue
     *          précondition
     * @throws IllegalArgumentException
     *          la précondition n'est pas vérifié
     */
    public static void checkArgument(boolean shouldBeTrue) throws IllegalArgumentException {
        if(!shouldBeTrue) {
            throw new IllegalArgumentException();
        }
    }


}
