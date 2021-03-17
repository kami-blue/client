package org.kamiblue.client.gui.hudgui.elements.world

import org.apache.commons.lang3.time.DurationFormatUtils
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.gui.hudgui.LabelHud
import org.kamiblue.commons.interfaces.DisplayEnum

internal object WorldTime : LabelHud(
    name = "WorldTime",
    category = Category.WORLD,
    description = "Time in the Minecraft world"
) {

    private val displayMode by setting("Display Mode", DisplayMode.H24)
    private val fromMidNight by setting("From Midnight", true, { displayMode == DisplayMode.REAL_TIME || displayMode == DisplayMode.TICKS })

    private enum class DisplayMode(override val displayName: String) : DisplayEnum {
        H12("12-Hours"),
        H24("24-Hours"),
        REAL_TIME("Real Time"),
        TICKS("Ticks"), // Dummy format
    }

    override fun SafeClientEvent.updateText() {
        displayText.add("World Time ", secondaryColor)

        val ticks = getWorldTimeTicks()

        when (displayMode) {
            DisplayMode.H12 -> {
                var ticksHalf = ticks % 12000L
                if (ticksHalf < 1000L) ticksHalf += 12000L // Hacky way to display 12:00 instead of 00:00

                val millisHalf = ticksHalf * 3600L
                val timeString = DurationFormatUtils.formatDuration(millisHalf, "HH:mm")

                val period = if (ticks < 12000L) "AM" else "PM"

                displayText.add(timeString, primaryColor)
                displayText.add(period, secondaryColor)
            }
            DisplayMode.H24 -> {
                val millis = ticks * 3600L
                val timeString = DurationFormatUtils.formatDuration(millis, "HH:mm")

                displayText.add(timeString, primaryColor)
            }
            DisplayMode.REAL_TIME -> {
                val realTimeMillis = ticks * 50L
                val timeString = DurationFormatUtils.formatDuration(realTimeMillis, "mm:ss")

                displayText.add(timeString, primaryColor)
            }
            DisplayMode.TICKS -> {
                displayText.add("$ticks", primaryColor)
                displayText.add("ticks", secondaryColor)
            }
        }
    }

    private fun SafeClientEvent.getWorldTimeTicks() =
        if (fromMidNight && (displayMode == DisplayMode.H12 || displayMode == DisplayMode.H24)) {
            Math.floorMod(world.worldTime - 18000L, 24000L)
        } else {
            world.worldTime
        }

}