package me.zeroeightsix.kami.gui.mc

import me.zeroeightsix.kami.module.modules.misc.AntiDisconnect
import me.zeroeightsix.kami.util.color.ColorConverter
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiMainMenu
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.gui.GuiScreen
import net.minecraft.realms.RealmsBridge
import net.minecraft.util.text.TextFormatting

class KamiGuiAntiDisconnect : GuiScreen() {

    private var disconnectCount = AntiDisconnect.presses.value
    private val button = GuiButton(1, width / 2 - 100, 230, buttonText)
    private val buttonText get() = TextFormatting.RED.toString() + "Press me $disconnectCount time(s) to disconnect."

    override fun initGui() {
        super.initGui()
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
                    mc.world.sendQuittingDisconnectingPacket()
                    mc.loadWorld(null)

                    when {
                        mc.isIntegratedServerRunning -> {
                            mc.displayGuiScreen(GuiMainMenu())
                        }
                        mc.isConnectedToRealms -> {
                            RealmsBridge().switchToRealms(GuiMainMenu())
                        }
                        else -> {
                            mc.displayGuiScreen(GuiMultiplayer(GuiMainMenu()))
                        }
                    }
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