package org.kamiblue.client.gui.hudgui.elements.misc

import net.minecraftforge.fml.common.gameevent.InputEvent
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.RunGameLoopEvent
import org.kamiblue.client.gui.hudgui.LabelHud
import org.kamiblue.client.util.TickTimer
import org.kamiblue.client.util.graphics.AnimationUtils
import org.kamiblue.event.listener.listener
import org.lwjgl.input.Mouse

internal object CPS : LabelHud(
    name = "CPS",
    category = Category.MISC,
    description = "Display your clicks per second."
) {

    private val averageSpeedTime by setting("Average Speed Time", 2.0f, 1.0f..5.0f, 0.1f, description = "The period of time to measure, in seconds")

    private val timer = TickTimer()
    private val clicks = ArrayDeque<Long>()
    private var currentCps = 0.0f
    private var prevCps = 0.0f

    init {
        listener<InputEvent.MouseInputEvent> {
            if (Mouse.getEventButtonState() && Mouse.getEventButton() == 0) {
                clicks.add(System.currentTimeMillis())
            }
        }

        listener<RunGameLoopEvent.Render> {
            if ((currentCps == 0.0f && clicks.size > 0) || timer.tick(1000L)) {
                val removeTime = System.currentTimeMillis() - (averageSpeedTime * 1000.0f).toLong()
                while (clicks.isNotEmpty() && clicks.first() < removeTime) {
                    clicks.removeFirst()
                }

                prevCps = currentCps
                currentCps = clicks.size / averageSpeedTime
            }
        }
    }

    override fun SafeClientEvent.updateText() {
        val deltaTime = AnimationUtils.toDeltaTimeFloat(timer.time) / 1000.0f
        val cps = (prevCps + (currentCps - prevCps) * deltaTime)

        displayText.add("%.2f".format(cps), primaryColor)
        displayText.add("CPS", secondaryColor)
    }
}