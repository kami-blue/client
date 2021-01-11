package me.zeroeightsix.kami.gui.hudgui.elements.misc

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.gui.hudgui.elements.combat.CombatItemCount
import me.zeroeightsix.kami.setting.GuiConfig.setting

object MemoryUsage : LabelHud(
    category = Category.MISC
) {

    private val showAllocated = setting(getTranslationKey("ShowAllocated"), false)
    private val showMax = setting(getTranslationKey("ShowMax"), false)

    override fun SafeClientEvent.updateText() {
        val memory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576L
        displayText.add(memory.toString(), primaryColor)
        if (showAllocated.value) {
            val allocatedMemory = Runtime.getRuntime().totalMemory() / 1048576L
            displayText.add(allocatedMemory.toString(), primaryColor)
        }
        if (showMax.value) {
            val maxMemory = Runtime.getRuntime().maxMemory() / 1048576L
            displayText.add(maxMemory.toString(), primaryColor)
        }
        displayText.add("MB", secondaryColor)
    }

}