package org.kamiblue.client.gui.hudgui.elements.misc

import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.gui.hudgui.LabelHud
import org.kamiblue.client.setting.GuiConfig.setting
import org.kamiblue.client.util.TimeUtils

object Time : LabelHud(
    name = "Time",
    category = Category.MISC,
    description = "System date and time"
) {

    private val showDate = setting("ShowDate", true)
    private val showTime = setting("ShowTime", true)
    private val dateFormat = setting("DateFormat", TimeUtils.DateFormat.DDMMYY, { showDate.value })
    private val timeFormat = setting("TimeFormat", TimeUtils.TimeFormat.HHMM, { showTime.value })
    private val timeUnit = setting("TimeUnit", TimeUtils.TimeUnit.H12, { showTime.value })

    override fun SafeClientEvent.updateText() {
        if (showDate.value) displayText.addLine(TimeUtils.getDate(dateFormat.value))
        if (showTime.value) displayText.addLine(TimeUtils.getTime(timeFormat.value, timeUnit.value))
    }

}