package me.zeroeightsix.kami.gui.hudgui.elements.client

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.setting.GuiConfig.setting

object Username : LabelHud(
    category = Category.CLIENT
) {

    private val prefix = setting(getTranslationKey("Prefix"), "Welcome")
    private val suffix = setting(getTranslationKey("Suffix"), "")

    override fun SafeClientEvent.updateText() {
        displayText.add(prefix.value, primaryColor)
        displayText.add(mc.session.username, secondaryColor)
        displayText.add(suffix.value, primaryColor)
    }

}