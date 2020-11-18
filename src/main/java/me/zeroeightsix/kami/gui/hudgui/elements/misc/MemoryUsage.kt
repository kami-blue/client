package me.zeroeightsix.kami.gui.hudgui.elements.misc

import me.zeroeightsix.kami.gui.hudgui.HudElement
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.setting.GuiConfig.setting

@HudElement.Info(
        category = HudElement.Category.MISC,
        description = "Display the used, allocated and max memory"
)
object MemoryUsage : LabelHud("MemoryUsage") {

    private val showAllocated = setting("ShowAllocated", false)
    private val showMax = setting("ShowMax", false)

    override fun updateText() {
        val memory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576L
        displayText.add(memory.toString(), primaryColor.value)
        if (showAllocated.value) {
            val allocatedMemory = Runtime.getRuntime().totalMemory() / 1048576L
            displayText.add(allocatedMemory.toString(), primaryColor.value)
        }
        if (showMax.value) {
            val maxMemory = Runtime.getRuntime().maxMemory() / 1048576L
            displayText.add(maxMemory.toString(), primaryColor.value)
        }
        displayText.add("MB", secondaryColor.value)
    }

}