package me.zeroeightsix.kami.gui.hudgui.elements

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.gui.hudgui.HudElement
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.setting.GuiConfig.setting
import me.zeroeightsix.kami.util.InfoCalculator
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.graphics.font.FontRenderAdapter
import me.zeroeightsix.kami.util.math.MathUtils
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.*

@HudElement.Info(
        category = HudElement.Category.PLAYER,
        description = "Display player movement speed"
)
object PlayerSpeed : LabelHud("PlayerSpeed") {

    private val speedUnit = setting("SpeedUnit", SpeedUnit.MPS)
    private val averageSpeedTime = setting("AverageSpeedTime", 1.0f, 0.25f..5.0f, 0.25f)

    @Suppress("UNUSED")
    private enum class SpeedUnit(val displayName: String) {
        MPS("m/s"),
        KMH("km/h")
        // No retarded imperial unit here
    }

    override val maxWidth: Float get() = displayText.getWidth() + 4.0f
    override val maxHeight: Float get() = FontRenderAdapter.getFontHeight() + 4.0f

    private val speedList = ArrayDeque<Double>()

    init {
        listener<SafeTickEvent> {
            if (it.phase == TickEvent.Phase.END) updateSpeedList()
        }
    }

    override fun updateText() {
        var averageSpeed = if (speedList.isEmpty()) 0.0 else speedList.sum() / speedList.size
        if (speedUnit.value == SpeedUnit.KMH) averageSpeed *= 3.6
        averageSpeed = MathUtils.round(averageSpeed, 2)
        displayText.add(averageSpeed.toString())
        displayText.add(speedUnit.value.displayName)
    }

    private fun updateSpeedList() {
        val speed = InfoCalculator.speed(false)
        if (speed > 0.0 || mc.player.ticksExisted % 4 == 0) speedList.add(speed) // Only adding it every 4 ticks if speed is 0
        else speedList.poll()
        while (speedList.size > averageSpeedTime.value * 20.0f) speedList.pollFirst()
    }

}