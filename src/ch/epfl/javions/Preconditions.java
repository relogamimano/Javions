package ch.epfl.javions;

public class Preconditions {
    private Preconditions() {}
    void checkArgument(boolean shouldBeTrue) throws IllegalArgumentException {
        if(!shouldBeTrue) {
            throw new IllegalArgumentException();
        }
    }


}
