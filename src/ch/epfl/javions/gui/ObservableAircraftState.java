package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.beans.property.*;
import javafx.collections.ObservableList;



import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.unmodifiableObservableList;


/**
 * Represents the state of an aircraft
 * @author Sofia Henriques Garfo (346298)
 * @author Romeo Maignal (360568)
 */
public final class ObservableAircraftState implements AircraftStateSetter {


    private long lastTimeStamp ;  // time stamp of the last message
    private final IcaoAddress icaoAddress;
    private final AircraftData aircraftData;
    private final ObjectProperty<CallSign> callSign = new SimpleObjectProperty<>();
    private final LongProperty timeSampsNs = new SimpleLongProperty();
    private final ObjectProperty<GeoPos> position = new SimpleObjectProperty<>();
    private final DoubleProperty trackOrHeading = new SimpleDoubleProperty();
    private final IntegerProperty category = new SimpleIntegerProperty();
    private final DoubleProperty altitude = new SimpleDoubleProperty();
    private final DoubleProperty velocity = new SimpleDoubleProperty();

    // list of the positions of the aircraft's position since the first timeStampsNs message
    private final ObservableList<AirbornePos> trajectoryList = observableArrayList();
    private final ObservableList<AirbornePos> unmodifiableTrajectoryList = unmodifiableObservableList(trajectoryList); //final?

    /**
     * Read-only trajectory list which contains the positions of the aircraft in space since the first message
     * @return read-only non-modifiable trajectory list
     */
    public ObservableList<AirbornePos> trajectoryProperty(){
        return unmodifiableTrajectoryList;
    }

    /**
     * Trajectory list which contains the positions of the aircraft in space since the first message
     * @return observable and modifiable trajectory list
     */
    public ObservableList<AirbornePos> getTrajectoryList(){
        return trajectoryList;
    }


    /**
     * @return IcaoAdress of the aircraft
     */
    public IcaoAddress getIcaoAddress(){
        return icaoAddress;
    }

    /**
     * @return AircraftData for the aircraft
     */
    public AircraftData getAircraftData() {
        return aircraftData;
    }

    /**
     * @return Read-only category of the aircraft
     */
    public ReadOnlyIntegerProperty categoryProperty(){
        return category;
    }

    /**
     * @return category of the aircraft
     */
    public int getCategory(){
        return category.get();
    }

    /**
     * Set the aircraft's category
     * @param category the category of the aircraft
     */
    @Override
    public void setCategory(int category) {
        this.category.set(category);
    }

    /**
     * @return read-only CallSign of the aircraft
     */
    public ReadOnlyObjectProperty<CallSign> callSignProperty(){
        return callSign;
    }

    /**
     * @return CallSign of the aircraft
     */
    public CallSign getCallSign() {
        return callSign.get();
    }

    /**
     * Sets the aircraft's callSign
     * @param callsSign the call sign of the aircraft
     */
    @Override
    public void setCallSign(CallSign callsSign) {
        this.callSign.set(callsSign);
    }

    /**
     * @return read-only geographic position of the aircraft
     */
    public ReadOnlyObjectProperty<GeoPos> positionProperty(){
        return position;
    }

    /**
     * @return  geographic position of the aircraft
     */
    public GeoPos getPosition(){
        return position.get();
    }

    /**
     * sets the aircraft's geographic position
     * @param position the geographic position of the airplane
     */
    @Override
    public void setPosition(GeoPos position) {
        this.position.set(position);
        if(!Double.isNaN(getAltitude())){
            trajectoryList.add(new AirbornePos(position,getAltitude()));
        }
    }

    /**
     * @return read-only direction of the aircraft
     */
    public ReadOnlyDoubleProperty trackOrHeadingProperty(){
        return trackOrHeading;
    }

    /**
     * @return direction of the aircraft
     */
    public double getTrackOrHeading() {
        return trackOrHeading.get();
    }

    /**
     *
     * @param trackOrHeading the direction of the airplane
     */
    @Override
    public void setTrackOrHeading(double trackOrHeading) {
        this.trackOrHeading.set(trackOrHeading);
    }

    /**
     * @return read-only altitude data of the aircraft
     */
    public ReadOnlyDoubleProperty altitudeProperty(){
        return altitude;
    }

    /**
     * @return altitude data of the aircraft
     */
    public double getAltitude(){
        return altitude.get();
    }

    /**
     * Sets the aircraft's altitude if the aircraft's position has changed or if
     * the time stamp of the last message is the same as the on of the current message
     * @param altitude the altitude data of the airplane
     */
    @Override
    public void setAltitude(double altitude) {
        this.altitude.set(altitude);
    if( trajectoryList.isEmpty()){
            add( getPosition(), altitude);
        }
        else if (   getTimeStampNs() == lastTimeStamp){
            trajectoryList.remove( trajectoryList.size()-1);
            add(getPosition(), altitude);
        }

    }

    /**
     * @return read-only velocity of the aircraft
     */
    public ReadOnlyDoubleProperty velocityProperty(){
        return velocity;
    }

    /**
     * @return velocity of the aircraft
     */
    public double getVelocity() {
        return velocity.get();
    }

    /**
     * @param velocity the airplane's velocity
     */
    @Override
    public void setVelocity(double velocity) {
        this.velocity.set(velocity);
    }

    /**
     * @return read-only time stamp of the aircraft's  message
     */
    public ReadOnlyLongProperty timeStampNsProperty(){
        return timeSampsNs;
    }

    /**
     * @return time stamp of the aircraft's message
     */
    public long getTimeStampNs(){
        return timeSampsNs.get();
    }

    /**
     * @param timeStampNs the timestamp of the  message
     */
    @Override
    public void setLastMessageTimeStampNs(long timeStampNs) {
        this.timeSampsNs.set(timeStampNs);
    }

    /**
     * Represents the position of the aircraft
     * @param geopos aircraft's position
     * @param altitude of the aircraft
     */
    public record AirbornePos(GeoPos geopos, double altitude){

    }

    /**
     * @param address of the aircraft
     * @param aircraftData of the aircraft
     */
    public ObservableAircraftState(IcaoAddress address, AircraftData aircraftData){
        this.icaoAddress = address;
        this.aircraftData = aircraftData;
    }

    private void add( GeoPos geopos, double alt){
        trajectoryList.add(new AirbornePos(geopos, alt));
        lastTimeStamp = getTimeStampNs();
    }
}
