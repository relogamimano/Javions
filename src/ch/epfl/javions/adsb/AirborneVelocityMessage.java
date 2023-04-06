package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;


import java.util.Objects;

import static ch.epfl.javions.Units.*;

/**
 * Velocity message of the flight
 * @param timeStampNs message time stamps
 * @param icaoAddress ICAO address
 * @param speed speed of the aircraft
 * @param trackOrHeading direction of the aircraft
 * @author: Sofia Henriques Garfo (346298)
 * @author: Romeo Maignal (360568)
 */
public record AirborneVelocityMessage(long timeStampNs, IcaoAddress icaoAddress, double speed, double trackOrHeading)
        implements Message {


    /**
     * Constructor that verifies that all arguments are valid
     * @param timeStampNs message time stamps
     * @param icaoAddress ICAO address
     * @param speed speed of the aircraft
     * @param trackOrHeading direction of the aircraft
     * @throws IllegalArgumentException if the time stamps, the speed or the direction are negative
     * @throws NullPointerException Icao address is null
     */
    public AirborneVelocityMessage{
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument( timeStampNs >= 0 && speed >= 0 && trackOrHeading>= 0 );
    }


    /**
     *
     * @return message time stapms
     */
    @Override
    public long timeStampNs() {
        return timeStampNs;
    }

    /**
     * @return ICAO address
     */
    @Override
    public IcaoAddress icaoAddress(){
        return icaoAddress;
    }

    /**
     * Extracts the velocity message from the raw message
     * @param rawMessage
     * @return velocity message
     */
    public static AirborneVelocityMessage of(RawMessage rawMessage){
        int st = Bits.extractUInt(rawMessage.payload(), 48 , 3);
        if ( rawMessage.typeCode() != 19 ){
            return null;
        }
        return switch (st) {
            case 1, 2 -> groundSpeed(rawMessage, st);
            case 3, 4 -> airSpeed(rawMessage, st);
            default -> null;
        };

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
            vx = ( dew == 1? -1: 1)*4*(vew -1);
            vy = ( dns == 1? -1: 1)*4*(vns -1);
        } else {
            vx = ( dew == 1? -1: 1)*(vew - 1 );
            vy = ( dns == 1? -1: 1)*(vns - 1 );
        }

        double speed = Math.hypot(vx,vy);
        double angle = Math.atan2(vx,vy);
        if ( angle <0 ){
            angle = angle+2*Math.PI;
        }

        return new AirborneVelocityMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(),
                convertFrom(speed, Units.Speed.KNOT),
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
                    convertFrom(as, Speed.KNOT),
                    cap);
        } else {
            return null;
    }
}}
