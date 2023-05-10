package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.WebMercator;
import com.sun.glass.ui.Screen;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.geometry.Point2D;


import java.io.IOException;
import java.util.Objects;

public final class BaseMapController {
    private final TileManager tileManager;
    private final MapParameters mapParameters;
    private final Canvas canvas ;
    private final Pane pane ;
    private final GraphicsContext context;
    private boolean redrawNeeded;
    private static final int TILE_SIZE = 256;

    private Point2D lastMousePosition = new Point2D(0d,0d) ;

    public BaseMapController(TileManager tileManager,MapParameters mapParameters){
        this.tileManager = tileManager;
        this.mapParameters = mapParameters;
        canvas = new Canvas();
        pane  = new Pane(canvas);

        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());

        redrawNeeded = true;

        installListeners();
        installHandlers();

        context = canvas.getGraphicsContext2D();
    }

    private void installListeners(){
        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        });

        canvas.widthProperty().addListener((p, oldW, newW) -> redrawOnNextPulse());
        canvas.widthProperty().addListener((p,oldW, newW) -> redrawOnNextPulse());
    }

    private void installHandlers(){
        LongProperty minScrollTime = new SimpleLongProperty();
        pane.setOnScroll(e -> {                                //Scroll Handler
            int zoomDelta = (int) Math.signum(e.getDeltaY());
            if (zoomDelta == 0) return;

            long currentTime = System.currentTimeMillis();
            if (currentTime < minScrollTime.get()) return;
            minScrollTime.set(currentTime + 200);

            mapParameters.scroll(e.getX(), e.getY());
            mapParameters.changeZoomLevel(zoomDelta);
            mapParameters.scroll(-e.getX(), -e.getY());
            redrawOnNextPulse();

        });

        pane.setOnMousePressed( e -> {
                lastMousePosition = new Point2D(e.getX(),e.getY());    // mouse pressed handler
                pane.setOnMouseDragged( d -> {
                    ;
                    double deltaX = lastMousePosition.getX() - e.getX();
                    double deltaY = lastMousePosition.getY() - e.getY();
                    mapParameters.scroll(deltaX, deltaY);
                    lastMousePosition = lastMousePosition.subtract(deltaX, deltaY);
                    redrawOnNextPulse();
                });
        }
        );
        pane.setOnMouseReleased(e -> {
            double deltaX = lastMousePosition.getX() - e.getX();
            double deltaY = lastMousePosition.getY() - e.getY();
            mapParameters.scroll(deltaX, deltaY);
            lastMousePosition = lastMousePosition.subtract(deltaX, deltaY);
            redrawOnNextPulse();
        });

    }

    public Pane pane ( ){
        return pane;
    }

    public void centerOn(GeoPos position){
        Objects.requireNonNull(position);
        double x = WebMercator.x(mapParameters.getZoomLevel(), position.longitude());
        double y = WebMercator.y(mapParameters.getZoomLevel(), position.latitude());

        double newMinX = x - this.canvas.getWidth()/2;
        double newMinY = y - this.canvas.getHeight()/2;

        double deltaMinX = newMinX - mapParameters.getMinX();
        double deltaMinY = newMinY - mapParameters.getMinY();

        mapParameters.scroll(deltaMinX, deltaMinY);
    }

    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }



    private void redrawIfNeeded() {
        if (!this.redrawNeeded)
            return;

        this.redrawNeeded = false;

        double widthPadding = mapParameters.getMinX() % TILE_SIZE;
        double heightPadding = mapParameters.getMinY() % TILE_SIZE;

        int minIndexTileX = (int)Math.floor(mapParameters.getMinX()/ TILE_SIZE);
        int minIndexTileY = (int)Math.floor(mapParameters.getMinY()/ TILE_SIZE);
        int maxIndexTileX = (int)Math.ceil((mapParameters.getMinX() + this.canvas.getWidth() + widthPadding)/ ch.epfl.javions.gui.BaseMapController.TILE_SIZE);
        int maxIndexTileY = (int)Math.ceil((mapParameters.getMinY() + this.canvas.getHeight() + heightPadding)/ ch.epfl.javions.gui.BaseMapController.TILE_SIZE);

        //code pour les images
        for (int i = minIndexTileX; i < maxIndexTileX; i++) {
            for (int j = minIndexTileY; j < maxIndexTileY; j++) {
                TileManager.TileId tileId = new TileManager.TileId(mapParameters.getZoomLevel(), i, j);
                try {
                    Image tileImage = tileManager.imageForTileAt(tileId);
                    this.context.drawImage(tileImage, (i-minIndexTileX)*TILE_SIZE - widthPadding, (j-minIndexTileY)*TILE_SIZE - heightPadding);
                } catch (Exception ignored) {};
            }


        }

    }

}


