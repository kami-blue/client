package me.zeroeightsix.kami.util;

import com.google.gson.annotations.SerializedName;

public class Coordinate {
    @SerializedName("x")
    public int x;
    @SerializedName("y")
    public int y;
    @SerializedName("z")
    public int z;

    public Coordinate(int x, int y, int z) {
        this.setX(x);
        this.setY(y);
        this.setZ(z);
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public int getZ() {
        return z;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setZ(int z) {
        this.z = z;
    }
}
