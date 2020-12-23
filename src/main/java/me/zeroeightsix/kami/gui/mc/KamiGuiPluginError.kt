package me.zeroeightsix.kami.gui.mc

import me.zeroeightsix.kami.plugin.PluginManager
import me.zeroeightsix.kami.util.color.ColorConverter
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import java.awt.Desktop
import java.nio.file.Paths

class KamiGuiPluginError : GuiScreen() {
    override fun initGui() {
        super.initGui()

        buttonList.add(GuiButton(0, 50, height - 38, width / 2 - 55, 20, "Open Plugins Folder"))
        buttonList.add(GuiButton(1, width / 2 + 5, height - 38, width / 2 - 55, 20, "Close Game"))
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        var missingPluginNames = mutableListOf<String>()
        val unsupportedKamiPluginsMap = hashMapOf<String, String>()
        val unloadedPluginNames = mutableListOf<String>()

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

        drawDefaultBackground()

        var offset = (45 - PluginManager.unloadablePluginMap.size * 10).coerceAtLeast(10)
        val message = "The following plugins could not be loaded: ${unloadedPluginNames.joinToString()}"

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

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun actionPerformed(button: GuiButton) {
        if (button.id == 0) Desktop.getDesktop().open(Paths.get(PluginManager.pluginPath).toFile())
        if (button.id == 1) mc.shutdown()
    }
}