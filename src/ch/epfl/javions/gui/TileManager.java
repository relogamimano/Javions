package ch.epfl.javions.gui;

import javafx.scene.image.Image;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashMap;

import static ch.epfl.javions.Preconditions.*;

/**
 * OSM tile manager
 * @author Sofia Henriques Garfo (346298)
 * @author Romeo Maignal (360568)
 */
public final class TileManager {
    private static final int MAX_CAPACITY = 100;
    private static final float LOAD_FACTOR = 0.75f;
    private static final boolean LRU_ORDER = true;
    private final Path discCachePath;
    private final String serverAddress;
    private final LinkedHashMap<TileId, Image> memoryCache = new LinkedHashMap<>(MAX_CAPACITY, LOAD_FACTOR, LRU_ORDER);;

    /**
     * Tile identity record
     * @param zoom  level of magnification
     * @param x     abscissa index of the tile
     * @param y     ordinate index of the tile
     */
    public record TileId(int zoom, int x, int y) {
        /**
         * Methode to check if the tile attributes are valid
         * @param zoom level of magnification
         * @param x    abscissa index of the tile
         * @param y    ordinate index of the tile
         * @return  validity of the tile
         */
        public TileId{
            checkArgument(isValid(zoom, x, y));
        }
        public static boolean isValid(int zoom, int x, int y) {
            return (zoom>=0 && zoom<=19)
                    && (x>=0 && x<=Math.scalb(1, zoom)-1)
                    && (y>=0 && y<=Math.scalb(1, zoom)-1);


        }
    }

    /**
     * Constructor of TileManager
     * @param filePath  UNIX Path of the disc cache directory
     * @param serverAddress     online server address
     */
    public TileManager(Path filePath, String serverAddress) {
        this.discCachePath = filePath;
        this.serverAddress = serverAddress;

    }

    /**
     * Image getter method : it takes a specific tile ID and look it up in the different image storing caches,
     * or download it from an online server.
     * @param tileId    Tile ID
     * @return  tile Image
     * @throws IOException if the input reading or the output writing fails
     */
    public Image imageForTileAt(TileId tileId) throws IOException {
        checkArgument(TileId.isValid(tileId.zoom(), tileId.x(), tileId.y()));

        Path localPath = Path.of(tileId.zoom + "/" + tileId.x + "/" + tileId.y + ".png") ;
        Path globalPath = Path.of(discCachePath + "/" + localPath);

        //check if image is already stored in memory cache
        if (memoryCache.containsKey(tileId)){// TODO: 02.05.23 contains ?
//            System.out.println("tile was extract from the memory cache");

            return memoryCache.get(tileId);
        } else {
            //if not, check in the disc cache if it contains the image
            if (Files.exists(globalPath)) {
                //if so, put it in the memory cache et return it

                Iterator<TileId> i = memoryCache.keySet().iterator();
                if (memoryCache.size() >= MAX_CAPACITY) {
                    memoryCache.remove(i.next());


                }
                FileInputStream fileIn = new FileInputStream(globalPath.toString());
                Image image = new Image(fileIn);
                memoryCache.put(tileId, image);// TODO: 02.05.23 put ?
//                System.out.println("tile was extracted from the disc cache");

                return image;
            } else {
                //if not, get it from the server, put it in the memory and disc cache, and return it
                URL u = new URL("https://" + serverAddress + "/" + localPath);
                URLConnection c = u.openConnection();
                c.setRequestProperty("User-Agent", "Javions");
                try (InputStream i = c.getInputStream()) {
                    Files.createDirectories( Path.of(globalPath.getParent() + "/"));
                    FileOutputStream fileOut = new FileOutputStream(globalPath.toString());
                    fileOut.write(i.readAllBytes());
//                    System.out.println("tile was extracted from the server");
                    return new Image(new ByteArrayInputStream( i.readAllBytes() ) );
                }
            }
        }
    }
}