package org.kamiblue.client.gui.rgui.component

import net.minecraft.util.text.TextFormatting
import org.kamiblue.client.gui.rgui.InteractiveComponent
import org.kamiblue.client.module.modules.client.ClickGUI
import org.kamiblue.client.module.modules.client.GuiColors
import org.kamiblue.client.module.modules.client.Tooltips
import org.kamiblue.client.util.TimedFlag
import org.kamiblue.client.util.graphics.AnimationUtils
import org.kamiblue.client.util.graphics.RenderUtils2D
import org.kamiblue.client.util.graphics.VertexHelper
import org.kamiblue.client.util.graphics.font.FontRenderAdapter
import org.kamiblue.client.util.graphics.font.TextComponent
import org.kamiblue.client.util.math.Vec2d
import org.kamiblue.client.util.math.Vec2f
import org.kamiblue.client.util.text.format
import org.lwjgl.opengl.GL11.*

open class Slider(
    name: String,
    valueIn: Double,
    private val description: String = "",
    private val visibility: (() -> Boolean)?
) : InteractiveComponent(name, 0.0f, 0.0f, 40.0f, 10.0f, SettingGroup.NONE) {
    protected var value = valueIn
        set(value) {
            if (value != field) {
                prevValue.value = renderProgress
                field = value.coerceIn(0.0, 1.0)
            }
        }

    protected val prevValue = TimedFlag(value)
    protected open val renderProgress: Double
        get() = AnimationUtils.linear(AnimationUtils.toDeltaTimeDouble(prevValue.lastUpdateTime), 50.0, prevValue.value, value)

    override val maxHeight
        get() = FontRenderAdapter.getFontHeight() + 3.0f
    protected var protectedWidth = 0.0

    private val displayDescription = TextComponent(" ")
    private var descriptionPosX = 0.0f
    private var shown = false

    var listening = false; protected set

    open val isBold
        get() = false

    override fun onClosed() {
        super.onClosed()
        onStopListening(false)
    }

    override fun onDisplayed() {
        super.onDisplayed()
        prevValue.value = 0.0
        value = 0.0
        setupDescription()
    }

    open fun onStopListening(success: Boolean) {
        listening = false
    }

    private fun setupDescription() {
        displayDescription.clear()
        if (description.isNotBlank()) {
            val stringBuilder = StringBuilder()
            val spaceWidth = FontRenderAdapter.getStringWidth(" ")
            var lineWidth = -spaceWidth

            for (string in description.split(' ')) {
                val wordWidth = FontRenderAdapter.getStringWidth(string) + spaceWidth
                val newWidth = lineWidth + wordWidth

                lineWidth = if (newWidth > 169.0f) {
                    displayDescription.addLine(stringBuilder.toString())
                    stringBuilder.clear()
                    -spaceWidth + wordWidth
                } else {
                    newWidth
                }

                stringBuilder.append(string)
                stringBuilder.append(' ')
            }

            if (stringBuilder.isNotEmpty()) displayDescription.addLine(stringBuilder.toString())
        }
    }

    override fun onTick() {
        super.onTick()
        height = maxHeight
        visibility?.let { visible = it.invoke() }
    }

    override fun onRender(vertexHelper: VertexHelper, absolutePos: Vec2f) {
        // Slider bar
        if (renderProgress > 0.0) RenderUtils2D.drawRectFilled(vertexHelper, Vec2d(0.0, 0.0), Vec2d(renderWidth * renderProgress, renderHeight.toDouble()), GuiColors.primary)

        // Slider hover overlay
        val overlayColor = getStateColor(mouseState).interpolate(getStateColor(prevState), AnimationUtils.toDeltaTimeDouble(lastStateUpdateTime), 200.0)
        RenderUtils2D.drawRectFilled(vertexHelper, Vec2d(0.0, 0.0), Vec2d(renderWidth.toDouble(), renderHeight.toDouble()), overlayColor)

        // Slider frame
        RenderUtils2D.drawRectOutline(vertexHelper, Vec2d(0.0, 0.0), Vec2f(renderWidth, renderHeight).toVec2d(), 1.5f, GuiColors.outline)

        // Slider name

        // TODO: do something with this https://discord.com/channels/573954110454366214/789630848194183209/795732239211429909
        //GlStateUtils.pushScissor()
        /*if (protectedWidth > 0.0) {
            GlStateUtils.scissor(
                    ((absolutePos.x + renderWidth - protectedWidth) * ClickGUI.getScaleFactor()).roundToInt(),
                    (mc.displayHeight - (absolutePos.y + renderHeight) * ClickGUI.getScaleFactor()).roundToInt(),
                    (protectedWidth * ClickGUI.getScaleFactor()).roundToInt(),
                    (renderHeight * ClickGUI.getScaleFactor()).roundToInt()
            )
        }*/
        val text = if (isBold) TextFormatting.BOLD format componentName else componentName

        FontRenderAdapter.drawString(text, 1.5f, 1.0f, color = GuiColors.text)
        //GlStateUtils.popScissor()
    }

    override fun onPostRender(vertexHelper: VertexHelper, absolutePos: Vec2f) {
        if (Tooltips.isDisabled || description.isBlank()) return

        var deltaTime = AnimationUtils.toDeltaTimeFloat(lastStateUpdateTime)

        if (mouseState == MouseState.HOVER && deltaTime > 500L || prevState == MouseState.HOVER && shown) {

            if (mouseState == MouseState.HOVER) {
                if (descriptionPosX == 0.0f) descriptionPosX = lastMousePos.x
                deltaTime -= 500L
                shown = true
            } else if (deltaTime > 250.0f) {
                descriptionPosX = 0.0f
                shown = false
                return
            }

            val alpha = (if (mouseState == MouseState.HOVER) AnimationUtils.exponentInc(deltaTime, 250.0f, 0.0f, 1.0f)
            else AnimationUtils.exponentDec(deltaTime, 250.0f, 0.0f, 1.0f))
            val textWidth = displayDescription.getWidth().toDouble()
            val textHeight = displayDescription.getHeight(2).toDouble()

            val relativeCorner = Vec2f(mc.displayWidth.toFloat(), mc.displayHeight.toFloat()).div(ClickGUI.getScaleFactorFloat()).minus(absolutePos)

            val posX = descriptionPosX.coerceIn(-absolutePos.x, (relativeCorner.x - textWidth - 10.0f).toFloat())
            val posY = (renderHeight + 4.0f).coerceIn(-absolutePos.y, (relativeCorner.y - textHeight - 10.0f).toFloat())

            glDisable(GL_SCISSOR_TEST)
            glPushMatrix()
            glTranslatef(posX, posY, 696.0f)

            RenderUtils2D.drawRectFilled(vertexHelper, posEnd = Vec2d(textWidth, textHeight).plus(4.0), color = GuiColors.backGround.apply { a = (a * alpha).toInt() })
            RenderUtils2D.drawRectOutline(vertexHelper, posEnd = Vec2d(textWidth, textHeight).plus(4.0), lineWidth = 2.0f, color = GuiColors.primary.apply { a = (a * alpha).toInt() })

            displayDescription.draw(Vec2d(2.0, 2.0), 2, alpha)

            glEnable(GL_SCISSOR_TEST)
            glPopMatrix()
        }
    }

    private fun getStateColor(state: MouseState) = when (state) {
        MouseState.NONE -> GuiColors.idle
        MouseState.HOVER -> GuiColors.hover
        else -> GuiColors.click
    }
}