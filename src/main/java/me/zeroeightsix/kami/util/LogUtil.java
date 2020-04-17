package me.zeroeightsix.kami.util;

import net.minecraft.client.Minecraft;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

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
        return "x: " + x + ", y: " + y + ", z: " + z + ", chunk: " + chunk + ", name: \"" + locationName + "\", date: \"" + time + "\"\n";
    }
    public static Coord[] coordsLogToArray() throws IOException {
        Coord[] coordsArray = new Coord[lineCount(coordsLogFilename)];
        try {
            File coordsFile = new File(coordsLogFilename);
            Scanner coordsReader = new Scanner(coordsFile);
            int line = 0;
            while (coordsReader.hasNextLine()) {
                String coordsRaw = coordsReader.nextLine();
                String[] split1 = coordsRaw.split("x: ")[1].split(", y: ");
                String[] split2 = split1[1].split(", z: ");
                String[] split3 = split2[1].split(", chunk: ");
                String[] split4 = split3[1].split(", name: ");
                String[] split5 = split4[1].split(", date: ");
                String[] split6 = split5[1].split("\n");
                Coord lineCoord = new Coord();
                lineCoord.x = Integer.parseInt(split1[0]);
                lineCoord.y = Integer.parseInt(split2[0]);
                lineCoord.z = Integer.parseInt(split3[0]);
                lineCoord.chunk = Boolean.parseBoolean(split3[0]);
                lineCoord.name = split5[0];
                lineCoord.time = split6[0];
                lineCoord.date = split6[1];
                coordsArray[line] = lineCoord;
                line++;
            }
            coordsReader.close();
            return coordsArray;
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
            return null;
        }
    }
    public static int lineCount(String filename) throws IOException {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("file.txt"));
            int lines = 0;
            while (reader.readLine() != null) lines++;
            reader.close();
            return lines;
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
            return 0;
        }
    }
}

class Coord {
    int x;
    int y;
    int z;
    boolean chunk;
    String name;
    String time;
    String date;
}


