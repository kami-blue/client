package me.zeroeightsix.kami.gui.hudgui.elements.misc

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.gui.hudgui.elements.combat.CombatItemCount
import me.zeroeightsix.kami.setting.GuiConfig.setting
import me.zeroeightsix.kami.util.TimeUtils

object Time : LabelHud(
    category = Category.MISC
) {

    private val showDate = setting(getTranslationKey("ShowDate"), true)
    private val showTime = setting(getTranslationKey("ShowTime"), true)
    private val dateFormat = setting(getTranslationKey("DateFormat"), TimeUtils.DateFormat.DDMMYY, { showDate.value })
    private val timeFormat = setting(getTranslationKey("TimeFormat"), TimeUtils.TimeFormat.HHMM, { showTime.value })
    private val timeUnit = setting(getTranslationKey("TimeUnit"), TimeUtils.TimeUnit.H12, { showTime.value })

    override fun SafeClientEvent.updateText() {
        if (showDate.value) displayText.addLine(TimeUtils.getDate(dateFormat.value))
        if (showTime.value) displayText.addLine(TimeUtils.getTime(timeFormat.value, timeUnit.value))
    }

}