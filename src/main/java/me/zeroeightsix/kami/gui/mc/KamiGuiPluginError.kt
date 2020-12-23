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
    private var missingPluginNames = mutableListOf<String>()
    private val unsupportedKamiPluginsMap = hashMapOf<String, String>()
    private val unloadedPluginNames = mutableListOf<String>()

    private var offset = (45 - PluginManager.unloadablePluginMap.size * 10).coerceAtLeast(10)
    private val message = "The following plugins could not be loaded: ${unloadedPluginNames.joinToString()}"

    override fun initGui() {
        super.initGui()

        buttonList.add(GuiButton(0, 50, height - 38, width / 2 - 55, 20, "Open Plugins Folder"))
        buttonList.add(GuiButton(1, width / 2 + 5, height - 38, width / 2 - 55, 20, "Continue"))

        PluginManager.unloadablePluginMap.filter { it.value.compareTo(PluginManager.PluginErrorReason.REQUIRED_PLUGIN) == 0 }.forEach { entry ->
            entry.key.requiredPlugins.forEach {
                if (!PluginManager.loadedPlugins.containsName(it)) {
                    missingPluginNames.add(it)
                }
            }
        }

        PluginManager.unloadablePluginMap.filter { it.value.compareTo(PluginManager.PluginErrorReason.UNSUPPORTED_KAMI) == 0 }.forEach {
            unsupportedKamiPluginsMap[it.key.name] = it.key.minKamiVersion
        }

        PluginManager.unloadablePluginMap.forEach {
            unloadedPluginNames.add(it.key.name)
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()

        drawCenteredString(fontRenderer, message, width / 2, offset, ColorConverter.rgbToHex(155, 144, 255))

        offset += 50

        if (unsupportedKamiPluginsMap.isNotEmpty()) {
            drawCenteredString(fontRenderer, "These plugins require newer versions of KAMI Blue:", width / 2, offset, 0xFFFFFF)

            offset += 10

            for (plugin in unsupportedKamiPluginsMap) {
                offset += 15

                drawCenteredString(fontRenderer, "- ${plugin.key} (Requires KAMI Blue version ${plugin.value})", width / 2, offset, 0xFF5555)
            }

            offset += 35
        }

        if (missingPluginNames.isNotEmpty()) {
            missingPluginNames = missingPluginNames.distinct().toMutableList()

            drawCenteredString(fontRenderer, "These required plugins were not loaded:", width / 2, offset, 0xFFFFFF)

            offset += 10

            for (missingPlugin in missingPluginNames) {
                offset += 15

                drawCenteredString(fontRenderer, "- $missingPlugin", width / 2, offset, 0xFF5555)
            }
        }

        offset = (45 - PluginManager.unloadablePluginMap.size * 10).coerceAtLeast(10)

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun actionPerformed(button: GuiButton) {
        if (button.id == 0) Desktop.getDesktop().open(File(PluginManager.pluginPath))
        if (button.id == 1) mc.displayGuiScreen(prevScreen)
    }
}