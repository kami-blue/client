package org.kamiblue.client.gui.mc

import com.google.gson.JsonParser
import kotlinx.coroutines.launch
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiMainMenu
import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.text.TextFormatting
import org.kamiblue.client.KamiMod
import org.kamiblue.client.util.WebUtils
import org.kamiblue.client.util.color.ColorConverter
import org.kamiblue.client.util.threads.mainScope
import org.kamiblue.commons.utils.ConnectionUtils
import java.io.IOException

class KamiGuiUpdateNotification : GuiScreen() {

    private val message = "A newer release of KAMI Blue is available ($latest)."

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
        if (button.id == 0) WebUtils.openWebLink(KamiMod.WEBSITE_LINK + "/download")

        mc.displayGuiScreen(GuiMainMenu())
    }

    companion object {
        private const val title = "KAMI Blue Update"

        var latest: String? = null // latest version (null if no internet or exception occurred)
        var isLatest = false

        @JvmStatic
        fun updateCheck() {
            mainScope.launch {
                try {
                    KamiMod.LOG.info("Attempting KAMI Blue update check...")

                    val parser = JsonParser()
                    val rawJson = ConnectionUtils.requestRawJsonFrom(KamiMod.DOWNLOADS_API) {
                        throw it
                    }

                    latest = parser.parse(rawJson).asJsonObject.getAsJsonObject("stable")["name"].asString
                    isLatest = latest.equals(KamiMod.VERSION_MAJOR)

                    if (!isLatest) {
                        KamiMod.LOG.warn("You are running an outdated version of KAMI Blue.\nCurrent: ${KamiMod.VERSION_MAJOR}\nLatest: $latest")
                    } else {
                        KamiMod.LOG.info("Your KAMI Blue (" + KamiMod.VERSION_MAJOR + ") is up-to-date with the latest stable release.")
                    }
                } catch (e: IOException) {
                    KamiMod.LOG.error("Oes noes! An exception was thrown during the update check.", e)
                }
            }
        }
    }
}