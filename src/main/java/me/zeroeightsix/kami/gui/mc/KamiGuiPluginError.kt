package me.zeroeightsix.kami.gui.mc

import me.zeroeightsix.kami.plugin.Plugin
import me.zeroeightsix.kami.plugin.PluginError
import me.zeroeightsix.kami.plugin.PluginManager
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.math.BlockPos
import net.minecraftforge.event.world.NoteBlockEvent
import java.awt.Desktop
import java.io.File
import java.util.*

class KamiGuiPluginError(
    private val prevScreen: GuiScreen?,
    pluginErrors: List<Pair<Plugin, PluginError>>
) : GuiScreen() {

    private val errorPlugins: String
    private val unsupportedPlugins: Set<String>
    private val missingPlugins: Set<String>

    init {
        val builder = StringBuilder()
        val unsupported = TreeSet<String>()
        val missing = TreeSet<String>()

        for ((index, pair) in pluginErrors.withIndex()) {
            builder.append(pair.first.name)
            if (index != pluginErrors.size - 1) builder.append(", ")

            if (pair.second == PluginError.UNSUPPORTED_KAMI) {
                unsupported.add("${pair.first.name} (${pair.first.minKamiVersion})")
            } else {
                missing.addAll(pair.first.requiredPlugins.filter { !PluginManager.loadedPlugins.containsName(it) })
            }
        }

        errorPlugins = builder.toString()
        unsupportedPlugins = unsupported
        missingPlugins = missing
    }

    override fun initGui() {
        super.initGui()
        buttonList.add(GuiButton(0, 50, height - 38, width / 2 - 55, 20, "Open Plugins Folder"))
        buttonList.add(GuiButton(1, width / 2 + 5, height - 38, width / 2 - 55, 20, "Continue"))
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val map = EnumMap<NoteBlockEvent.Instrument, Array<BlockPos>>(NoteBlockEvent.Instrument::class.java)
        map.getOrPut(NoteBlockEvent.Instrument.BASSDRUM) { Array(25) { BlockPos.ORIGIN } }


        drawDefaultBackground()
        super.drawScreen(mouseX, mouseY, partialTicks)

        GlStateManager.pushMatrix()
        GlStateManager.translate(width / 2.0f, 50.0f, 0.0f)

        drawCenteredString(fontRenderer, warning, 0, 0, 0x909BFF) // 155, 144, 255
        GlStateManager.translate(0.0f, fontRenderer.FONT_HEIGHT + 5.0f, 0.0f)

        drawCenteredString(fontRenderer, errorPlugins, 0, 0, 0xFF5555) // 255, 85, 85
        GlStateManager.translate(0.0f, 30.0f, 0.0f)

        drawList(unsupported, unsupportedPlugins)
        drawList(missing, missingPlugins)

        GlStateManager.popMatrix()
    }

    private fun drawList(title: String, list: Set<String>) {
        if (title.isNotEmpty()) {
            drawCenteredString(fontRenderer, title, 0, 0, 0xFFFFFF) // 255, 255, 255
            GlStateManager.translate(0.0f, 3.0f, 0.0f)

            list.forEach {
                GlStateManager.translate(0.0f, fontRenderer.FONT_HEIGHT + 2.0f, 0.0f)
                drawCenteredString(fontRenderer, it, 0, 0, 0xFF5555) // 255, 85, 85
            }

            GlStateManager.translate(0.0f, 30.0f, 0.0f)
        }
    }

    override fun actionPerformed(button: GuiButton) {
        if (button.id == 0) Desktop.getDesktop().open(File(PluginManager.pluginPath))
        if (button.id == 1) mc.displayGuiScreen(prevScreen)
    }

    private companion object {
        const val warning = "The following plugins could not be loaded:"
        const val unsupported = "These plugins require newer versions of KAMI Blue:"
        const val missing = "These required plugins were not loaded:"
    }

}