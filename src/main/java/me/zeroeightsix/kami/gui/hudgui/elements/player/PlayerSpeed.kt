package me.zeroeightsix.kami.gui.hudgui.elements.player

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.setting.GuiConfig.setting
import me.zeroeightsix.kami.util.InfoCalculator
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.commons.utils.MathUtils
import org.kamiblue.event.listener.listener
import java.util.*

object PlayerSpeed : LabelHud(
    name = "PlayerSpeed",
    category = Category.PLAYER,
    description = "Player movement speed"
) {

    private val speedUnit = setting("SpeedUnit", SpeedUnit.MPS)
    private val averageSpeedTime = setting("AverageSpeedTime", 1.0f, 0.25f..5.0f, 0.25f)

    @Suppress("UNUSED")
    private enum class SpeedUnit(val displayName: String) {
        MPS("m/s"),
        KMH("km/h"),
        MPH("mph")
    }

    private val speedList = ArrayDeque<Double>()

    init {
        listener<SafeTickEvent> {
            if (it.phase == TickEvent.Phase.END) updateSpeedList()
        }
    }

    override fun updateText() {
        var averageSpeed = if (speedList.isEmpty()) 0.0 else speedList.sum() / speedList.size

        if (speedUnit.value == SpeedUnit.KMH) {
            averageSpeed *= 3.6
        } else if (speedUnit.value == SpeedUnit.MPH) {
            averageSpeed *= 2.237
        }

        averageSpeed = MathUtils.round(averageSpeed, 2)

        displayText.add(averageSpeed.toString(), primaryColor)
        displayText.add(speedUnit.value.displayName, secondaryColor)
    }

    private fun updateSpeedList() {
        val speed = InfoCalculator.speed(false)

        if (speed > 0.0 || mc.player.ticksExisted % 4 == 0) {
            speedList.add(speed) // Only adding it every 4 ticks if speed is 0
        } else {
            speedList.poll()
        }

        while (speedList.size > averageSpeedTime.value * 20.0f) speedList.pollFirst()
    }

}