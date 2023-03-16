package ch.epfl.javions;

/**
 * @author: Sofia Henriques Garfo (346298)
 * @author: Romeo Maignal (360568)
 */
public final class Math2 {
    private Math2() {}

    /**
     * Limits the value of a variable v to the given maximum and minimum
     * @param min minimum value
     * @param v actual value
     * @param max maximal value
     * @return min if v is less than the minimun
     *         max if v is strictly greater than the max
     *         v otherwise
     */
    public static int clamp(int min, int v, int max) {
        Preconditions.checkArgument(min <= max);
        if (v < min) {
            return min;
        } else if (v > max) {
            return max;
        } else {
            return v;
        }
    }

    /**
     * Calculates the hyperbolic sinus of x
     * @param x
     * @return hyperbolic sinus
     */
    public static double asinh(double x) {
        return Math.log(x + Math.sqrt(1 + x * x));
    }

}
