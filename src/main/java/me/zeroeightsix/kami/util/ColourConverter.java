package me.zeroeightsix.kami.util;

/**
 * @author dominikaaaa
 * Updated by dominikaaaa on 04/03/20
 */
public class ColourConverter {
    public static float toF(int i) { return i / 255f; }

    public static float toF(double d) { return (float) (d / 255f); }

    public static int rgbToInt(int r, int g, int b, int a) { return (r << 16) | (g << 8) | (b) | (a << 24); }

    public static int rgbToInt(int r, int g, int b) { return (r << 16) | (g << 8) | (b); }

    // settingsToInt(r.getValue(), g.getValue(), b.getValue(), aBlock.getValue()
}
