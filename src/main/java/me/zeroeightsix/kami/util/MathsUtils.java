package me.zeroeightsix.kami.util;

/**
 * Created by Dewy on the 17th of April, 2020
 */
public class MathsUtils {

    public static double normalizeAngle(double angleIn) {
        while (angleIn <= -180.0) {
            angleIn += 360.0;
        }

        while (angleIn > 180.0) {
            angleIn -= 360.0;
        }

        return angleIn;
    }

    public static double round(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }

    public static boolean isNumberEven(int i) { return (i & 1) == 0; }

    public static int reverseNumber(int num, int min, int max) { return (max + min) - num; }
}
