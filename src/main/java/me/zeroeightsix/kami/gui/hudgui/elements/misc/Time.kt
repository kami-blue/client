package me.zeroeightsix.kami.gui.hudgui.elements.misc

import me.zeroeightsix.kami.gui.hudgui.HudElement
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.setting.GuiConfig.setting
import me.zeroeightsix.kami.util.TimeUtils

@HudElement.Info(
        category = HudElement.Category.MISC,
        description = "System date and time"
)
object Time : LabelHud("Time") {

    private val showDate = setting("ShowDate", true)
    private val showTime = setting("ShowTime", true)
    private val dateFormat = setting("DateFormat", TimeUtils.DateFormat.DDMMYY, { showDate.value })
    private val timeFormat = setting("TimeFormat", TimeUtils.TimeFormat.HHMM, { showTime.value })
    private val timeUnit = setting("TimeUnit", TimeUtils.TimeUnit.H12, { showTime.value })

    override fun updateText() {
        displayText.addLine(TimeUtils.getDate(dateFormat.value))
        displayText.addLine(TimeUtils.getTime(timeFormat.value, timeUnit.value))
    }

}