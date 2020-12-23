package me.zeroeightsix.kami.gui.mc

import me.zeroeightsix.kami.plugin.PluginManager
import me.zeroeightsix.kami.util.color.ColorConverter
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import java.awt.Desktop
import java.io.File

class KamiGuiPluginError(
    private val prevScreen: GuiScreen?
) : GuiScreen() {
    private val missingPluginNames = HashSet<String>()
    private val unsupportedKamiPluginsMap = hashMapOf<String, String>()
    private val unloadedPluginNames = mutableListOf<String>()

    private val message = "The following plugins could not be loaded: ${unloadedPluginNames.joinToString()}"

    override fun initGui() {
        super.initGui()

        buttonList.add(GuiButton(0, 50, height - 38, width / 2 - 55, 20, "Open Plugins Folder"))
        buttonList.add(GuiButton(1, width / 2 + 5, height - 38, width / 2 - 55, 20, "Continue"))

        PluginManager.unloadablePluginMap.filter { it.value == PluginManager.PluginErrorReason.REQUIRED_PLUGIN }.forEach { entry ->
            entry.key.requiredPlugins.forEach {
                if (!PluginManager.loadedPlugins.containsName(it)) {
                    missingPluginNames.add(it)
                }
            }
        }

        PluginManager.unloadablePluginMap.filter { it.value == PluginManager.PluginErrorReason.UNSUPPORTED_KAMI }.forEach {
            unsupportedKamiPluginsMap[it.key.name] = it.key.minKamiVersion
        }

        PluginManager.unloadablePluginMap.forEach {
            unloadedPluginNames.add(it.key.name)
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()

        var offset = (45 - PluginManager.unloadablePluginMap.size * (fontRenderer.FONT_HEIGHT + 1)).coerceAtLeast(10)

        drawCenteredString(fontRenderer, message, width / 2, offset, ColorConverter.rgbToHex(155, 144, 255))

        offset += 50

        if (unsupportedKamiPluginsMap.isNotEmpty()) {
            drawCenteredString(fontRenderer, "These plugins require newer versions of KAMI Blue:", width / 2, offset, 0xFFFFFF)

            offset += fontRenderer.FONT_HEIGHT + 1

            for (plugin in unsupportedKamiPluginsMap) {
                offset += fontRenderer.FONT_HEIGHT + 6

                drawCenteredString(fontRenderer, "- ${plugin.key} (Requires KAMI Blue version ${plugin.value})", width / 2, offset, 0xFF5555)
            }

            offset += 35
        }

        if (missingPluginNames.isNotEmpty()) {
            drawCenteredString(fontRenderer, "These required plugins were not loaded:", width / 2, offset, 0xFFFFFF)

            offset += fontRenderer.FONT_HEIGHT + 1

            for (missingPlugin in missingPluginNames) {
                offset += fontRenderer.FONT_HEIGHT + 6

                drawCenteredString(fontRenderer, "- $missingPlugin", width / 2, offset, 0xFF5555)
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun actionPerformed(button: GuiButton) {
        if (button.id == 0) Desktop.getDesktop().open(File(PluginManager.pluginPath))
        if (button.id == 1) mc.displayGuiScreen(prevScreen)
    }
}