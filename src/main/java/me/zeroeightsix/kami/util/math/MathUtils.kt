package me.zeroeightsix.kami.util.math

import me.zeroeightsix.kami.util.math.RotationUtils.normalizeAngle
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.BlockPos
import kotlin.math.*

/**
 * Created by Dewy on the 17th of April, 2020
 * Updated by Xiaro on 18/08/20
 */
object MathUtils {
    @JvmStatic
    fun mcPlayerPosFloored(mc: Minecraft): BlockPos {
        return BlockPos(floor(mc.player.posX), floor(mc.player.posY), floor(mc.player.posZ))
    }

    @JvmStatic
    fun round(value: Float, places: Int): Double {
        val scale = 10.0.pow(places.toDouble())
        return round(value * scale) / scale
    }

    @JvmStatic
    fun round(value: Double, places: Int): Double {
        val scale = 10.0.pow(places.toDouble())
        return round(value * scale) / scale
    }

    @JvmStatic
    fun isNumberEven(i: Int): Boolean {
        return i and 1 == 0
    }

    @JvmStatic
    fun reverseNumber(num: Int, min: Int, max: Int): Int {
        return max + min - num
    }

    @JvmStatic
    fun isBetween(min: Int, max: Int, value: Int): Boolean {
        return value in min..max
    }

    @JvmStatic
    fun isBetween(min: Double, max: Double, value: Double): Boolean {
        return value in min..max
    }

    @JvmStatic
    fun getPlayerCardinal(player: EntityPlayer): Cardinal {
        return if (isBetween(-22.5, 22.5, normalizeAngle(player.rotationYaw.toDouble()))) {
            Cardinal.POS_Z
        } else if (isBetween(22.6, 67.5, normalizeAngle(player.rotationYaw.toDouble()))) {
            Cardinal.NEG_X_POS_Z
        } else if (isBetween(67.6, 112.5, normalizeAngle(player.rotationYaw.toDouble()))) {
            Cardinal.NEG_X
        } else if (isBetween(112.6, 157.5, normalizeAngle(player.rotationYaw.toDouble()))) {
            Cardinal.NEG_X_NEG_Z
        } else if (normalizeAngle(player.rotationYaw.toDouble()) >= 157.6 || normalizeAngle(player.rotationYaw.toDouble()) <= -157.5) {
            Cardinal.NEG_Z
        } else if (isBetween(-157.6, -112.5, normalizeAngle(player.rotationYaw.toDouble()))) {
            Cardinal.POS_X_NEG_Z
        } else if (isBetween(-112.5, -67.5, normalizeAngle(player.rotationYaw.toDouble()))) {
            Cardinal.POS_X
        } else if (isBetween(-67.6, -22.6, normalizeAngle(player.rotationYaw.toDouble()))) {
            Cardinal.POS_X_POS_Z
        } else {
            Cardinal.ERROR
        }
    }

    @JvmStatic
    fun getPlayerMainCardinal(player: EntityPlayer): CardinalMain {
        return when (Character.toUpperCase(player.horizontalFacing.toString()[0])) {
            'N' -> CardinalMain.NEG_Z
            'S' -> CardinalMain.POS_Z
            'E' -> CardinalMain.POS_X
            'W' -> CardinalMain.NEG_X
            else -> CardinalMain.NULL
        }
    }

    @JvmStatic
    fun convertRange(valueIn: Int, minIn: Int, maxIn: Int, minOut: Int, maxOut: Int): Int {
        return convertRange(valueIn.toDouble(), minIn.toDouble(), maxIn.toDouble(), minOut.toDouble(), maxOut.toDouble()).toInt()
    }

    @JvmStatic
    fun convertRange(valueIn: Float, minIn: Float, maxIn: Float, minOut: Float, maxOut: Float): Float {
        return convertRange(valueIn.toDouble(), minIn.toDouble(), maxIn.toDouble(), minOut.toDouble(), maxOut.toDouble()).toFloat()
    }

    @JvmStatic
    fun convertRange(valueIn: Double, minIn: Double, maxIn: Double, minOut: Double, maxOut: Double): Double {
        val rangeIn = maxIn - minIn
        val rangeOut = maxOut - minOut
        val convertedIn = (valueIn - minIn) * (rangeOut / rangeIn) + minOut
        val actualMin = min(minOut, maxOut)
        val actualMax = max(minOut, maxOut)
        return min(max(convertedIn, actualMin), actualMax)
    }

    enum class Cardinal(@JvmField var cardinalName: String) {
        POS_Z("+Z"),
        NEG_X_POS_Z("-X / +Z"),
        NEG_X("-X"),
        NEG_X_NEG_Z("-X / -Z"),
        NEG_Z("-Z"),
        POS_X_NEG_Z("+X / -Z"),
        POS_X("+X"),
        POS_X_POS_Z("+X / +Z"),
        ERROR("ERROR_CALC_DIRECT");
    }

    enum class CardinalMain(@JvmField var cardinalName: String) {
        POS_Z("+Z"),
        NEG_X("-X"),
        NEG_Z("-Z"),
        POS_X("+X"),
        NULL("N/A");
    }
}