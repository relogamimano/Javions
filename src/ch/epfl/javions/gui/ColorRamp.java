package ch.epfl.javions.gui;

import ch.epfl.javions.Preconditions;
import javafx.scene.paint.Color;

/**
 * Immutable Color Ramp class
 * @author Sofia Henriques Garfo (346298)
 * @author Romeo Maignal (360568)
 */

public final class ColorRamp {
    public static final ColorRamp PLASMA = new ColorRamp(
            Color.valueOf("0x0d0887ff"), Color.valueOf("0x220690ff"),
            Color.valueOf("0x320597ff"), Color.valueOf("0x40049dff"),
            Color.valueOf("0x4e02a2ff"), Color.valueOf("0x5b01a5ff"),
            Color.valueOf("0x6800a8ff"), Color.valueOf("0x7501a8ff"),
            Color.valueOf("0x8104a7ff"), Color.valueOf("0x8d0ba5ff"),
            Color.valueOf("0x9814a0ff"), Color.valueOf("0xa31d9aff"),
            Color.valueOf("0xad2693ff"), Color.valueOf("0xb6308bff"),
            Color.valueOf("0xbf3984ff"), Color.valueOf("0xc7427cff"),
            Color.valueOf("0xcf4c74ff"), Color.valueOf("0xd6556dff"),
            Color.valueOf("0xdd5e66ff"), Color.valueOf("0xe3685fff"),
            Color.valueOf("0xe97258ff"), Color.valueOf("0xee7c51ff"),
            Color.valueOf("0xf3874aff"), Color.valueOf("0xf79243ff"),
            Color.valueOf("0xfa9d3bff"), Color.valueOf("0xfca935ff"),
            Color.valueOf("0xfdb52eff"), Color.valueOf("0xfdc229ff"),
            Color.valueOf("0xfccf25ff"), Color.valueOf("0xf9dd24ff"),
            Color.valueOf("0xf5eb27ff"), Color.valueOf("0xf0f921ff"));

    private final Color[] colors;
    private final double mod;

    /**
     * Color Ramp constructor : it takes a list of color as arguments that must contains at least 2 element.
     * The number of element will define the index, associated to each element, based on the even splitting.
     * @param colors array of colors
     */

    public ColorRamp(Color ... colors) {
        Preconditions.checkArgument(colors.length >= 2);
        this.colors = colors;
        mod = 1. / ( colors.length - 1 );

    }

    /**
     * Method to used to get either a specific color index by the ratio or a mix of two colors. If the ratio is under
     * 0, then the color returned is the lowest one in the list. If the ration is above 1, then the color
     * returned is the highest one in the list
     * @param x  normalized abscissa on the color spectre
     * @return  specific color or a mix of two
     */

    public Color at(double x) {
        int i = (int) (colors.length * x);
        if (x < 0) return colors[0];
        if (x > 1) return colors[colors.length - 1];
        Color c0 = colors[i];
        Color c1 = colors[i + 1];
        double p = ( x - i * mod) / mod;
        return c0.interpolate(c1, p);
    }





}
