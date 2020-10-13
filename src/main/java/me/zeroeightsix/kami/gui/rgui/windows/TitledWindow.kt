package me.zeroeightsix.kami.gui.rgui.windows

import me.zeroeightsix.kami.module.modules.client.GuiColors
import me.zeroeightsix.kami.util.graphics.RenderUtils2D
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.graphics.font.KamiFontRenderer
import me.zeroeightsix.kami.util.math.Vec2d

/**
 * Window with rectangle and title rendering
 */
open class TitledWindow(
        name: String,
        posX: Float,
        posY: Float,
        width: Float,
        height: Float
) : BasicWindow(name, posX, posY, width, height) {
    override val draggableHeight: Float
        get() = KamiFontRenderer.getFontHeight() + 4.0f

    override fun onRender(vertexHelper: VertexHelper) {
        super.onRender(vertexHelper)
        RenderUtils2D.drawRectFilled(vertexHelper, Vec2d(0.0, 0.0), Vec2d(renderWidth, draggableHeight), GuiColors.primary)
        KamiFontRenderer.drawString(name, 2f, 2f, color = GuiColors.text)
    }
}