package org.kamiblue.client.gui.mc

import net.minecraft.client.gui.*
import net.minecraft.realms.RealmsBridge
import net.minecraft.util.text.TextFormatting
import org.kamiblue.client.module.modules.misc.AntiDisconnect
import org.kamiblue.client.util.color.ColorConverter
import org.kamiblue.client.util.text.format

class KamiGuiAntiDisconnect : GuiScreen() {

    private var disconnectCount = AntiDisconnect.presses.value
    private var button = GuiButton(1, width / 2 - 100, 230, buttonText)
    private val buttonText get() = TextFormatting.RED format "Press me $disconnectCount time(s) to disconnect."

    override fun initGui() {
        super.initGui()
        button = GuiButton(1, width / 2 - 100, 230, buttonText)
        buttonList.add(GuiButton(0, width / 2 - 100, 200, "Back to Game"))
        buttonList.add(button)
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> {
                mc.displayGuiScreen(null)
            }
            1 -> {
                if (disconnectCount > 1) {
                    disconnectCount--
                    button.displayString = buttonText
                } else {
                    button.enabled = false

                    when {
                        mc.isIntegratedServerRunning -> {
                            mc.displayGuiScreen(GuiWorldSelection(GuiMainMenu()))
                        }
                        mc.isConnectedToRealms -> {
                            RealmsBridge().switchToRealms(GuiMainMenu())
                        }
                        else -> {
                            mc.displayGuiScreen(GuiMultiplayer(GuiMainMenu()))
                        }
                    }

                    mc.world.sendQuittingDisconnectingPacket()
                    mc.loadWorld(null)
                }
            }
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        drawCenteredString(fontRenderer, "Disconnect Confirmation", width / 2, 40, ColorConverter.rgbToHex(155, 144, 255))
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

}
