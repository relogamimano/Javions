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
        int vns =  Bits.extractUInt(rawMessage.payload(), 0, 10);
        int dns = Bits.extractUInt(rawMessage.payload(),10,1);

        if ( vns == 0 || dns == 0 ){
            return null;
        }

        int dew = Bits.extractUInt(rawMessage.payload(), 21, 1);
        int vew = Bits.extractUInt(rawMessage.payload(), 11, 10);
        int vx;
        int vy;

        if ( st == 2 ){
            vx = ( dew == 1? 1: -1)*4*(vew -1);
            vy = ( dns == 1? 1: -1)*4*(vns -1);
        } else {
            vx = ( dew == 1? 1: -1)*(vew - 1 );
            vy = ( dns == 1? 1: -1)*(vns - 1 );
        }

        double speed = Math.hypot(vx,vy);
        double angle = Math.atan2(vy,vx);
        if ( angle <0 ){
            angle = angle+2*Math.PI;
        }

        return new AirborneVelocityMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(),
                convert(speed, Units.Speed.KNOT, Speed.KILOMETER_PER_HOUR)* (Length.KILOMETER/60*2),
                convertTo(angle, Units.Angle.RADIAN));
    }

    private static AirborneVelocityMessage airSpeed( RawMessage rawMessage, int st){
        int sh = Bits.extractUInt(rawMessage.payload(), 21, 1 );
        double cap;
        double as;
        if ( sh == 1 ){
            cap = convert(Bits.extractUInt(rawMessage.payload(), 11,10)/Math.pow(2,10), Angle.TURN, Angle.RADIAN);
            as = ( st == 3 ? 1 : 4)*(Bits.extractUInt(rawMessage.payload(), 0, 10) - 1);
            if ( as == 0 ){
                return null;
            } else return new AirborneVelocityMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(),
                    convert(as, Speed.KNOT, Speed.KILOMETER_PER_HOUR)*(Length.KILOMETER/60*60),
                    convertFrom(cap, Angle.TURN));
        } else {
            return null;
    }
}}

