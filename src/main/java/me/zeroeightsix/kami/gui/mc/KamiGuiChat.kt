package me.zeroeightsix.kami.gui.mc

import me.zeroeightsix.kami.command.CommandManager
import me.zeroeightsix.kami.gui.kami.theme.kami.KamiGuiColors
import me.zeroeightsix.kami.mixin.extension.historyBuffer
import me.zeroeightsix.kami.mixin.extension.sentHistoryCursor
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.GlStateUtils
import me.zeroeightsix.kami.util.graphics.RenderUtils2D
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.math.Vec2d
import net.minecraft.client.gui.GuiChat
import java.util.*

open class KamiGuiChat(startStringIn: String, historyBufferIn: String, sentHistoryCursorIn: Int) : GuiChat(startStringIn) {

    init {
        historyBuffer = historyBufferIn
        sentHistoryCursor = sentHistoryCursorIn
    }

    private var predictString = ""

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        super.keyTyped(typedChar, keyCode)

        if (!inputField.text.startsWith(CommandManager.prefix.value)) {
            displayNormalChatGUI()
            return
        }
    }

    private fun displayNormalChatGUI() {
        GuiChat(inputField.text).apply {
            historyBuffer = this@KamiGuiChat.historyBuffer
            sentHistoryCursor = this@KamiGuiChat.sentHistoryCursor
        }.also {
            mc.displayGuiScreen(it)
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawScreen(mouseX, mouseY, partialTicks)

        // Draw predict string
        if (predictString.isNotBlank()) {
            val posX = fontRenderer.getStringWidth(inputField.text) + inputField.x
            val posY = inputField.y
            fontRenderer.drawStringWithShadow(predictString, posX.toFloat(), posY.toFloat(), 0x666666)
        }

        // Draw outline around input field
        val vertexHelper = VertexHelper(GlStateUtils.useVbo())
        val pos1 = Vec2d(inputField.x - 2.0, inputField.y - 2.0)
        val pos2 = pos1.add(inputField.width.toDouble(), inputField.height.toDouble())
        RenderUtils2D.drawRectOutline(vertexHelper, pos1, pos2, 1.5f, ColorHolder(KamiGuiColors.GuiC.windowOutline.color))
    }

}