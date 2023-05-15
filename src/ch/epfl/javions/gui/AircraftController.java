package ch.epfl.javions.gui;

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
import javafx.scene.layout.Pane;
import javafx.scene.shape.*;
import javafx.scene.text.Text;


import java.util.Objects;
import java.util.Optional;

import static ch.epfl.javions.Units.Angle.DEGREE;
import static ch.epfl.javions.Units.Speed.KM_PER_HOUR;
import static ch.epfl.javions.Units.convertTo;
import static javafx.beans.binding.Bindings.createBooleanBinding;
import static javafx.beans.binding.Bindings.createDoubleBinding;


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
        pane.setPickOnBounds(false);
        pane.getStylesheets().add("aircraft.css");

        states.addListener((SetChangeListener<ObservableAircraftState>)
                change -> {
                if (change.wasAdded()) {
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

    private Group annotatedAircraft(ObservableAircraftState state) {
        Objects.requireNonNull(state, "state est null frero");
        Group aircraftCompound = new Group(tag(state), icon(state));
        aircraftCompound.setId(state.getIcaoAddress().string());
        aircraftCompound.layoutXProperty().bind(Bindings.createDoubleBinding(() ->
                        WebMercator.x(mapParameters.getZoomLevel(),
                                state.getPosition().longitude()) - mapParameters.getMinX(),
                mapParameters.zoomProperty(),
                state.positionProperty(),
                mapParameters.minXProperty()
        ));

        aircraftCompound.layoutYProperty().bind(Bindings.createDoubleBinding(() ->
                        WebMercator.y(mapParameters.getZoomLevel(),
                                state.getPosition().latitude()) - mapParameters.getMinY(),
                mapParameters.zoomProperty(),
                state.positionProperty(),
                mapParameters.minYProperty()
        ));
        return new Group(trajectory(), aircraftCompound);
    }

    private Group trajectory() {
        Line line = new Line();
        return new Group();
    }

    private SVGPath icon(ObservableAircraftState state) {
        SVGPath svgPath = new SVGPath();
        svgPath.getStyleClass().add("aircraft");
        Optional<AircraftTypeDesignator> type = Optional.ofNullable(state.getAircraftData()).map(AircraftData::typeDesignator);
        Optional<AircraftDescription> descr = Optional.ofNullable(state.getAircraftData()).map(AircraftData::description);
        Optional<Integer> category = Optional.of(state.getCategory());
        Optional<WakeTurbulenceCategory> turbCategory = Optional.ofNullable(state.getAircraftData()).map(AircraftData::wakeTurbulenceCategory);

        AircraftIcon aircraftIcon = AircraftIcon.iconFor(
                type.orElse(new AircraftTypeDesignator("")),
                descr.orElse(new AircraftDescription("")),
                category.orElse(0),
                turbCategory.orElse(WakeTurbulenceCategory.UNKNOWN));

        ObjectProperty<AircraftIcon> aircraftIconProperty = new SimpleObjectProperty<>(aircraftIcon);
        svgPath.contentProperty().bind(
                aircraftIconProperty.map(AircraftIcon::svgPath));
        svgPath.rotateProperty().bind(createDoubleBinding(() ->
                        aircraftIcon.canRotate()
                                ? convertTo(state.getTrackOrHeading(), DEGREE)
                                : 0.0,
                state.trackOrHeadingProperty()
        ));

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
        Group tag = new Group(rect, txt);
        tag.getStyleClass().add("label");

        Optional<String> registration = Optional.ofNullable(state.getAircraftData()).map(AircraftData::registration).map(AircraftRegistration::string);
        Optional<String > callSign = Optional.ofNullable(state.getCallSign()).map(CallSign::string);
        Optional<String> address = Optional.ofNullable(state.getIcaoAddress()).map(IcaoAddress::string);

        txt.textProperty().bind(
                Bindings.createStringBinding(() -> {
                    String label = registration.orElse(callSign.orElse(address.orElse("")));
                    String velocity = Double.isNaN(state.getVelocity())
                            ? "?"
                            : String.valueOf((int)convertTo(state.getVelocity(), KM_PER_HOUR));
                    String altitude = Double.isNaN(state.getAltitude())
                            ? "?"
                            : String.valueOf((int)convertTo(state.getAltitude(), KM_PER_HOUR));
                    return String.format("%s\n%s km/h\u2002%s m", label, velocity, altitude);
                    },
                        state.velocityProperty(),
                        state.altitudeProperty())
        );

        rect.widthProperty().bind(
                txt.layoutBoundsProperty().map(b -> b.getWidth() + 4));
        rect.heightProperty().bind(
                txt.layoutBoundsProperty().map(b -> b.getHeight() + 4));

        tag.visibleProperty().bind(selectedState
                .isEqualTo(state)
                .or(mapParameters
                                .zoomProperty()
                                .greaterThanOrEqualTo(11)));

        return tag;
    }
}
