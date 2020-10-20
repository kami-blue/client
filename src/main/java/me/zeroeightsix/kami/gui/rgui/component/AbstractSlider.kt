package me.zeroeightsix.kami.gui.rgui.component

import me.zeroeightsix.kami.gui.rgui.InteractiveComponent
import me.zeroeightsix.kami.module.modules.client.GuiColors
import me.zeroeightsix.kami.util.TimedFlag
import me.zeroeightsix.kami.util.graphics.AnimationUtils
import me.zeroeightsix.kami.util.graphics.RenderUtils2D
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.graphics.font.KamiFontRenderer
import me.zeroeightsix.kami.util.math.Vec2d

abstract class AbstractSlider(override var name: String, valueIn: Double) : InteractiveComponent() {
    protected var value = valueIn
        set(value) {
            if (value != field) {
                prevValue.value = renderProgress
                field = value
            }
        }
    override val maxHeight
        get() = KamiFontRenderer.getFontHeight() + 4.0f

    private val prevValue = TimedFlag(value)
    protected val renderProgress: Double
        get() = AnimationUtils.exponent(AnimationUtils.toDeltaTimeDouble(prevValue.lastUpdateTime), 200.0, prevValue.value, value)

    override fun onTick() {
        super.onTick()
        height = maxHeight
    }

    override fun onRender(vertexHelper: VertexHelper) {
        super.onRender(vertexHelper)

        // Slider bar
        if (renderProgress > 0.0) RenderUtils2D.drawRectFilled(vertexHelper, Vec2d(0.0, 0.0), Vec2d(renderWidth * renderProgress, renderHeight.toDouble()), GuiColors.primary)

        // Slider hover overlay
        val overlayColor = getStateColor(mouseState).interpolate(getStateColor(prevState), AnimationUtils.toDeltaTimeDouble(lastStateUpdateTime), 200.0)
        RenderUtils2D.drawRectFilled(vertexHelper, Vec2d(1.0, 1.0), Vec2d(renderWidth - 1.0, renderHeight - 1.0), overlayColor)

        // Slider frame
        RenderUtils2D.drawRectOutline(vertexHelper, Vec2d(0.0, 0.0), Vec2d(renderWidth, renderHeight), 1.0f, GuiColors.outline)

        // Slider name
        KamiFontRenderer.drawString(name, 2f, 1f, colorIn = GuiColors.text)
    }

    private fun getStateColor(state: MouseState) = when (state) {
        MouseState.NONE -> GuiColors.idle
        MouseState.HOVER -> GuiColors.hover
        else -> GuiColors.click
    }
}