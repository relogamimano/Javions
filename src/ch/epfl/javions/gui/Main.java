package ch.epfl.javions.gui;


import ch.epfl.javions.ByteString;
import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

import static ch.epfl.javions.Units.*;
import static java.nio.file.Path.of;

public class Main extends Application {
    private final ConcurrentLinkedQueue<RawMessage> queue = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) {launch(args);}

    static List<RawMessage> readAllMessages(String fileName)
            throws IOException {
        List<RawMessage> list = new ArrayList<>();
        try (DataInputStream s = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(fileName)))) {
            byte[] bytes = new byte[RawMessage.LENGTH];

            while (true) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                RawMessage rawMessage = new RawMessage(timeStampNs, new ByteString(bytes));
                list.add(rawMessage);
            }
        } catch (EOFException e) {
            return list;
        }
    }

    /**
     * Recursive method used to launch the program by calling itself at its end.
     * @param primaryStage the primary stage for this application, onto which
     * the application scene can be set.
     * Applications may create other stages, if needed, but they will not be
     * primary stages.
     * @throws Exception if the reading of the input stream faces issues
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        long bootTime = System.nanoTime();
        List<String> raw = getParameters().getRaw();
        final String str = raw.isEmpty() ? null : raw.get(0);
        Supplier<RawMessage> supplier = (str == null)
                ? airSpySupplier()
                : defaultMessageSupplier(str);

        Thread messagesThread = new Thread(() -> {
            while(true) {
                long elapsedTime = System.nanoTime() - bootTime;
                RawMessage rm = supplier.get();
                if (Objects.isNull(rm)) continue;
                if (rm.timeStampNs() > elapsedTime) {
                    try {
                        Thread.sleep((long) convertFrom(rm.timeStampNs() - elapsedTime, NANO / MILLI));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                queue.add(supplier.get());
            }
        });
        messagesThread.setDaemon(true);
        messagesThread.start();

        Path tileCache = of("tile-cache");
        TileManager tm = new TileManager(tileCache, "tile.openstreetmap.org");
        MapParameters mp = new MapParameters(8, 33530, 23070);
        URL u = getClass().getResource("/aircraft.zip");
        assert u != null;
        Path p = Path.of(u.toURI());
        AircraftDatabase db = new AircraftDatabase(p.toString());

        AircraftStateManager asm = new AircraftStateManager(db);
        ObjectProperty<ObservableAircraftState> sap = new SimpleObjectProperty<>();
        BaseMapController bmc = new BaseMapController(tm, mp);
        AircraftController ac = new AircraftController(mp, asm.states(), sap);
        AircraftTableController atc = new AircraftTableController(asm.states(), sap);
        StackPane aircraftView = new StackPane(bmc.pane(), ac.pane());
        StackPane table = new StackPane(atc.pane());
//        BorderPane aircraftConsole = new SplitPane(atc.pane(), );

        SplitPane splitPane = new SplitPane(aircraftView, table);
        splitPane.setOrientation(Orientation.VERTICAL);
        primaryStage.setScene(new Scene(splitPane));
        primaryStage.setTitle("Javions");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();


        // Aircraft animation
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                try {
                    while (!queue.isEmpty()) {
                        RawMessage rawMessage = queue.poll();
                        Message m = Objects.isNull(rawMessage) ? null : MessageParser.parse(rawMessage);
                        if (m != null) {
                            //slc
                            asm.updateWithMessage(m);
                        }
                        asm.purge();
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }.start();
    }

    private Supplier<RawMessage> defaultMessageSupplier(String str) throws IOException {
        List<RawMessage> l = readAllMessages(str);
        Iterator<RawMessage> it = l.iterator();
        return it::next;
    }

    private Supplier<RawMessage> airSpySupplier() throws IOException {
        AdsbDemodulator adsbDemodulator = new AdsbDemodulator(System.in);
        return () -> {
            try {
                return adsbDemodulator.nextMessage();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
