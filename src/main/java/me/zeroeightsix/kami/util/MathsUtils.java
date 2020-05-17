package me.zeroeightsix.kami.util;

import net.minecraft.client.Minecraft;

/**
 * Created by Dewy on the 17th of April, 2020
 */
public class MathsUtils {

    public static double normalizeAngle(double angleIn) {
        angleIn = angleIn % 360.0D;

        if (angleIn >= 180.0D) {
            angleIn -= 360.0D;
        }

        if (angleIn < -180.0D) {
            angleIn += 360.0D;
        }

        return angleIn;
    }

    public static double round(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }

    public static boolean isNumberEven(int i) {
        return (i & 1) == 0;
    }

    public static int reverseNumber(int num, int min, int max) {
        return (max + min) - num;
    }

    public static boolean isBetween(int min, int max, int value) {
        return value >= min && value <= max;
    }

    public static boolean isBetween(double min, double max, double value) {
        return value >= min && value <= max;
    }

    public static Cardinal getPlayerCardinal(Minecraft mc) { // TODO: switch to isBetween
        if (normalizeAngle(mc.player.rotationYaw) >= -22.5 && normalizeAngle(mc.player.rotationYaw) <= 22.5) {
            return Cardinal.POS_Z;
        } else if (normalizeAngle(mc.player.rotationYaw) >= 22.6 && normalizeAngle(mc.player.rotationYaw) <= 67.5) {
            return Cardinal.NEG_X_POS_Z;
        } else if (normalizeAngle(mc.player.rotationYaw) >= 67.6 && normalizeAngle(mc.player.rotationYaw) <= 112.5) {
            return Cardinal.NEG_X;
        } else if (normalizeAngle(mc.player.rotationYaw) >= 112.6 && normalizeAngle(mc.player.rotationYaw) <= 157.5) {
            return Cardinal.NEG_X_NEG_Z;
        } else if (normalizeAngle(mc.player.rotationYaw) >= 157.6 || normalizeAngle(mc.player.rotationYaw) <= -157.5) {
            return Cardinal.NEG_Z;
        } else if (normalizeAngle(mc.player.rotationYaw) >= -157.6 && normalizeAngle(mc.player.rotationYaw) <= -112.5) {
            return Cardinal.POS_X_NEG_Z;
        } else if (normalizeAngle(mc.player.rotationYaw) >= -112.6 && normalizeAngle(mc.player.rotationYaw) <= -67.5) {
            return Cardinal.POS_X;
        } else if (normalizeAngle(mc.player.rotationYaw) >= -67.6 && normalizeAngle(mc.player.rotationYaw) <= -22.6) {
            return Cardinal.POS_X_POS_Z;
        } else {
            return null;
        }
    }

    // prototype version using inBetween: throws NullPointerException and returns null
//    public static Cardinal getPlayerCardinal(Minecraft mc) {
//        if (isBetween(-22.5, 22.5, normalizeAngle(mc.player.rotationYaw))) {
//            return Cardinal.POS_Z;
//        } else if (isBetween(22.6, 67.5, normalizeAngle(mc.player.rotationYaw))) {
//            return Cardinal.NEG_X_POS_Z;
//        } else if (isBetween(67.6, 112.5, normalizeAngle(mc.player.rotationYaw))) {
//            return Cardinal.NEG_X;
//        } else if (isBetween(112.6, 157.5, normalizeAngle(mc.player.rotationYaw))) {
//            return Cardinal.NEG_X_NEG_Z;
//        } else if (isBetween(157.6, -157.5, normalizeAngle(mc.player.rotationYaw))) {
//            return Cardinal.NEG_Z;
//        } else if (isBetween(-157.6, -112.5, normalizeAngle(mc.player.rotationYaw))) {
//            return Cardinal.POS_X_NEG_Z;
//        } else if (isBetween(-112.5, -67.5, normalizeAngle(mc.player.rotationYaw))) {
//            return Cardinal.POS_X;
//        } else if (isBetween(-67.6, -22.6, normalizeAngle(mc.player.rotationYaw))) {
//            return Cardinal.POS_X_POS_Z;
//        } else {
//            return null;
//        }
//    }

    public enum Cardinal {
        POS_Z("+Z"),
        NEG_X_POS_Z("-X / +Z"),
        NEG_X("-X"),
        NEG_X_NEG_Z("-X / -Z"),
        NEG_Z("-Z"),
        POS_X_NEG_Z("+X / -Z"),
        POS_X("+X"),
        POS_X_POS_Z("+X / +Z");

        public String cardinalName;

        Cardinal(String cardinalName) {
            this.cardinalName = cardinalName;
        }
    }
}
