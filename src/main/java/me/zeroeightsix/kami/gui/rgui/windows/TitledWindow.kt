package me.zeroeightsix.kami.gui.rgui.windows

import me.zeroeightsix.kami.module.modules.client.GuiColors
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.graphics.font.KamiFontRenderer
import me.zeroeightsix.kami.util.math.Vec2f

/**
 * Window with rectangle and title rendering
 */
open class TitledWindow(
        name: String,
        posX: Float,
        posY: Float,
        width: Float,
        height: Float,
        saveToConfig: Boolean
) : BasicWindow(name, posX, posY, width, height, saveToConfig) {
    override val draggableHeight: Float
        get() = KamiFontRenderer.getFontHeight() + 6.0f

    override fun onRender(vertexHelper: VertexHelper, absolutePos: Vec2f) {
        super.onRender(vertexHelper, absolutePos)
        KamiFontRenderer.drawString(name.value, 3.0f, 3.0f, colorIn = GuiColors.text)
    }
}