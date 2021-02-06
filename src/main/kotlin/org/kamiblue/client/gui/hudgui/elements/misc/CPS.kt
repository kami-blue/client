package org.kamiblue.client.gui.hudgui.elements.misc

import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.MouseClickEvent
import org.kamiblue.client.gui.hudgui.LabelHud
import org.kamiblue.client.setting.GuiConfig.setting
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.event.listener.asyncListener

object CPS : LabelHud(
    name = "CPS",
    category = Category.MISC,
    description = "Display your clicks per second."
) {

    var clicks = HashSet<Long>()

    private val measurementTime by setting("Measurement Time (m)", 1000, 500..5000, 100)


    override fun SafeClientEvent.updateText() {
        displayText.add("%.2f".format(clicks.size.toDouble() / (measurementTime.toDouble() / 1000)), primaryColor)

        displayText.add("CPS", secondaryColor)
    }

    init {
        asyncListener<MouseClickEvent> {
            if ((it.buttonState) and (it.mouseButton == 0)) {
                clicks.add(System.currentTimeMillis())
            }
        }

        safeListener<TickEvent.ClientTickEvent> {
            // This needs to happen whenever running else it will continue to show a score after clicking has stopped.
            clicks.removeIf { it < System.currentTimeMillis() - measurementTime }
        }
    }

}