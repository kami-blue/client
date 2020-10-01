package me.zeroeightsix.kami.util

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.util.text.RomanNumerals
import net.minecraft.potion.PotionEffect
import java.util.concurrent.TimeUnit

data class PotionInfo(val name: String, val amplifier: Int, val potionEffect: PotionEffect) {
    /**
     * (min:secs)
     */
    fun formattedTimeLeft(): String {
        val compensatedDuration: Long = (potionEffect.duration / LagCompensator.tickRate).toLong() // Convert type after calculation for more accurate result
        val min = TimeUnit.SECONDS.toMinutes(compensatedDuration)
        val secs = TimeUnit.SECONDS.toSeconds(compensatedDuration) - min * 60
        return String.format("(%d:%02d)", min, secs)
    }

    /**
     * Returns the fully formatted potion name without the time
     * Speed II
     */
    fun formattedName(): String {
        return "$name ${RomanNumerals.numberToRoman(amplifier + 1)}"
    }

    /**
     * Formats name and time based on UI alignment
     */
    fun formattedName(right: Boolean): String {
        return if (right) {
            "${KamiMod.colour}7${formattedTimeLeft()}${KamiMod.colour}r ${formattedName()}"
        } else {
            "${formattedName()} ${KamiMod.colour}7${formattedTimeLeft()}"
        }
    }
}
