package org.kamiblue.client.gui.mc

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import org.kamiblue.client.module.modules.combat.AutoLog
import org.kamiblue.client.util.threads.mainScope
import java.time.LocalTime

class KamiGuiDisconnected(private val reason: Array<String>, private val screen: GuiScreen, private val disable: Boolean, private val logoutTime: LocalTime) : GuiScreen() {

    override fun initGui() {
        super.initGui()

        buttonList.add(GuiButton(0, width / 2 - 100, 200, "Okay"))
        if (!disable) {
            buttonList.add(GuiButton(1, width / 2 - 100, 220, "Disable AutoLog"))
        } else {
            disable()
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)
        drawCenteredString(fontRenderer, "[AutoLog] Logged because:", width / 2, 80, 0x9B90FF)
        for ((index, reason) in reason.withIndex()) {
            drawCenteredString(fontRenderer, reason, width / 2, 94 + (14 * index), 0xFFFFFF)
        }

        drawCenteredString(fontRenderer, "Logged out at: $logoutTime", width / 2, 140, 0xFFFFFFF)

        if (!disable) drawCenteredString(fontRenderer, "Disabled AutoLog", width / 2, 224, 0xDE413C)
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {}

    override fun actionPerformed(button: GuiButton) {
        if (button.id == 0) mc.displayGuiScreen(screen)
        if (button.id == 1) {
            disable()
            buttonList.remove(button)
        }
    }

    private fun disable() {
        mainScope.launch {
            delay(250L)
            AutoLog.disable()
        }
    }

}
