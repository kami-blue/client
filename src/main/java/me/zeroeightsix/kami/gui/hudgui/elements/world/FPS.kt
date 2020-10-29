package me.zeroeightsix.kami.gui.hudgui.elements.world

import me.zeroeightsix.kami.gui.hudgui.HudElement
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.setting.GuiConfig.setting
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.roundToInt

@HudElement.Info(
        category = HudElement.Category.PLAYER,
        description = "Display game fps"
)
object FPS : LabelHud("FPS") {

    private val showAverage = setting("ShowAverage", true)

    private val fpsList = IntArray(5)
    private var index = 0
    private var tickCounter = 0
    private var fps = 0
    private var prevFps = 0
    private var averageFps = 0
    private var prevAverageFps = 0

    init {
        listener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.END) return@listener
            tickCounter = (tickCounter + 1) % 20
            if (tickCounter == 0) updateFpsList()
        }
    }

    override fun updateText() {
        val fps = (prevFps + (fps - prevFps) * (tickCounter / 20.0f)).roundToInt()

        val stringBuilder = StringBuilder(2)
        stringBuilder.append(fps.toString())
        if (showAverage.value) {
            val averageFps = (prevAverageFps + (averageFps - prevAverageFps) * (tickCounter / 20.0f)).roundToInt()
            stringBuilder.append("/$averageFps")
        }

        displayText.add(stringBuilder.toString())
        displayText.add("fps")
    }

    private fun updateFpsList() {
        prevFps = fps
        fps = Minecraft.debugFPS

        fpsList[index] = fps
        index = (index + 1) % 5

        if (showAverage.value) {
            prevAverageFps = averageFps
            averageFps = fpsList.average().roundToInt()
        }
    }

}