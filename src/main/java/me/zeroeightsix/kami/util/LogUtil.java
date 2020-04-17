package me.zeroeightsix.kami.util;

import net.minecraft.client.Minecraft;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author S-B99
 * Created by S-B99 on 18/02/20
 * Updated by wnuke on 14/04/20
 */
public class LogUtil {
    public static final String coordsLogFilename = "KAMIBlueCoords.txt";
    public static int[] getCurrentCoord(boolean chunk) {
        Minecraft mc = Minecraft.getMinecraft();
        int[] currentCoords = {(int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ};
        if (chunk) {
            return new int[] {currentCoords[0]/16, currentCoords[1]/16, currentCoords[2]/16};
        } else {
            return currentCoords;
        }
    }

    public static int[] writePlayerCoords(String locationName, boolean chunk) {
        int[] coords = getCurrentCoord(chunk);
        writeCoords(coords, locationName, Boolean.toString(chunk));
        return coords;
    }

    public static void writeCoords(int[] xyz, String locationName, String chunk) {
        try {
            FileWriter fW = new FileWriter(coordsLogFilename, true);
            fW.write(formatter(xyz[0], xyz[1], xyz[2], locationName, chunk));
            fW.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String formatter(int x, int y, int z, String locationName, String chunk) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        String time = sdf.format(new Date());
        return "x: " + x + ", y:" + y + ", z:" + z + ", chunk: " + chunk + ", name: \"" + locationName + "\", date: \"" + time + "\"\n";
    }
}
