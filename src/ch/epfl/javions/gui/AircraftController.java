package ch.epfl.javions.gui;

import ch.epfl.javions.WebMercator;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

import static javafx.beans.binding.Bindings.createBooleanBinding;


public final class AircraftController {
    private static final int ALT_RANGE = 12000;
    private final MapParameters mapParameters;
    private final ObservableSet<ObservableAircraftState> states;
    private final ObjectProperty<ObservableAircraftState> selectedState;
    private final Pane pane;

    public AircraftController(MapParameters mapParameter,
                              ObservableSet<ObservableAircraftState> states,
                              ObjectProperty<ObservableAircraftState> selectedState) {
        this.mapParameters = mapParameter;
        this.states = states;
        this.selectedState = selectedState;
        this.pane = new Pane();

        states.addListener((SetChangeListener<ObservableAircraftState>)
                change -> {
            if (change.wasAdded()) {
                pane().getChildren().add(annotatedAircraft(change.getElementAdded()));

            } else if (change.wasRemoved()) {
                pane().getChildren().remove(change.getElementRemoved());

            }
        });

    }

    public Pane pane(){
        return pane;
    }

    public Group annotatedAircraft(ObservableAircraftState state) {
        Group aircraftCompound = new Group(tag(state), icon(state));
        // TODO: 11.05.23 condition pour verifié que les positions peuvent belle et bien etre representées
        aircraftCompound.layoutXProperty().bind(Bindings.createDoubleBinding(() ->
                        WebMercator.x(mapParameters.getZoomLevel(),
                                state.getPosition().longitude() - mapParameters.getMinX()),
                state.positionProperty(),
                mapParameters.minXProperty(),
                mapParameters.zoomProperty()
        ));
        aircraftCompound.layoutYProperty().bind(Bindings.createDoubleBinding(() ->
                        WebMercator.y(mapParameters.getZoomLevel(),
                                state.getPosition().latitude() - mapParameters.getMinY()),
                state.positionProperty(),
                mapParameters.minYProperty(),
                mapParameters.zoomProperty()
        ));
        return new Group(trajectory(), aircraftCompound);
    }

    private Group trajectory() {
        return new Group();
    }

    private SVGPath icon(ObservableAircraftState state) {
        SVGPath svgPath = new SVGPath();
        AircraftIcon aircraftIcon = AircraftIcon.iconFor(
                state.getAircraftData().typeDesignator(),
                state.getAircraftData().description(),
                state.getCategory(),
                state.getAircraftData().wakeTurbulenceCategory());

        ObjectProperty<AircraftIcon> aircraftIconProperty = new SimpleObjectProperty<>(aircraftIcon);
        svgPath.contentProperty().bind(
                aircraftIconProperty.map(AircraftIcon::svgPath));

        svgPath.setRotate(aircraftIcon.canRotate()
                ? state.trackOrHeadingProperty().doubleValue()
                : 0.0);
        svgPath.setContent(aircraftIcon.svgPath());
        ColorRamp colorRamp = ColorRamp.PLASMA;
        ReadOnlyDoubleProperty alt = state.altitudeProperty();
        svgPath.fillProperty().bind(
                        alt.map(
                                b -> colorRamp.at(
                                        Math.cbrt( b.doubleValue() / ALT_RANGE ) ) ) );

        return svgPath;
    }

    private Group tag(ObservableAircraftState state) {
        Group grp = new Group();
        Text txt = new Text();
        Rectangle rect = new Rectangle();
        ObservableBooleanValue velNotNull = createBooleanBinding(() -> state.velocityProperty() != null);
        ObservableBooleanValue altNotNull = createBooleanBinding(() -> state.altitudeProperty() != null);
        txt.textProperty().bind(// TODO: 09.05.23 is the following code correclty written and to implement de registration-callSign-icao label ?
                Bindings.createStringBinding(() ->
                        String.format("%o\u2002km/h %o\u2002m", state.getAircraftData().registration(),
                                state.getVelocity(),
                                state.getAltitude()),
                        state.velocityProperty().when(velNotNull).orElse(Double.NaN),
                        state.altitudeProperty().when(altNotNull).orElse(Double.NaN))
        );

        rect.widthProperty().bind(
                txt.layoutBoundsProperty().map(b -> b.getWidth() + 4));
        rect.heightProperty().bind(
                txt.layoutBoundsProperty().map(b -> b.getHeight() + 4));

        rect.setVisible(state.equals(selectedState) &&  mapParameters.getZoomLevel() < 11);// TODO: 09.05.23 state.equals(selectedState) ou selectedState.equals(state)

        grp.getChildren().add(txt);
        grp.getChildren().add(rect);
        return grp;
    }
}
