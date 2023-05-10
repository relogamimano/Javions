/*
package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.WebMercator;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;

import javafx.scene.input.MouseEvent;
import java.io.IOException;
import java.util.Objects;

public final class BaseMapController {
    private final TileManager tileManager;
    private final MapParameters mapParams;
    private final Canvas canvas;
    private final Pane pane;
    private final GraphicsContext context;
    private boolean redrawNeeded = true;
    private Point2D oldMousePos = new Point2D(0d, 0d);
    private static final int TILE_SIZE = 256; //todo: meileure idée de le mettre dans une des classes Tiles

    public BaseMapController(TileManager tileManager, MapParameters mapParams) {
        this.tileManager = Objects.requireNonNull(tileManager);
        this.mapParams = Objects.requireNonNull(mapParams);
        this.canvas = new Canvas();
        this.pane = new Pane(canvas);

        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());

        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        });

        //event handlers
        LongProperty minScrollTime = new SimpleLongProperty();
        pane.setOnScroll(e -> {
            int zoomDelta = (int) Math.signum(e.getDeltaY());
            if (zoomDelta == 0)
                return;

            long currentTime = System.currentTimeMillis();
            if (currentTime < minScrollTime.get())
                return;

            minScrollTime.set(currentTime + 200);

            this.mapParams.scroll(e.getX(), e.getY());
            this.mapParams.changeZoomLevel(zoomDelta);
            this.mapParams.scroll(-e.getX(), -e.getY());

            this.redrawOnNextPulse();
        });

        pane.setOnMousePressed(e -> this.oldMousePos = new Point2D(e.getX(), e.getY()));
        pane.setOnMouseDragged(this::updateOnDraggedAndReleasedEvent);
        pane.setOnMouseReleased(this::updateOnDraggedAndReleasedEvent);
        pane.widthProperty().addListener((obs, oldVal, newVal) -> { this.redrawOnNextPulse(); });
        pane.heightProperty().addListener((obs, oldVal, newVal) -> { this.redrawOnNextPulse(); });

        this.context = canvas.getGraphicsContext2D();
    }

    private void updateOnDraggedAndReleasedEvent(MouseEvent e) {
        double deltaX = this.oldMousePos.getX() - e.getX();
        double deltaY = this.oldMousePos.getY() - e.getY();
        this.mapParams.scroll(deltaX, deltaY);
        this.oldMousePos = this.oldMousePos.subtract(deltaX, deltaY);
        this.redrawOnNextPulse();
    }

    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }

    private void redrawIfNeeded() {
        if (!this.redrawNeeded)
            return;

        this.redrawNeeded = false;

        double widthPadding = this.mapParams.getMinX() % 256;
        double heightPadding = this.mapParams.getMinY() % 256; //todo: check s'il y a pas des méthodes d'appels inutiles

        int minIndexTileX = (int)Math.floor(this.mapParams.getMinX()/BaseMapController.TILE_SIZE); //floor probablement inutile dans ce cas puisqu'on doit cast à un int dans tous les case
        int minIndexTileY = (int)Math.floor(this.mapParams.getMinY()/BaseMapController.TILE_SIZE); //de même ici
        int maxIndexTileX = (int)Math.ceil((this.mapParams.getMinX() + this.canvas.getWidth() + widthPadding)/BaseMapController.TILE_SIZE);
        int maxIndexTileY = (int)Math.ceil((this.mapParams.getMinY() + this.canvas.getHeight() + heightPadding)/BaseMapController.TILE_SIZE);

        //code pour les images
        for (int i = minIndexTileX; i < maxIndexTileX; i++) {
            for (int j = minIndexTileY; j < maxIndexTileY; j++) {
                TileManager.TileId tileId = new TileManager.TileId(this.mapParams.getZoomLevel(), i, j);
                try {
                    Image tileImage = tileManager.imageForTileAt(tileId);
                    this.context.drawImage(tileImage, (i-minIndexTileX)*256 - widthPadding, (j-minIndexTileY)*256 - heightPadding);
                } catch (Exception e) {}; //todo: fix
            }
        }
    }

    public void centerOn(GeoPos newCenter) {
        Objects.requireNonNull(newCenter);
        double x = WebMercator.x(this.mapParams.getZoomLevel(), newCenter.longitude());
        double y = WebMercator.y(this.mapParams.getZoomLevel(), newCenter.latitude());

        double newMinX = x - this.canvas.getWidth()/2;
        double newMinY = y - this.canvas.getHeight()/2;

        double deltaMinX = newMinX - this.mapParams.getMinX();
        double deltaMinY = newMinY - this.mapParams.getMinY();

        this.mapParams.scroll(deltaMinX, deltaMinY);
    }

    public Pane pane() { return this.pane; }
}*/
