package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.*;
import javafx.scene.text.Text;


import java.util.Objects;
import java.util.Optional;

import static ch.epfl.javions.Units.Angle.DEGREE;
import static ch.epfl.javions.Units.Speed.KM_PER_HOUR;
import static ch.epfl.javions.Units.convertTo;
import static ch.epfl.javions.WebMercator.x;
import static ch.epfl.javions.WebMercator.y;
import static javafx.beans.binding.Bindings.*;
import static javafx.scene.paint.CycleMethod.NO_CYCLE;


public final class AircraftController {
    private static final ColorRamp COLOR_RAMP = ColorRamp.PLASMA;
    private static final int ALT_RANGE = 12000;
    private static final int MIN_TAG_ZOOM = 11;
    private static final int MARGIN_SIZE = 4;
    private final MapParameters mapParameters;
    int zoom;
    private final ObjectProperty<ObservableAircraftState> selectedState;
    private final Pane pane;

    public AircraftController(MapParameters mapParameter,
                              ObservableSet<ObservableAircraftState> states,
                              ObjectProperty<ObservableAircraftState> selectedState) {
        Objects.requireNonNull(selectedState);
        this.mapParameters = mapParameter;
        this.zoom = mapParameters.getZoomLevel();
        this.selectedState = selectedState;
        pane  = new Pane();
        pane.setPickOnBounds(false);
        pane.getStylesheets().add("aircraft.css");


        states.addListener((SetChangeListener<ObservableAircraftState>)
                change -> {
                if (change.wasAdded()) {
                    pane().getChildren().add(annotatedAircraft(change.getElementAdded()));
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
                        x(zoom, state.getPosition().longitude()) - mapParameters.getMinX(),
                mapParameters.zoomProperty(),
                state.positionProperty(),
                mapParameters.minXProperty()
        ));
        aircraftCompound.layoutYProperty().bind(Bindings.createDoubleBinding(() ->
                        y(zoom, state.getPosition().latitude()) - mapParameters.getMinY(),
                mapParameters.zoomProperty(),
                state.positionProperty(),
                mapParameters.minYProperty()
        ));

        return new Group(trajectory(state), aircraftCompound);
    }

    private Group trajectory(ObservableAircraftState state) {
        Group trajectory = new Group();

        selectedState.isEqualTo(state).addListener(ChangeListener -> {
            state.getTrajectoryList().addListener((ListChangeListener<ObservableAircraftState.AirbornePos>) change -> {
                updateTrajectory(trajectory, state.getTrajectoryList());
            });
            mapParameters.zoomProperty().addListener(ZoomChangeListener -> {
                updateTrajectory(trajectory, state.getTrajectoryList());
            });
        });

        return trajectory;
    }


    private void updateTrajectory(Group trajectory, ObservableList<ObservableAircraftState.AirbornePos> list) {
        for (int i = 0; i < list.size()-1; i++) {
            if (list.get(i).geopos() != null && list.get(i).geopos() != null) {
                Stop s1 = new Stop(0, COLOR_RAMP.at(Math.cbrt( list.get(i).altitude() / ALT_RANGE ) ));
                Stop s2 = new Stop(1, COLOR_RAMP.at(Math.cbrt( list.get(i+1).altitude() / ALT_RANGE ) ));
                LinearGradient linearGradient = new LinearGradient(0, 0, 1, 0, true, NO_CYCLE, s1, s2);
                Line line = new Line();
                line.startXProperty().bind(subtract(x(zoom, list.get(i).geopos().longitude()), mapParameters.minXProperty()));
                line.startYProperty().bind(subtract(y(zoom, list.get(i).geopos().latitude()), mapParameters.minYProperty()));
                line.endXProperty().bind(subtract(x(zoom, list.get(i+1).geopos().longitude()), mapParameters.minXProperty()));
                line.endYProperty().bind(subtract(y(zoom, list.get(i+1).geopos().latitude()), mapParameters.minYProperty()));
                line.setStroke(linearGradient);
                line.setStrokeWidth(2);
                trajectory.getChildren().add(line);
            }
        }
    }


    private SVGPath icon(ObservableAircraftState state) {
        SVGPath svgPath = new SVGPath();
        svgPath.setOnMouseClicked(e -> {
            selectedState.set(state);

        });
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


        ReadOnlyDoubleProperty alt = state.altitudeProperty();
        svgPath.fillProperty().bind(
                        alt.map(
                                b -> COLOR_RAMP.at(
                                        Math.cbrt( b.doubleValue() / ALT_RANGE ) ) ) );

        return svgPath;
    }

    private Group tag(ObservableAircraftState state) {
        Text txt = new Text();
        Rectangle rect = new Rectangle();
        Group tag = new Group(rect, txt);
        tag.getStyleClass().add("label");

        Optional<String> registration = Optional.ofNullable(state.getAircraftData()).map(AircraftData::registration).map(AircraftRegistration::string);
        Optional<String> callSign = Optional.ofNullable(state.getCallSign()).map(CallSign::string);
        Optional<String> address = Optional.ofNullable(state.getIcaoAddress()).map(IcaoAddress::string);

        txt.textProperty().bind(
                createStringBinding(() -> {
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
                txt.layoutBoundsProperty().map(b -> b.getWidth() + MARGIN_SIZE));
        rect.heightProperty().bind(
                txt.layoutBoundsProperty().map(b -> b.getHeight() + MARGIN_SIZE));
        tag.visibleProperty().bind(mapParameters.zoomProperty().greaterThanOrEqualTo(MIN_TAG_ZOOM).or(selectedState.isEqualTo(state)));


        return tag;
    }
}
