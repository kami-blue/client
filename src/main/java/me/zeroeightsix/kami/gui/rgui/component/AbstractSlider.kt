package me.zeroeightsix.kami.gui.rgui.component

import me.zeroeightsix.kami.gui.rgui.InteractiveComponent
import me.zeroeightsix.kami.module.modules.client.GuiColors
import me.zeroeightsix.kami.util.TimedFlag
import me.zeroeightsix.kami.util.graphics.AnimationUtils
import me.zeroeightsix.kami.util.graphics.GlStateUtils
import me.zeroeightsix.kami.util.graphics.RenderUtils2D
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.graphics.font.KamiFontRenderer
import me.zeroeightsix.kami.util.math.Vec2d
import me.zeroeightsix.kami.util.math.Vec2f

abstract class AbstractSlider(
        name: String,
        valueIn: Double
) : InteractiveComponent(name, 0.0f, 0.0f, 40.0f, 10.0f, false) {
    protected var value = valueIn
        set(value) {
            if (value != field) {
                prevValue.value = renderProgress
                field = value
            }
        }

    protected val prevValue = TimedFlag(value)
    protected open val renderProgress: Double
        get() = AnimationUtils.linear(AnimationUtils.toDeltaTimeDouble(prevValue.lastUpdateTime), 50.0, prevValue.value, value)

    override val maxHeight
        get() = KamiFontRenderer.getFontHeight() + 4.0f
    protected var protectedWidth = 0.0

    override fun onGuiInit() {
        super.onGuiInit()
        prevValue.value = 0.0
        value = 0.0
    }

    override fun onTick() {
        super.onTick()
        height.value = maxHeight
    }

    override fun onRender(vertexHelper: VertexHelper, absolutePos: Vec2f) {
        // Slider bar
        if (renderProgress > 0.0) RenderUtils2D.drawRectFilled(vertexHelper, Vec2d(0.0, 0.0), Vec2d(renderWidth * renderProgress, renderHeight.toDouble()), GuiColors.primary)

        // Slider hover overlay
        val overlayColor = getStateColor(mouseState).interpolate(getStateColor(prevState), AnimationUtils.toDeltaTimeDouble(lastStateUpdateTime), 200.0)
        RenderUtils2D.drawRectFilled(vertexHelper, Vec2d(0.0, 0.0), Vec2d(renderWidth, renderHeight), overlayColor)

        // Slider frame
        RenderUtils2D.drawRectOutline(vertexHelper, Vec2d(0.0, 0.0), Vec2d(renderWidth, renderHeight), 1.0f, GuiColors.outline)

        // Slider name
        GlStateUtils.pushScissor()
        /*if (protectedWidth > 0.0) {
            GlStateUtils.scissor(
                    ((absolutePos.x + renderWidth - protectedWidth) * ClickGUI.getScaleFactor()).roundToInt(),
                    (mc.displayHeight - (absolutePos.y + renderHeight) * ClickGUI.getScaleFactor()).roundToInt(),
                    (protectedWidth * ClickGUI.getScaleFactor()).roundToInt(),
                    (renderHeight * ClickGUI.getScaleFactor()).roundToInt()
            )
        }*/
        KamiFontRenderer.drawString(name.value, 2f, 1f, colorIn = GuiColors.text)
        GlStateUtils.popScissor()
    }

    protected fun getStateColor(state: MouseState) = when (state) {
        MouseState.NONE -> GuiColors.idle
        MouseState.HOVER -> GuiColors.hover
        else -> GuiColors.click
    }
}