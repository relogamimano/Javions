package ch.epfl.javions.gui;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ColorRampTest {
    @Test void atExceptionWorkingProperly() {
        assertThrows(IllegalArgumentException.class, () -> new ColorRamp(Color.color(0.5, 0.5, 0.5)));
    }

    @Test
    void atWorkingWithInBetweenRatio() {
        ColorRamp cr = new ColorRamp(
                Color.color(1., 0., 0.),
                Color.color(0., 1., 0.),
                Color.color(0., 0., 1.),
                Color.color(0.5, 0.5, 0.5),
                Color.color(1., 1., 1.)
        );
        Color actualColor = cr.at(0.3);
        Color expectedColor = Color.color(0.0, 0.8, 0.2);
        assertEquals(expectedColor, actualColor);

    }

    @Test void atWorkingWithExactRatio() {
        ColorRamp cr = new ColorRamp(
                Color.color(1., 0., 0.),
                Color.color(0., 1., 0.),
                Color.color(0., 0., 1.),
                Color.color(0.5, 0.5, 0.5),
                Color.color(1., 1., 1.)
        );
        Color actualColor = cr.at(0.25);
        Color expectedColor = Color.color(0.0, 1.0, 0.0);
        assertEquals(expectedColor, actualColor);
    }
}
