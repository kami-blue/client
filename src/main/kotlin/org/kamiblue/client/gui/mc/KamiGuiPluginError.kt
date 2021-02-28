package org.kamiblue.client.gui.mc

import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.math.BlockPos
import net.minecraftforge.event.world.NoteBlockEvent
import org.kamiblue.client.plugin.PluginError
import org.kamiblue.client.plugin.PluginLoader
import org.kamiblue.client.plugin.PluginManager
import java.awt.Desktop
import java.io.File
import java.util.*

internal class KamiGuiPluginError(
    private val prevScreen: GuiScreen?,
    pluginErrors: List<Pair<PluginLoader, PluginError>>
) : GuiScreen() {

    private val errorPlugins: String
    private val hotReload = TreeSet<String>()
    private val duplicate = TreeSet<String>()
    private val unsupported = TreeSet<String>()
    private val missing = TreeSet<String>()

    init {
        val builder = StringBuilder()
        for ((index, pair) in pluginErrors.withIndex()) {
            builder.append(pair.first.toString())
            if (index != pluginErrors.size - 1) builder.append(", ")

            when (pair.second) {
                PluginError.HOT_RELOAD -> {
                    hotReload.add(pair.first.toString())
                }
                PluginError.DUPLICATE -> {
                    duplicate.add(pair.first.toString())
                }
                PluginError.UNSUPPORTED -> {
                    unsupported.add("${pair.first} (${pair.first.info.minApiVersion})")
                }
                PluginError.REQUIRED_PLUGIN -> {
                    missing.addAll(pair.first.info.requiredPlugins.filter { !PluginManager.loadedPlugins.containsName(it) })
                }
            }
        }

        errorPlugins = builder.toString()
    }

    override fun initGui() {
        super.initGui()
        buttonList.add(GuiButton(0, width / 2 - 210, height - 40, "Open Plugins Folder"))
        buttonList.add(GuiButton(1, width / 2 + 10, height - 40, "Continue"))
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

        drawList(hotReloadMessage, hotReload)
        drawList(duplicateMessage, duplicate)
        drawList(unsupportedMessage, unsupported)
        drawList(missingMessage, missing)

        GlStateManager.popMatrix()
    }

    private fun drawList(title: String, list: Set<String>) {
        if (list.isNotEmpty()) {
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
        const val hotReloadMessage = "These plugins could not be hot reloaded:"
        const val duplicateMessage = "These plugins were duplicate:"
        const val unsupportedMessage = "These plugins require newer versions of KAMI Blue:"
        const val missingMessage = "These required plugins were not loaded:"
    }

}