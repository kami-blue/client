package org.kamiblue.client.gui.hudgui.elements.misc

import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.gui.hudgui.LabelHud
import org.kamiblue.client.util.TickTimer
import org.kamiblue.client.util.TimeUnit
import org.kamiblue.client.util.WebUtils
import org.kamiblue.client.util.threads.safeListener

internal object Queue2B2T : LabelHud(
    name = "2B2T Queue",
    category = Category.MISC,
    description = "Length of 2B2T Queue"
) {
    private var queueURL = "https://2bqueue.info/queue"
    private val waitTime = TickTimer(TimeUnit.SECONDS)
    var initialGet = WebUtils.getUrlContents(queueURL).replace("\n", "").split("\"")
    val fixData = Regex("[^A-Za-z0-9 ]") //There's probably an easier way, but it's 2 am
    var priority = fixData.replace(initialGet[2], "")
    var standard = fixData.replace(initialGet[4], "")

    init{
        safeListener<TickEvent.ClientTickEvent> {
            if (!waitTime.tick(60L)) return@safeListener
            priority = fixData.replace(getQueueData()[2], "")
            standard = fixData.replace(getQueueData()[4], "")
            waitTime.reset()
        }
    }
    override fun SafeClientEvent.updateText() {
        displayText.add(("Priority: $priority, Standard: $standard"), primaryColor)
    }

    fun getQueueData(): List<String> {
        return WebUtils.getUrlContents(queueURL).replace("\n", "").split("\"")
    }
}