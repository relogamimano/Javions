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

    private static final int PAY_LOAD = 19;
    private static final int SUB_TYPE_START = 48;
    private static final int VNS_START = 21;
    private static final int SH_START= 42;
    private static final int DIRECTION_SIZE = 1;
    private static final int VELOCITY_SIZE = 10;
    private static final int DNS_START = VNS_START+VELOCITY_SIZE;
    private static final int EW_VELOCITY = DNS_START+DIRECTION_SIZE;
    private static final int EW_DIRECTION = EW_VELOCITY + VELOCITY_SIZE;
    
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
        int st = Bits.extractUInt(rawMessage.payload(), SUB_TYPE_START , 3);
        if ( rawMessage.typeCode() != PAY_LOAD ){
            return null;
        }
        return switch (st) {
            case 1, 2 -> groundSpeed(rawMessage, st);
            case 3, 4 -> airSpeed(rawMessage, st);
            default -> null;
        };

    }


    private static AirborneVelocityMessage groundSpeed(RawMessage rawMessage, int st){
        int vns =  Bits.extractUInt(rawMessage.payload(),VNS_START, VELOCITY_SIZE);
        int vew = Bits.extractUInt(rawMessage.payload(), EW_VELOCITY, VELOCITY_SIZE);


        if ( vns == 0 || vew == 0 ){
            return null;
        }

        int dns = Bits.extractUInt(rawMessage.payload(),DNS_START,DIRECTION_SIZE);
        int dew = Bits.extractUInt(rawMessage.payload(), EW_DIRECTION, DIRECTION_SIZE);
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
        int sh = Bits.extractUInt(rawMessage.payload(), SH_START, DIRECTION_SIZE );
        double cap;
        double as = 0;
        if ( sh == 1 ){
            cap = convert(Bits.extractUInt(rawMessage.payload(), EW_VELOCITY,VELOCITY_SIZE)/Math.pow(2,10),
                    Angle.TURN, Angle.RADIAN);
            as = ( st == 3 ? 1 : 4)*(Bits.extractUInt(rawMessage.payload(), VNS_START, VELOCITY_SIZE) - 1);
            if ( as <= 0 ){
                return null;
            } else return new AirborneVelocityMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(),
                    convertFrom(as, Speed.KNOT), cap);
        } else {
            return null;
    }
}}
