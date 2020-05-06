package me.zeroeightsix.kami.util;

import com.google.gson.annotations.SerializedName;

/**
 * @author wnuke
 * Created by wnuke on 17/04/20
 */

public class CoordinateInfo {
    @SerializedName("position")
    public Coordinate xyz;
    @SerializedName("name")
    public String name;
    @SerializedName("time")
    public String time;
    @SerializedName("date")
    public String date;

    public CoordinateInfo(int x, int y, int z, String nameSet, String timeSet) {
        xyz = new Coordinate(x, y, z);
        name = nameSet;
        time = timeSet;
    }

    public CoordinateInfo(Coordinate pos, String nameSet, String timeSet) {
        xyz = pos;
        name = nameSet;
        time = timeSet;
    }

    public Coordinate getPos() {
        return xyz;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }
}
