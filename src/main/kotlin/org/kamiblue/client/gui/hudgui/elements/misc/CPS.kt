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

    private var clicks = HashSet<Long>()

    override fun SafeClientEvent.updateText() {
        displayText.add(clicks.size.toString(), primaryColor)

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
            clicks.removeIf { it < System.currentTimeMillis() - 1000 }
        }
    }

}