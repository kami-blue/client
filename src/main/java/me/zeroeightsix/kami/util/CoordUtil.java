package me.zeroeightsix.kami.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author dominikaaaa
 * Created by dominikaaaa on 18/02/20
 * Updated by wnuke on 14/04/20
 * Renamed by wnuke on 26/04/20 from LogUtil.java to CoordUtil.java
 */
public class CoordUtil {
    private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
    public static final String coordsLogFilename = "KAMIBlueCoords.json";
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static Coordinate getCurrentCoord() {
        Minecraft mc = Minecraft.getMinecraft();
        return new Coordinate((int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ);
    }

    public static Coordinate writePlayerCoords(String locationName) {
        Coordinate coords = getCurrentCoord();
        writeCoords(coords, locationName, coordsLogFilename);
        return coords;
    }

    public static void writeCoords(Coordinate xyz, String locationName, String filename) {
        try {
            ArrayList<CoordinateInfo> coords = readCoords(filename);
            coords.add(formatter(xyz, locationName));
            FileWriter writer = new FileWriter(filename);
            gson.toJson(coords, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean removeCoord(String name, String filename) {
        boolean removed = false;
        try {
            ArrayList<CoordinateInfo> coords = readCoords(filename);
            for (CoordinateInfo coord : coords) {
                if (coord.getName().equals(name)) {
                    coords.remove(coord);
                    removed = true;
                    break;
                }
            }
            FileWriter writer = new FileWriter(filename);
            gson.toJson(coords, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return removed;
    }

    private static CoordinateInfo formatter(Coordinate xyz, String locationName) {
        String time = sdf.format(new Date());
        return new CoordinateInfo(xyz, locationName, time);
    }
    public static ArrayList<CoordinateInfo> readCoords(String filename) {
        try {
            ArrayList<CoordinateInfo> coords;
            coords = gson.fromJson(new FileReader(filename), new TypeToken<ArrayList<CoordinateInfo>>(){}.getType());
            if (coords != null) {
                return coords;
            } else {
                return new ArrayList<>();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            try {
                File file = new File(filename);
                file.createNewFile();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            return new ArrayList<>();
        }
    }
}


