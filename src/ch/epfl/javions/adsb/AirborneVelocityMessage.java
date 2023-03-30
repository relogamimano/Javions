package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;


import java.util.Objects;

import static ch.epfl.javions.Units.*;

public record AirborneVelocityMessage(long timeStampNS, IcaoAddress icaoAddress, double speed, double trackOrHeading)
        implements Message {


    public AirborneVelocityMessage{
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument( timeStampNS >= 0 && speed >= 0 && trackOrHeading>= 0 );
    }

    @Override
    public long timeStampsNs() {
        return timeStampNS;
    }

    @Override
    public IcaoAddress icaoAddress(){
        return icaoAddress;
    }

    //qui retourne le message de vitesse en vol correspondant au message brut donné, ou null si le sous-type est invalide,
    //ou si la vitesse ou la direction de déplacement ne peuvent pas être déterminés.
    public static AirborneVelocityMessage of(RawMessage rawMessage){
        int st = Bits.extractUInt(rawMessage.payload(), 48 , 3);
        if ( rawMessage.typeCode() != 19 ){
            return null;
        }
        switch (st){
            case 1 : case 2 :
                return groundSpeed(rawMessage, st);
            case 3 : case 4 :
                return airSpeed(rawMessage, st);
            default:
                return null;
        }

    }

    private static AirborneVelocityMessage groundSpeed(RawMessage rawMessage, int st){
        int vns =  Bits.extractUInt(rawMessage.payload(), 21, 10);
        int vew = Bits.extractUInt(rawMessage.payload(), 32, 10);


        if ( vns == 0 || vew == 0 ){
            return null;
        }

        int dns = Bits.extractUInt(rawMessage.payload(),31,1);
        int dew = Bits.extractUInt(rawMessage.payload(), 42, 1);
        int vx;
        int vy;

        if ( st == 2 ){
            vx = ( dew == 1? 1: -1)*4*(vew -1);
            vy = ( dns == 1? 1: -1)*4*(vns -1);
        } else {
            vx = ( dew == 1? -1: 1)*(vew - 1 );
            vy = ( dns == 1? -1: 1)*(vns - 1 );
        }

        double speed = Math.hypot(vx,vy);
        double angle = Math.atan2(vy,vx);
        if ( angle <0 ){
            angle = angle+2*Math.PI;
        }

        return new AirborneVelocityMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(),
                convert(speed, Units.Speed.KNOT, Speed.KILOMETER_PER_HOUR)* (Length.KILOMETER/3600),
                angle);
    }

    private static AirborneVelocityMessage airSpeed( RawMessage rawMessage, int st){
        int sh = Bits.extractUInt(rawMessage.payload(), 42, 1 );
        double cap;
        double as = 0;
        if ( sh == 1 ){
            cap = convert(Bits.extractUInt(rawMessage.payload(), 32,10)/Math.pow(2,10), Angle.TURN, Angle.RADIAN);
            as = ( st == 3 ? 1 : 4)*(Bits.extractUInt(rawMessage.payload(), 21, 10) - 1);
            if ( as == 0 ){
                return null;
            } else return new AirborneVelocityMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(),
                    convert(as, Speed.KNOT, Speed.KILOMETER_PER_HOUR)*((Length.KILOMETER)/3600),
                    cap);
        } else {
            return null;
    }
}}
/*
package ch.epfl.javions.adsb;
217.1759987875795
2.1469729376568147

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;
*/
/*
public record AirborneVelocityMessage(long timeStampNs, IcaoAddress icaoAddress, double speed, double trackOrHeading) implements Message {

    public AirborneVelocityMessage{
        if(icaoAddress == null){
            throw new NullPointerException();
        }
        Preconditions.checkArgument(timeStampNs() >= 0 && speed() >= 0 && trackOrHeading() >= 0);
    }
    private static double getGroundSpeed(long payload, int Vew, int Vns){

        if(Vns == 0  Vew == 0){
            return -1;
        }

        return Math.hypot(Vns - 1, Vew - 1);

    }

    private static double getGroundAngle(long payload,int Dew, int Dns, int Vew, int Vns) {

        if (Dew == 0) {
            if (Dns == 0) {
                // direction North-East
                return Math.atan2(Vew, Vns);
            } else {
                //direction South-East
                return Math.atan2(Vew, -Vns);
            }
        } else {
            //add full turn to get positive angle
            if (Dns == 0) {
                //direction North-West
                return Math.atan2(-Vew, Vns) + 2 * Math.PI;
            } else {
                //direction South-West
                return Math.atan2(-Vew, -Vns) + 2 * Math.PI;
            }
        }
    }

/*public static AirborneVelocityMessage of(RawMessage rawMessage){
    int subType = Bits.extractUInt(rawMessage.payload(),48,3);

    if(rawMessage.typeCode() != 19  subType < 1  subType > 4){
        return null;
    }
    double trackOrHeading;
    double speed;


    final int VNS = Bits.extractUInt(rawMessage.payload(),21,10);
    final int DNS = Bits.extractUInt(rawMessage.payload(),31, 1);
    final int VEW = Bits.extractUInt(rawMessage.payload(), 32, 10);
    final int DEW = Bits.extractUInt(rawMessage.payload(), 42,1);

    if(subType == 1  subType == 2){

        speed = getGroundSpeed(rawMessage.payload(),VEW,VNS);

        trackOrHeading = getGroundAngle(rawMessage.payload(),DEW,DNS,VEW,VNS);

    } else{
        final int SH = DEW;
        final int HDG = VEW;
        final int AS = VNS;

        speed = AS - 1;

        if(SH == 0){
            return null;
        }else{
            trackOrHeading = Units.convert(HDG/Math.scalb(1,10),Units.Angle.TURN, Units.Angle.RADIAN);
        }

    }

    if(subType == 2 || subType ==4){
        speed *= 4;
    }

    return new AirborneVelocityMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(),speed,trackOrHeading);

}














}
*/

