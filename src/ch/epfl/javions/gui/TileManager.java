package ch.epfl.javions.gui;

import javafx.scene.image.Image;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

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
    private final Path discCache;
    private final String serverAddress;
    //    private final LinkedHashMap<TileId, Image> memoryCache = new LinkedHashMap<>(MAX_CAPACITY, LOAD_FACTOR, LRU_ORDER);
    private final LinkedHashMap<Path, Image> memoryCache = new LinkedHashMap<>(MAX_CAPACITY, LOAD_FACTOR, LRU_ORDER) {
        protected boolean removeEldestEntry(Map.Entry<Path, Image> eldest) {
            return size() > MAX_CAPACITY;
        }
    };
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
         */
        public TileId{
            checkArgument(isValid(zoom, x, y));
        }
        public static boolean isValid(int zoom, int x, int y) {
            return (zoom >= MapParameters.MIN_ZOOM && zoom <= MapParameters.MAX_ZOOM)
                    && (x>=0 && x<=(1<<zoom)-1)
                    && (y>=0 && y<=(1<<zoom)-1);
        }
    }

    /**
     * Constructor of TileManager
     * @param filePath  UNIX Path of the disc cache directory
     * @param serverAddress     online server address
     */
    public TileManager(Path filePath, String serverAddress) {
        this.discCache = filePath;
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
        Path dirPath = discCache.resolve(String.valueOf(tileId.zoom))
                .resolve(String.valueOf(tileId.x));
        checkArgument(TileId.isValid(tileId.zoom(), tileId.x(), tileId.y()));
        Path imagePath = dirPath.resolve(tileId.y+".png");
        File imageFile = imagePath.toFile();

        //check if image is already stored in memory cache
        if (memoryCache.containsKey(imagePath)){
            return memoryCache.get(imagePath);
        } else {
            //if not, check in the disc cache if it contains the image
            if (Files.exists(imagePath)) {
                //if so, put it in the memory cache et return it

                FileInputStream fileIn = new FileInputStream(imagePath.toString());
                Image image = new Image(fileIn);
                memoryCache.put(imagePath, image);
                return image;
            } else {
                //if not, get it from the server, put it in the memory and disc cache, and return it
                Files.createDirectories(dirPath);
                String urlString = "https://"+serverAddress+"/"+tileId.zoom+"/"+tileId.x+"/"+tileId.y+".png";
                URL u = new URL(urlString);
                URLConnection c = u.openConnection();
                c.setRequestProperty("User-Agent", "JaVelo");

                try (InputStream i = c.getInputStream(); OutputStream a =
                        new FileOutputStream(imageFile)) {
                    i.transferTo(a);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                InputStream i = new FileInputStream(imageFile);
                Image image = new Image(i);
                memoryCache.put(imagePath,image);
                return image;
            }
        }
    }
}