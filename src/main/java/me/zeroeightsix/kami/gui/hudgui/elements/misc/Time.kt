package me.zeroeightsix.kami.gui.hudgui.elements.misc

import me.zeroeightsix.kami.gui.hudgui.HudElement
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.setting.GuiConfig.setting
import me.zeroeightsix.kami.util.TimeUtilsNew

@HudElement.Info(
        category = HudElement.Category.MISC,
        description = "System date and time"
)
object Time : LabelHud("Time") {

    private val showDate = setting("ShowDate", true)
    private val showTime = setting("ShowTime", true)
    private val dateFormat = setting("DateFormat", TimeUtilsNew.DateFormat.DDMMYY, { showDate.value })
    private val timeFormat = setting("TimeFormat", TimeUtilsNew.TimeFormat.HHMM, { showTime.value })
    private val timeUnit = setting("TimeUnit", TimeUtilsNew.TimeUnit.H12, { showTime.value })

    override fun updateText() {
        displayText.addLine(TimeUtilsNew.getDate(dateFormat.value))
        displayText.addLine(TimeUtilsNew.getTime(timeFormat.value, timeUnit.value))
    }

}