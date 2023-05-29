package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.*;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import java.util.Objects;
import java.util.Optional;

import static ch.epfl.javions.Units.Angle.DEGREE;
import static ch.epfl.javions.Units.Speed.KILOMETER_PER_HOUR;
import static ch.epfl.javions.Units.convertTo;
import static ch.epfl.javions.WebMercator.x;
import static ch.epfl.javions.WebMercator.y;
import static javafx.beans.binding.Bindings.*;
import static javafx.scene.paint.CycleMethod.NO_CYCLE;

/**
 *
 * Aircraft controller class to manage the manage the entire set of aircraft on the map
 * @author Sofia Henriques Garfo (346298)
 * @author Romeo Maignal (360568)
 */
public final class AircraftController {
    private static final ColorRamp COLOR_RAMP = ColorRamp.PLASMA;
    private static final int ALT_RANGE = 12000;// Average altitude range for plane from airliner to small recreational plane.
    private static final int MIN_TAG_ZOOM = 11;// Minimum zoom for all tags to appear on screen
    private static final int MARGIN_SIZE = 4;// Margin between the text and the edge of the rectangle containing it.
    private final MapParameters mapParameters;
    private final ObjectProperty<ObservableAircraftState> selectedState;
    private final Pane pane;

    /**
     * Aircraft Controller's constructor.
     * It builds the manager of the entire set of observable aircraft and organize its placement over the map.
     * @param mapParameter parameters of the background map (includes access to methods used to organize the placement of all airplanes)
     * @param states    Set of observable airplanes
     * @param selectedState Last state that was clicked on.
     */
    public AircraftController(MapParameters mapParameter,
                              ObservableSet<ObservableAircraftState> states,
                              ObjectProperty<ObservableAircraftState> selectedState) {
        Objects.requireNonNull(selectedState);
        this.mapParameters = mapParameter;
        this.selectedState = selectedState;
        pane = new Pane();
        pane.setPickOnBounds(false);
        pane.getStylesheets().add("aircraft.css");

        states.addListener((SetChangeListener<ObservableAircraftState>)
                change -> {
            // If change was made to the list
                if (change.wasAdded()) {
                    //, and it happens to be the adding of an element :
                    //The specific annotated aircraft is added from the pane children list
                    pane().getChildren().add(annotatedAircraft(change.getElementAdded()));
                } else if (change.wasRemoved()) {
                    //, or it happens to be the removing of an element :
                    //The specific annotated aircraft is removed from the pane children list
                    pane().getChildren().removeIf(p ->
                            change.getElementRemoved().getIcaoAddress().string().equals(p.getId()));
                }
        });
    }

    /**
     * Getter method that return the AircraftController's pane
     * @return pane
     */
    public Pane pane() {
        return pane;
    }
    // AnnotatedAircraft -> Group = AircraftCompound + Trajectory
    private Group annotatedAircraft(ObservableAircraftState state) {
        Group annotatedAircraft = new Group(trajectory(state), aircraftCompound(state));
        annotatedAircraft.viewOrderProperty().bind(state.altitudeProperty().negate());
        annotatedAircraft.setId(state.getIcaoAddress().string());
        return annotatedAircraft;
    }
    //  AircraftCompound -> Group = icon + tag
    private Group aircraftCompound(ObservableAircraftState state) {
        Group aircraftCompound = new Group(tag(state), icon(state));
        //Binding of the abscissa position of the annotated aircraft on the pane
        aircraftCompound.layoutXProperty().bind(Bindings.createDoubleBinding(() ->
                        x(mapParameters.getZoomLevel(), state.getPosition().longitude())
                                - mapParameters.getMinX(),
                mapParameters.zoomProperty(),
                state.positionProperty(),
                mapParameters.minXProperty()
        ));
        //Binding of the ordinate position of the annotated aircraft on the pane
        aircraftCompound.layoutYProperty().bind(Bindings.createDoubleBinding(() ->
                        y(mapParameters.getZoomLevel(), state.getPosition().latitude())
                                - mapParameters.getMinY(),
                mapParameters.zoomProperty(),
                state.positionProperty(),
                mapParameters.minYProperty()
        ));
        return aircraftCompound;
    }

    // Trajectory -> Group = Line + Line + ... + Line
    private Group trajectory(ObservableAircraftState state) {
        Group trajectory = new Group();
        trajectory.visibleProperty().bind(selectedState.isEqualTo(state));
        var l0 = (ListChangeListener<ObservableAircraftState.AirbornePos>) change ->
            updateTrajectory(trajectory, state.getTrajectoryList());
        var l1 = (InvalidationListener) change -> updateTrajectory(trajectory, state.getTrajectoryList());
        //Visibility of the trajectory depends on whether the trajectory list or the zoom level has been changed or not.
        //The trajectory is created only if the aircraft is selected. It is done so in order to keep a certain degree of processing speed.
        trajectory.visibleProperty().addListener((o, oV, nV) -> {
                    if (nV) {
                        state.getTrajectoryList().addListener(l0);
                        mapParameters.zoomProperty().addListener(l1);
                    } else {
                        state.getTrajectoryList().removeListener(l0);
                        mapParameters.zoomProperty().removeListener(l1);
                    }
                });

        return trajectory;
    }

    //Method used to update the trajectory by adding one new line to the existing group of lines segments,
    //it is called each time an aircraft change it trajectory or each time the level of zoom changes.
    private void updateTrajectory(Group trajectory, ObservableList<ObservableAircraftState.AirbornePos> list) {
        for (int i = 0; i < list.size()-1; i++) {
            if (list.get(i).geopos() != null && list.get(i+1).geopos() != null) {
                //Coloring of the line
                Stop s1 = new Stop(0, COLOR_RAMP.at(Math.cbrt( list.get(i).altitude() / ALT_RANGE ) ));
                Stop s2 = new Stop(1, COLOR_RAMP.at(Math.cbrt( list.get(i+1).altitude() / ALT_RANGE ) ));
                LinearGradient linearGradient = new LinearGradient(0, 0, 1, 0, true, NO_CYCLE, s1, s2);
                Line line = new Line();
                line.startXProperty().bind(subtract(/* position x of the start of the line */
                        x(mapParameters.getZoomLevel(), list.get(i).geopos().longitude()),
                        mapParameters.minXProperty()));
                line.startYProperty().bind(subtract(/* position y of the start of the line */
                        y(mapParameters.getZoomLevel(), list.get(i).geopos().latitude()),
                        mapParameters.minYProperty()));
                line.endXProperty().bind(subtract(/* position x of the end of the line */
                        x(mapParameters.getZoomLevel(), list.get(i+1).geopos().longitude()),
                        mapParameters.minXProperty()));
                line.endYProperty().bind(subtract(/* position y of the end of the line */
                        y(mapParameters.getZoomLevel(), list.get(i+1).geopos().latitude()),
                        mapParameters.minYProperty()));
                line.setStroke(linearGradient);
                line.setStrokeWidth(2);
                trajectory.getChildren().add(line);
            }
        }
    }

    // Icon -> Path
    private SVGPath icon(ObservableAircraftState state) {
        SVGPath svgPath = new SVGPath();
        svgPath.setOnMouseClicked(e -> selectedState.set(state));
        svgPath.getStyleClass().add("aircraft");
        //Optional types are used because a series of called instances are subject to being null
        //and its use remove the need for explicit null testing
        Optional<AircraftTypeDesignator> type = Optional.ofNullable(state.getAircraftData())
                .map(AircraftData::typeDesignator);
        Optional<AircraftDescription> descr = Optional.ofNullable(state.getAircraftData())
                .map(AircraftData::description);
        Optional<WakeTurbulenceCategory> turbCategory = Optional.ofNullable(state.getAircraftData())
                .map(AircraftData::wakeTurbulenceCategory);
        //Initialization of the aircraft icon while being careful to implement the fact that the category is subject to changing.
        //Therefore, to the icon being updated.
        ObservableValue<AircraftIcon> aircraftIcon = state.categoryProperty().map(c -> AircraftIcon.iconFor(
                type.orElse(new AircraftTypeDesignator("")),
                descr.orElse(new AircraftDescription("")),
                c.intValue(),
                turbCategory.orElse(WakeTurbulenceCategory.UNKNOWN)));
        //Binding of the svg path content property to the path of the icon
        svgPath.contentProperty().bind(
                aircraftIcon.map(AircraftIcon::svgPath));

        //Binding of the rotation property to the trajectory of the aircraft
        svgPath.rotateProperty().bind(createDoubleBinding(() ->
                        aircraftIcon.getValue().canRotate()
                                ? convertTo(state.getTrackOrHeading(), DEGREE)
                                : 0.0,
                state.trackOrHeadingProperty()
        ));
        //Coloring of the icon in real time.
        ReadOnlyDoubleProperty alt = state.altitudeProperty();
        svgPath.fillProperty().bind(
                        alt.map(b -> COLOR_RAMP.at(
                                        Math.cbrt( b.doubleValue() / ALT_RANGE ) ) ) );
        return svgPath;
    }

    // Tag -> Group = | Label            |
    //                | Speed Altitude   |
    private Group tag(ObservableAircraftState state) {
        Text txt = new Text();
        Rectangle rect = new Rectangle();
        Group tag = new Group(rect, txt);
        tag.getStyleClass().add("label");
        //Optional types are used because ICAO address is subject to being null
        //and its use remove the need for explicit null testing
        Optional<String> address = Optional.ofNullable(state.getIcaoAddress())
                .map(IcaoAddress::string);

        txt.textProperty().bind(
                createStringBinding(() -> {
                    //The Label string is the top part of the rectangle that display the registration if not null
                    //otherwise the call sign if not null otherwise the ICAO address
                    String label = state.getAircraftData() != null
                            ? state.getAircraftData().registration().string()
                            :  Bindings.when(state.callSignProperty().isNotNull())
                            .then(Bindings.convert(state.callSignProperty().map(CallSign::string)))
                            .otherwise(address.orElse("")).get();

                    String velocity = Double.isNaN(state.getVelocity())
                            ? "?"
                            : String.valueOf((int)convertTo(state.getVelocity(), KILOMETER_PER_HOUR));
                    String altitude = Double.isNaN(state.getAltitude())
                            ? "?"
                            : String.valueOf((int)state.getAltitude());
                    return String.format("%s\n%s km/h\u2002%s m", label, velocity, altitude);
                    },
                        state.callSignProperty(),// Dependencies make sure that the code inside de brackets is updated in real time
                        state.velocityProperty(),
                        state.altitudeProperty())
        );

        //Set of the dimensions and the visibility of the tag
        rect.widthProperty().bind(
                txt.layoutBoundsProperty().map(b -> b.getWidth() + MARGIN_SIZE));
        rect.heightProperty().bind(
                txt.layoutBoundsProperty().map(b -> b.getHeight() + MARGIN_SIZE));
        tag.visibleProperty().bind(mapParameters.zoomProperty()
                .greaterThanOrEqualTo(MIN_TAG_ZOOM)
                .or(selectedState.isEqualTo(state)));


        return tag;
    }
}
