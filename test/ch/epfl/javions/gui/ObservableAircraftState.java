package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.adsb.AircraftStateAccumulator;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.beans.property.*;
import javafx.collections.ObservableList;

import java.lang.invoke.DelegatingMethodHandle$Holder;

import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.unmodifiableObservableList;

public final class ObservableAircraftState implements AircraftStateSetter {


    private long lastTimeStamp ;
    private final IcaoAddress icaoAddress;
    private final AircraftData aircraftData;
    private ObjectProperty<CallSign> callSign = new SimpleObjectProperty<>();
    private LongProperty timeSampsNs = new SimpleLongProperty();
    private ObjectProperty<GeoPos> position = new SimpleObjectProperty<>();
    private DoubleProperty trackOrHeading = new SimpleDoubleProperty();
    private IntegerProperty category = new SimpleIntegerProperty();
    private DoubleProperty altitude = new SimpleDoubleProperty();
    private DoubleProperty velocity = new SimpleDoubleProperty();
    private ObservableList<AirbornePos> trajectoryList = observableArrayList();
    private ObservableList<AirbornePos> unmodifiableTrajectoryList = unmodifiableObservableList(trajectoryList); //final?

    public ObservableList<AirbornePos> trajectoryPropriety(){
        return unmodifiableTrajectoryList;
    }

    public ObservableList<AirbornePos> getTrajectoryList(){
        return trajectoryList;
    }
    
    public IcaoAddress getIcaoAddress(){
        return icaoAddress;
    }

    public AircraftData getAircraftData() {
        return aircraftData;
    }

    public ReadOnlyIntegerProperty categoryPropriety(){
        return category;
    }

    public int getCategory(){
        return category.get();
    }

    @Override
    public void setCategory(int category) {
        this.category.set(category);
    }

    public ReadOnlyLongProperty callSignsPropriety(){
        return timeSampsNs;
    }


    public CallSign getCallSign() {
        return callSign.get();
    }

    @Override
    public void setCallSign(CallSign callsSign) {
        this.callSign.set(callsSign);
    }


    public ReadOnlyObjectProperty<GeoPos> positionPropriety(){
        return position;
    }

    public GeoPos getPosition(){
        return position.get();
    }

    @Override
    public void setPosition(GeoPos position) {
        this.position.set(position);
        if( trajectoryList.isEmpty()){
            add( position, getAltitude());
        } else if ( trajectoryList.get(trajectoryList.size()-1).geopos() != position){
            add(position, getAltitude());
        }
        else if ( lastTimeStamp == getTimeStampNs()){
            trajectoryList.remove( trajectoryList.size()-1);
            add(position, getAltitude());
        }
    }



    public ReadOnlyDoubleProperty trackOrHeadingPropriety(){
        return trackOrHeading;
    }

    public double getTrackOrHeading() {
        return trackOrHeading.get();
    }

    @Override
    public void setTrackOrHeading(double trackOrHeading) {
        this.trackOrHeading.set(trackOrHeading);
    }

    public ReadOnlyDoubleProperty altitudePropriety(){
        return altitude;
    }
    public double getAltitude(){
        return altitude.get();
    }

    @Override
    public void setAltitude(double altitude) {
        this.altitude.set(altitude);
        if( trajectoryList.isEmpty()){
            add( getPosition(), altitude);
        }
        else if ( lastTimeStamp == getTimeStampNs()){  // in setAltitude and setPosition ou juste dans alt??????
            trajectoryList.remove( trajectoryList.size()-1);
            add(getPosition(), altitude);
        }

    }

    public ReadOnlyDoubleProperty velocityPropriety(){
        return velocity;
    }

    public double getVelocity() {
        return velocity.get();
    }

    @Override
    public void setVelocity(double velocity) {
        this.velocity.set(velocity);
    }

    public ReadOnlyLongProperty timeStampNsPropriety(){
        return timeSampsNs;
    }
    public long getTimeStampNs(){
        return timeSampsNs.get();
    }

    @Override
    public void setLastMessageTimeStampNs(long timeStampNs) {
        this.timeSampsNs.set(timeStampNs);
    }

    public record AirbornePos(GeoPos geopos, double altitude){ };


    public ObservableAircraftState(IcaoAddress address, AircraftData aircraftData){
        icaoAddress = address;
        this.aircraftData = aircraftData;
    }

    private void add( GeoPos geopos, double alt){
        trajectoryList.add(new AirbornePos(geopos, alt));
        lastTimeStamp = getTimeStampNs();
    }
}
