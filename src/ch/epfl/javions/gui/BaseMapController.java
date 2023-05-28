package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.WebMercator;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.io.UncheckedIOException;
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


    public BaseMapController(TileManager tileManager, MapParameters mapParams) {
        this.tileManager = tileManager;
        this.mapParameters = mapParams;
        canvas = new Canvas();
        pane  = new Pane(canvas);

        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());

        redrawNeeded = true;

        context = canvas.getGraphicsContext2D();

        installListeners();
        installHandlers();
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
        pane.setOnScroll(e -> {
            int zoomDelta = (int) Math.signum(e.getDeltaY());
            if (zoomDelta == 0)
                return;

            long currentTime = System.currentTimeMillis();
            if (currentTime < minScrollTime.get())
                return;

            minScrollTime.set(currentTime + 200);

            mapParameters.scroll(e.getX(), e.getY());
            mapParameters.changeZoomLevel(zoomDelta);
            mapParameters.scroll(-e.getX(), -e.getY());

            redrawOnNextPulse();
        });

        pane.setOnMousePressed(e ->
                lastMousePosition = new Point2D(e.getX(), e.getY()));
        pane.setOnMouseDragged(this::mouseDraggedAndReleased);
        pane.setOnMouseReleased(this::mouseDraggedAndReleased);
        pane.widthProperty().addListener((obs, oldVal, newVal) ->
                this.redrawOnNextPulse());
        pane.heightProperty().addListener((obs, oldVal, newVal) ->
                this.redrawOnNextPulse());

    }




    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }

    private void redrawIfNeeded() {
        if (!this.redrawNeeded)
            return;

        this.redrawNeeded = false;

        int firstTileX = (int)(mapParameters.getMinX()/TILE_SIZE);
        int firstTileY = (int)(mapParameters.getMinY()/TILE_SIZE);
        int lastTileX = (int)((mapParameters.getMinX() + canvas.getWidth()  )/TILE_SIZE + 1);
        int lastTileY = (int)((mapParameters.getMinY() + canvas.getHeight() )/TILE_SIZE + 1);

        for (int i = 0; i < lastTileX - firstTileX; i++) {
            for (int j = 0; j < lastTileY - firstTileY; j++) {
                TileManager.TileId tileId = new TileManager.TileId(this.mapParameters.getZoomLevel(),
                        i + firstTileX, j + firstTileY);
                try {
                    Image tileImage = tileManager.imageForTileAt(tileId);
                    this.context.drawImage(tileImage, i*TILE_SIZE - this.mapParameters.getMinX() % TILE_SIZE,
                            j*TILE_SIZE - this.mapParameters.getMinY() % TILE_SIZE);
                } catch (IOException exception) { throw new UncheckedIOException(exception); }
            }
        }
    }


    public void centerOn(GeoPos position) {Objects.requireNonNull(position);
        double positionEnX = WebMercator.x(mapParameters.getZoomLevel(), position.longitude()) - canvas.getWidth()/2;
        double positionEnY = WebMercator.y(mapParameters.getZoomLevel(), position.latitude()) - canvas.getHeight()/2;
        mapParameters.scroll(positionEnX - mapParameters.getMinX(),  positionEnY - mapParameters.getMinY());
    }


    public Pane pane() { return pane; }

    private void mouseDraggedAndReleased(MouseEvent e) {
        double x = lastMousePosition.getX() - e.getX();
        double y = lastMousePosition.getY() - e.getY();
        mapParameters.scroll(x, y);
        lastMousePosition = lastMousePosition.subtract(x,y);
        this.redrawOnNextPulse();
    }
}