package me.zeroeightsix.kami.gui.mc

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.util.WebUtils.openWebLink
import me.zeroeightsix.kami.util.color.ColorConverter
import net.minecraft.client.gui.*
import net.minecraft.util.text.TextFormatting
import java.net.URI

/**
 * Created by Dewy on 09/04/2020
 */
class KamiGuiUpdateNotification(private val buttonId: Int) : GuiScreen() {

    private val message = "A newer release of KAMI Blue is available (" + KamiMod.latest + ")."

    override fun initGui() {
        super.initGui()
        buttonList.add(GuiButton(0, width / 2 - 100, 200, "Download Latest (Recommended)"))
        buttonList.add(GuiButton(1, width / 2 - 100, 230, "${TextFormatting.RED}Use Current Version"))
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        drawCenteredString(fontRenderer, title, width / 2, 80, ColorConverter.rgbToHex(155, 144, 255))
        drawCenteredString(fontRenderer, message, width / 2, 110, ColorConverter.rgbToHex(255, 255, 255))

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun actionPerformed(button: GuiButton) {
        if (button.id == 0) openWebLink(URI(KamiMod.WEBSITE_LINK + "/download"))

        val screen = if (buttonId == 1) GuiWorldSelection(GuiMainMenu()) // Single
        else GuiMultiplayer(GuiMainMenu()) // Multi

        mc.displayGuiScreen(screen)
    }

    private companion object {
        const val title = "KAMI Blue Update"
    }
}