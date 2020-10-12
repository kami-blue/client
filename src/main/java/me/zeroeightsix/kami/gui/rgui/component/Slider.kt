package me.zeroeightsix.kami.gui.rgui.component

import me.zeroeightsix.kami.gui.rgui.InteractiveComponent
import me.zeroeightsix.kami.module.modules.client.GuiColors
import me.zeroeightsix.kami.util.TimedFlag
import me.zeroeightsix.kami.util.graphics.AnimationUtils
import me.zeroeightsix.kami.util.graphics.RenderUtils2D
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.graphics.font.KamiFontRenderer
import me.zeroeightsix.kami.util.math.Vec2d

open class Slider(override var name: String, valueIn: Double) : InteractiveComponent() {
    protected var value = valueIn
        set(value) {
            if (value != field) {
                prevValue.value = renderProgress
                field = value
            }
        }
    override val maxHeight
        get() = KamiFontRenderer.getFontHeight() + 4.0

    private val prevValue = TimedFlag(value)
    protected val renderProgress: Double
        get() = AnimationUtils.exponent(AnimationUtils.toDeltaTime(prevValue.lastUpdateTime), 50.0, prevValue.value, value)

    override fun onTick() {
        super.onTick()
        height = maxHeight
    }

    override fun onRender(vertexHelper: VertexHelper) {
        val color = getStateColor(mouseState).interpolate(getStateColor(prevState), AnimationUtils.toDeltaTime(lastStateUpdateTime), 100.0)
        if (renderProgress > 0.0) RenderUtils2D.drawRectFilled(vertexHelper, Vec2d(0.5, 0.5), Vec2d((renderWidth - 0.5) * renderProgress, renderHeight - 0.5), color)
        RenderUtils2D.drawRectOutline(vertexHelper, Vec2d(0.0, 0.0), Vec2d(renderWidth, renderHeight), 1f, GuiColors.outline)
        KamiFontRenderer.drawString(name, 1f, 1f, color = GuiColors.text)
    }

    private fun getStateColor(state: MouseState) = when (state) {
        MouseState.NONE -> GuiColors.primary
        MouseState.HOVER -> GuiColors.hover
        else -> GuiColors.click
    }
}