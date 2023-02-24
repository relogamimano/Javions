package ch.epfl.javions;

public class Preconditions {
    private Preconditions() {}
    public static void checkArgument(boolean shouldBeTrue) throws IllegalArgumentException {
        if(!shouldBeTrue) {
            throw new IllegalArgumentException();
        }
    }


}
