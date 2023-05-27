package ch.epfl.javions.gui;

import ch.epfl.javions.Math2;
import ch.epfl.javions.Preconditions;
import javafx.beans.property.*;

public final class MapParameters {

    private static final int MAX_ZOOM = 19;
    private static final int MIN_ZOOM = 6;
    private final IntegerProperty zoom = new SimpleIntegerProperty(); // zoom level , 6 <= zoom level <= 19
    private final DoubleProperty minX = new SimpleDoubleProperty(); // x value of the left hand top corner
    private final DoubleProperty minY = new SimpleDoubleProperty(); // y value of the top left hand corner

    public MapParameters(int zoom, double minX, double minY) {
        this.minX.set(minX);
        this.minY.set(minY);
        Preconditions.checkArgument(zoom >= MIN_ZOOM && zoom <= MAX_ZOOM);
        this.zoom.set(zoom);

    }

    public ReadOnlyIntegerProperty zoomProperty() {
        return zoom;
    }

    public int getZoomLevel() {
        return zoom.get();
    }

    public ReadOnlyDoubleProperty minXProperty() {
        return minX;
    }

    public double getMinX() {
        return this.minX.get();
    }

    public ReadOnlyDoubleProperty minYProperty() {
        return this.minY;
    }

    public double getMinY() {
        return this.minY.get();
    }

    public void scroll(double translateX, double translateY) {
        this.minX.set(getMinX() + translateX);
        this.minY.set(getMinY() + translateY);
    }


    public void changeZoomLevel(int newZoom) {
        int odlZoom = zoom.get();
        zoom.set(Math2.clamp(MIN_ZOOM, odlZoom + newZoom, MAX_ZOOM));
        minX.set(Math.scalb(minX.get(), zoom.get() - odlZoom));
        minY.set(Math.scalb(minY.get() , zoom.get() - odlZoom));

    }


}

