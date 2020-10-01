package me.zeroeightsix.kami.gui.rgui.windows

import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.RenderUtils2D
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.graphics.font.KamiFontRenderer
import me.zeroeightsix.kami.util.math.Vec2d

/**
 * Window with rectangle and title rendering
 */
open class TitledWindow(
        name: String,
        posX: Double,
        posY: Double,
        width: Double,
        height: Double
) : BasicWindow(name, posX, posY, width, height) {
    override val draggableHeight: Double
        get() = KamiFontRenderer.getFontHeight() + 2.0

    override fun onRender(vertexHelper: VertexHelper) {
        super.onRender(vertexHelper)
        RenderUtils2D.drawRectFilled(vertexHelper, Vec2d(0.0, 0.0), Vec2d(prevWidth, draggableHeight), ColorHolder(123, 114, 204))
        KamiFontRenderer.drawString(name, 4f, 0f, color = ColorHolder(255, 255, 255))
    }
}