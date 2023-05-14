package ch.epfl.javions.gui;

import ch.epfl.javions.Preconditions;
import ch.epfl.javions.WebMercator;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Text;
import jdk.jfr.Event;


import java.util.Objects;
import java.util.Optional;

import static javafx.beans.binding.Bindings.createBooleanBinding;
import static javafx.beans.binding.Bindings.subtract;


public final class AircraftController {
    private static final int ALT_RANGE = 12000;
    private final MapParameters mapParameters;
    private final ObjectProperty<ObservableAircraftState> selectedState;
    private final Pane pane;

    public AircraftController(MapParameters mapParameter,
                              ObservableSet<ObservableAircraftState> states,
                              ObjectProperty<ObservableAircraftState> selectedState) {
        Objects.requireNonNull(selectedState);
        this.mapParameters = mapParameter;
        this.selectedState = selectedState;
        pane  = new Pane();
        pane.getStylesheets().add("resources/aircraft.css");


        states.addListener((SetChangeListener<ObservableAircraftState>)
                change -> {
                if (change.wasAdded()) {
                    annotatedAircraft(change.getElementAdded()).setVisible(true);
                    pane().getChildren().add(annotatedAircraft(change.getElementAdded()));
                    System.out.println("hello le vaud");
                } else if (change.wasRemoved()) {
                    IcaoAddress add = change.getElementRemoved().getIcaoAddress();
                    pane().getChildren().removeIf((Node c) ->
                        add.string().equals(c.getId())
                    );
                }
        });

    }

    public Pane pane() {
        return pane;
    }

    public Group annotatedAircraft(ObservableAircraftState state) {
        Objects.requireNonNull(state, "state est null frero");
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
        Line line = new Line();
        return new Group();
    }

    private SVGPath icon(ObservableAircraftState state) {

        Optional<AircraftTypeDesignator> type = Optional.ofNullable(state.getAircraftData()).map(AircraftData::typeDesignator);
        Optional<AircraftDescription> descr = Optional.ofNullable(state.getAircraftData()).map(AircraftData::description);
        Optional<Integer> category = Optional.of(state.getCategory());
        Optional<WakeTurbulenceCategory> turbCategory = Optional.ofNullable(state.getAircraftData()).map(AircraftData::wakeTurbulenceCategory);
        SVGPath svgPath = new SVGPath();
        svgPath.getStyleClass().add("aircraft");
        AircraftIcon aircraftIcon = AircraftIcon.iconFor(
                type.orElse(new AircraftTypeDesignator("")),
                descr.orElse(new AircraftDescription("")),
                category.orElse(0),
                turbCategory.orElse(WakeTurbulenceCategory.UNKNOWN));

        ObjectProperty<AircraftIcon> aircraftIconProperty = new SimpleObjectProperty<>(aircraftIcon);
        svgPath.contentProperty().bind(
                aircraftIconProperty.map(AircraftIcon::svgPath));

        svgPath.setRotate(aircraftIcon.canRotate()
                ? state.trackOrHeadingProperty().doubleValue()
                : 0.0);
        ColorRamp colorRamp = ColorRamp.PLASMA;
        ReadOnlyDoubleProperty alt = state.altitudeProperty();
        svgPath.fillProperty().bind(
                        alt.map(
                                b -> colorRamp.at(
                                        Math.cbrt( b.doubleValue() / ALT_RANGE ) ) ) );

        return svgPath;
    }

    private Group tag(ObservableAircraftState state) {

        Text txt = new Text();
        Rectangle rect = new Rectangle();
        ObservableBooleanValue velNotNull = createBooleanBinding(() -> state.velocityProperty() != null);
        ObservableBooleanValue altNotNull = createBooleanBinding(() -> state.altitudeProperty() != null);
        Optional<String> registration = Optional.ofNullable(state.getAircraftData()).map(AircraftData::registration).map(AircraftRegistration::string);
        Optional<String > callSign = Optional.ofNullable(state.getCallSign()).map(CallSign::string);
        Optional<String> address = Optional.ofNullable(state.getIcaoAddress()).map(IcaoAddress::string);

        txt.textProperty().bind(
                Bindings.createStringBinding(() -> {
                    String label = registration.orElse(callSign.orElse(address.orElse("")));
                    return String.format("%s\n%f\u2002km/h %f\u2002m", label, state.getVelocity(), state.getAltitude());
                    },
                        state.velocityProperty().when(velNotNull).orElse(Double.NaN),
                        state.altitudeProperty().when(altNotNull).orElse(Double.NaN))
        );

        rect.widthProperty().bind(
                txt.layoutBoundsProperty().map(b -> b.getWidth() + 4));
        rect.heightProperty().bind(
                txt.layoutBoundsProperty().map(b -> b.getHeight() + 4));

        rect.setVisible(selectedState.equals(state) &&  mapParameters.getZoomLevel() < 11);// TODO: 09.05.23 state.equals(selectedState) ou selectedState.equals(state)
        Group tag = new Group(rect, txt);
        tag.getStyleClass().add("label");
        return tag;
    }
}
