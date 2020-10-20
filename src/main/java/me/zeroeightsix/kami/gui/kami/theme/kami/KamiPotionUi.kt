package me.zeroeightsix.kami.gui.kami.theme.kami

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.gui.kami.component.Potions
import me.zeroeightsix.kami.gui.rgui.component.AlignedComponent
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI
import me.zeroeightsix.kami.util.LagCompensator
import me.zeroeightsix.kami.util.color.ColorConverter.hexToRgb
import me.zeroeightsix.kami.util.graphics.font.FontRenderAdapter
import me.zeroeightsix.kami.util.text.RomanNumerals
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.I18n
import net.minecraft.potion.PotionEffect
import java.util.*
import java.util.concurrent.TimeUnit

class KamiPotionUi : AbstractComponentUI<Potions>() {
    private val mc = Minecraft.getMinecraft()

    override fun renderComponent(component: Potions) {
        if (mc.player == null) return

        val potions = ArrayList<PotionInfo>()
        for ((potion, potionEffect) in mc.player.activePotionMap) {
            potions.add(PotionInfo(potionEffect, I18n.format(potion.name), potionEffect.amplifier))
        }

        val textHeight = FontRenderAdapter.getFontHeight() + 2
        var posY = 1

        for (potion in potions) {
            val color = hexToRgb(potion.potionEffect.potion.liquidColor)
            val text = potion.formattedName(component.alignment == AlignedComponent.Alignment.RIGHT)
            val lineWidth = FontRenderAdapter.getStringWidth(text)
            FontRenderAdapter.drawString(text, getAlignmentX(lineWidth, component), posY.toFloat(), true, color)
            posY += textHeight.toInt()
        }

        component.height = posY
    }

    private fun getAlignmentX(width: Float, component: Potions) = when (component.alignment) {
        AlignedComponent.Alignment.RIGHT -> component.width - width
        AlignedComponent.Alignment.CENTER -> component.width / 2.0f - width / 2.0f
        else -> 0.0f
    }

    override fun handleSizeComponent(component: Potions) {
        component.width = 100
        component.height = 100
    }

    private data class PotionInfo(val potionEffect: PotionEffect, val name: String, val amplifier: Int) {
        /**
         * Formats name and time based on UI alignment
         */
        fun formattedName(right: Boolean) =
                if (right) "${KamiMod.colour}7${formattedTimeLeft()}${KamiMod.colour}r ${formattedName()}"
                else "${formattedName()} ${KamiMod.colour}7${formattedTimeLeft()}"

        /**
         * (min:secs)
         */
        private fun formattedTimeLeft(): String {
            val compensatedDuration: Long = (potionEffect.duration / LagCompensator.tickRate).toLong() // Convert type after calculation for more accurate result
            val min = TimeUnit.SECONDS.toMinutes(compensatedDuration)
            val secs = TimeUnit.SECONDS.toSeconds(compensatedDuration) - min * 60
            return String.format("(%d:%02d)", min, secs)
        }

        /**
         * Returns the fully formatted potion name without the time
         * Speed II
         */
        private fun formattedName() = "$name ${RomanNumerals.numberToRoman(amplifier + 1)}"
    }
}