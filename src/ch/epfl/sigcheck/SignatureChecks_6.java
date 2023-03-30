package ch.epfl.sigcheck;

// Attention : cette classe n'est *pas* un test JUnit, et son code n'est
// pas destiné à être exécuté. Son seul but est de vérifier, autant que
// possible, que les noms et les types des différentes entités à définir
// pour cette étape du projet sont corrects.

final class SignatureChecks_6 {
    private SignatureChecks_6() {}

    void checkAirborneVelocityMessage() throws Exception {
        v01 = new ch.epfl.javions.adsb.AirborneVelocityMessage(v02, v03, v04, v04);
        v01 = ch.epfl.javions.adsb.AirborneVelocityMessage.of(v05);
        v07 = v01.equals(v06);
        v08 = v01.hashCode();
        v03 = v01.icaoAddress();
        v04 = v01.speed();
        v02 = v01.timeStampNs();
        v09 = v01.toString();
        v04 = v01.trackOrHeading();
    }

    void checkAircraftStateAccumulator() throws Exception {
        v10 = new ch.epfl.javions.adsb.AircraftStateAccumulator<>(v11);
        v11 = v10.stateSetter();
        v10.update(v12);
    }

    void checkMessageParser() throws Exception {
        v12 = ch.epfl.javions.adsb.MessageParser.parse(v05);
    }

    ch.epfl.javions.adsb.AirborneVelocityMessage v01;
    long v02;
    ch.epfl.javions.aircraft.IcaoAddress v03;
    double v04;
    ch.epfl.javions.adsb.RawMessage v05;
    java.lang.Object v06;
    boolean v07;
    int v08;
    java.lang.String v09;
    ch.epfl.javions.adsb.AircraftStateAccumulator<ch.epfl.javions.adsb.AircraftStateSetter> v10;
    ch.epfl.javions.adsb.AircraftStateSetter v11;
    ch.epfl.javions.adsb.Message v12;
}
