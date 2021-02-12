package org.kamiblue.client.gui.rgui.windows

import org.kamiblue.client.module.modules.client.GuiColors
import org.kamiblue.client.util.graphics.VertexHelper
import org.kamiblue.client.util.graphics.font.FontRenderAdapter
import org.kamiblue.client.util.math.Vec2f

/**
 * Window with rectangle and title rendering
 */
open class TitledWindow(
    name: String,
    posX: Float,
    posY: Float,
    width: Float,
    height: Float,
    settingGroup: SettingGroup
) : BasicWindow(name, posX, posY, width, height, settingGroup) {
    override val draggableHeight: Float get() = FontRenderAdapter.getFontHeight() + 5.0f

    override val minimizable get() = true

    override fun onRender(vertexHelper: VertexHelper, absolutePos: Vec2f) {
        super.onRender(vertexHelper, absolutePos)
        FontRenderAdapter.drawString(componentName, 3.0f, 3.0f, color = GuiColors.text)
    }
}