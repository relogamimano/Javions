package ch.epfl.javions.gui;

import ch.epfl.javions.Math2;
import ch.epfl.javions.Preconditions;
import javafx.beans.property.*;

/**
 * Parameters of the visible portion of the map
 * @author Sofia Henriques Garfo (346298)
 * @author Romeo Maignal (360568)
 */
public final class MapParameters {

    private static final int MAX_ZOOM = 19;
    private static final int MIN_ZOOM = 6;
    private final IntegerProperty zoom = new SimpleIntegerProperty(); // zoom level , 6 <= zoom level <= 19
    private final DoubleProperty minX = new SimpleDoubleProperty(); // x value of the left hand top corner
    private final DoubleProperty minY = new SimpleDoubleProperty(); // y value of the top left hand corner

    /**
     * Verifies that the zoom level is valid and set's the map's parameters
     * @param zoom
     * @param minX
     * @param minY
     */
    public MapParameters(int zoom, double minX, double minY) {
        this.minX.set(minX);
        this.minY.set(minY);
        Preconditions.checkArgument(zoom >= MIN_ZOOM && zoom <= MAX_ZOOM);
        this.zoom.set(zoom);

    }

    /**
     * Zoom propriety
     * @return read only zoom level
     */
    public ReadOnlyIntegerProperty zoomProperty() {
        return zoom;
    }

    /**
     * @return zoom level
     */
    public int getZoomLevel() {
        return zoom.get();
    }

    /**
     * Propriety of the x of the left top corner
     * @return read only x value
     */
    public ReadOnlyDoubleProperty minXProperty() {
        return minX;
    }

    /**
     * @return x coordinate of the top left corner
     */
    public double getMinX() {
        return this.minX.get();
    }

    /**
     * Propriety of the y of the left top corner
     * @return read only y value
     */
    public ReadOnlyDoubleProperty minYProperty() {
        return this.minY;
    }

    /**
     * @return y coordinate of the top left corner
     */
    public double getMinY() {
        return this.minY.get();
    }

    /**
     * Translate the top left corner by the given x,y values
     * @param translateX
     * @param translateY
     */
    public void scroll(double translateX, double translateY) {
        this.minX.set(getMinX() + translateX);
        this.minY.set(getMinY() + translateY);
    }

    /**
     * Adjusts the current zoomlevel
     * @param deltaZoom difference of zoom level
     */
    public void changeZoomLevel(int deltaZoom) {
        int odlZoom = zoom.get();
        zoom.set(Math2.clamp(MIN_ZOOM, odlZoom + deltaZoom, MAX_ZOOM));
        minX.set(Math.scalb(minX.get(), zoom.get() - odlZoom));
        minY.set(Math.scalb(minY.get() , zoom.get() - odlZoom));

    }


}

