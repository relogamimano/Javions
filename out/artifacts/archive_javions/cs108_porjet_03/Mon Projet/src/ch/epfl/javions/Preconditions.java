package ch.epfl.javions;
/**
 * Assure the valid progress of the running program
 * @author: Sofia Henriques Garfo (346298)
 * @author: Romeo Maignal (360568)
 */
public class Preconditions {
    private Preconditions() {}

    /**
     * Verifies that the preconditions needed to execute a method are met
     * @param shouldBeTrue precondition
     * @throws IllegalArgumentException if the precondition is not verified
     */
    public static void checkArgument(boolean shouldBeTrue) throws IllegalArgumentException {
        if(!shouldBeTrue) {
            throw new IllegalArgumentException();
        }
    }


}
