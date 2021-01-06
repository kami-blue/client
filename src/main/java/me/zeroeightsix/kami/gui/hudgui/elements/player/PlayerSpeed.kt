package me.zeroeightsix.kami.gui.hudgui.elements.player

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.setting.GuiConfig.setting
import me.zeroeightsix.kami.util.InfoCalculator
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.commons.interfaces.DisplayEnum
import org.kamiblue.commons.utils.MathUtils
import java.util.*

object PlayerSpeed : LabelHud(
    name = "PlayerSpeed",
    category = Category.PLAYER,
    description = "Player movement speed"
) {

    private val speedUnit by setting("SpeedUnit", SpeedUnit.MPS)
    private val averageSpeedTime by setting("AverageSpeedTime", 1.0f, 0.25f..5.0f, 0.25f)

    @Suppress("UNUSED")
    private enum class SpeedUnit(override val displayName: String, val multiplier: Double) : DisplayEnum {
        MPS("m/s", 1.0),
        KMH("km/h", 3.6),
        MPH("mph", 2.237) // Monkey Americans
    }

    private val speedList = ArrayDeque<Double>()

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase == TickEvent.Phase.END && visible) updateSpeedList()
        }
    }

    override fun SafeClientEvent.updateText() {
        var averageSpeed = if (speedList.isEmpty()) 0.0 else speedList.sum() / speedList.size

        averageSpeed *= speedUnit.multiplier
        averageSpeed = MathUtils.round(averageSpeed, 2)

        displayText.add(averageSpeed.toString(), primaryColor)
        displayText.add(speedUnit.displayName, secondaryColor)
    }

    private fun updateSpeedList() {
        val speed = InfoCalculator.speed(false)

        if (speed > 0.0 || mc.player.ticksExisted % 4 == 0) {
            speedList.add(speed) // Only adding it every 4 ticks if speed is 0
        } else {
            speedList.poll()
        }

        while (speedList.size > averageSpeedTime * 20.0f) speedList.pollFirst()
    }

}