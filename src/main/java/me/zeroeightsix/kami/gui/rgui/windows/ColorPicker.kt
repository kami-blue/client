package me.zeroeightsix.kami.gui.rgui.windows

import me.zeroeightsix.kami.gui.AbstractKamiGui
import me.zeroeightsix.kami.gui.rgui.component.Button
import me.zeroeightsix.kami.gui.rgui.component.SettingSlider
import me.zeroeightsix.kami.gui.rgui.component.Slider
import me.zeroeightsix.kami.module.modules.client.GuiColors
import me.zeroeightsix.kami.setting.GuiConfig.setting
import me.zeroeightsix.kami.setting.impl.other.ColorSetting
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.Alignment
import me.zeroeightsix.kami.util.graphics.RenderUtils2D
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.math.Vec2d
import me.zeroeightsix.kami.util.math.Vec2f
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11.*
import java.awt.Color

object ColorPicker : TitledWindow("Color Picker", 0.0f, 0.0f, 200.0f, 200.0f, SettingGroup.NONE) {

    override val resizable: Boolean get() = false
    override val minimizable: Boolean get() = false

    var setting: ColorSetting? = null
    private var hoveredComponent: Slider? = null
        set(value) {
            if (value == field) return
            field?.onLeave(AbstractKamiGui.getRealMousePos())
            value?.onHover(AbstractKamiGui.getRealMousePos())
            field = value
        }

    // Positions
    private var fieldHeight = 0.0f
    private var fieldPos = Pair(Vec2f.ZERO, Vec2f.ZERO)
    private var huePos = Pair(Vec2f.ZERO, Vec2f.ZERO)
    private var hueLinePos = Pair(Vec2f.ZERO, Vec2f.ZERO)
    private var prevColorPos = Pair(Vec2f.ZERO, Vec2f.ZERO)
    private var currentColorPos = Pair(Vec2f.ZERO, Vec2f.ZERO)

    // Main values
    private var hue = 0.0f
    private var saturation = 1.0f
    private var brightness = 1.0f
    private var prevHue = 0.0f
    private var prevSaturation = 1.0f
    private var prevBrightness = 1.0f

    // Sliders
    private val r = setting("Red", 255, 0..255, 1, description = "")
    private val g = setting("Green", 255, 0..255, 1, description = "")
    private val b = setting("Blue", 255, 0..255, 1, description = "")
    private val a = setting("Alpha", 255, 0..255, 1, { setting?.hasAlpha ?: true }, description = "")
    private val sliderR = SettingSlider(r)
    private val sliderG = SettingSlider(g)
    private val sliderB = SettingSlider(b)
    private val sliderA = SettingSlider(a)

    // Buttons
    private val buttonOkay = Button("Okay", { actionOk() }, "")
    private val buttonCancel = Button("Cancel", { actionCancel() }, "")

    private val components = arrayOf(sliderR, sliderG, sliderB, sliderA, buttonOkay, buttonCancel)

    override fun onDisplayed() {
        super.onDisplayed()
        updatePos()
        setting?.value?.clone()?.let { updateHSBFromRGB() }
        lastActiveTime = System.currentTimeMillis() + 1000L
        for (component in components) component.onDisplayed()
    }

    override fun onTick() {
        super.onTick()
        prevHue = hue
        prevSaturation = saturation
        prevBrightness = brightness
        for (component in components) component.onTick()
        if (hoveredComponent != null) updateHSBFromRGB()
    }

    override fun onMouseInput(mousePos: Vec2f) {
        super.onMouseInput(mousePos)

        hoveredComponent = components.firstOrNull {
            it.visible.value
                    && preDragMousePos.x in it.posX..it.posX + it.width.value
                    && preDragMousePos.y in it.posY..it.posY + it.height.value
        }?.also {
            it.onMouseInput(mousePos)
        }
    }

    override fun onClick(mousePos: Vec2f, buttonId: Int) {
        super.onClick(mousePos, buttonId)
        val relativeMousePos = mousePos.subtract(posX, posY)

        hoveredComponent?.let {
            it.onClick(relativeMousePos.subtract(it.posX, it.posY), buttonId)
        } ?: run {
            updateValues(relativeMousePos, relativeMousePos)
        }
    }

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        val relativeMousePos = mousePos.subtract(posX, posY)

        hoveredComponent?.let {
            it.onRelease(relativeMousePos.subtract(it.posX, it.posY), buttonId)
        } ?: run {
            updateValues(relativeMousePos, relativeMousePos)
        }
    }

    override fun onDrag(mousePos: Vec2f, clickPos: Vec2f, buttonId: Int) {
        super.onDrag(mousePos, clickPos, buttonId)
        val relativeMousePos = mousePos.subtract(posX, posY)
        val relativeClickPos = clickPos.subtract(posX, posY)

        hoveredComponent?.let {
            it.onDrag(relativeMousePos.subtract(it.posX, it.posY), clickPos, buttonId)
        } ?: run {
            updateValues(relativeMousePos, relativeClickPos)
        }
    }

    private fun updateValues(mousePos: Vec2f, clickPos: Vec2f) {
        val relativeX = mousePos.x - 4.0f
        val relativeY = mousePos.y - draggableHeight - 4.0f
        val fieldHeight = fieldHeight

        if (isInPair(clickPos, fieldPos)) {
            saturation = (relativeX / fieldHeight).coerceIn(0.0f, 1.0f)
            brightness = (1.0f - relativeY / fieldHeight).coerceIn(0.0f, 1.0f)
            updateRGBFromHSB()
        } else if (isInPair(clickPos, huePos)) {
            hue = (relativeY / fieldHeight).coerceIn(0.0f, 1.0f)
            updateRGBFromHSB()
        }
    }

    private fun isInPair(mousePos: Vec2f, pair: Pair<Vec2f, Vec2f>) =
            mousePos.x in pair.first.x..pair.second.x && mousePos.y in pair.first.y..pair.second.y

    override fun onRender(vertexHelper: VertexHelper, absolutePos: Vec2f) {
        super.onRender(vertexHelper, absolutePos)

        drawColorField(vertexHelper)
        drawHueSlider(vertexHelper)
        drawColorPreview(vertexHelper)

        for (component in components) {
            if (!component.visible.value) continue
            glPushMatrix()
            glTranslatef(component.renderPosX, component.renderPosY, 0.0f)
            component.onRender(vertexHelper, absolutePos.add(component.renderPosX, component.renderPosY))
            glPopMatrix()
        }
    }

    private fun drawColorField(vertexHelper: VertexHelper) {
        RenderUtils2D.prepareGl()
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE)

        // Saturation
        val interpolatedHue = prevHue + (hue - prevHue) * mc.renderPartialTicks
        val rightColor = ColorHolder(Color.getHSBColor(interpolatedHue, 1.0f, 1.0f))
        val leftColor = ColorHolder(255, 255, 255)
        vertexHelper.begin(GL_TRIANGLE_STRIP)
        vertexHelper.put(Vec2d(fieldPos.first), leftColor) // Top left
        vertexHelper.put(Vec2d(fieldPos.first.x, fieldPos.second.y), leftColor) // Bottom left
        vertexHelper.put(Vec2d(fieldPos.second.x, fieldPos.first.y), rightColor) // Top right
        vertexHelper.put(Vec2d(fieldPos.second), rightColor) // Bottom right
        vertexHelper.end()

        // Brightness
        val topColor = ColorHolder(0, 0, 0, 0)
        val bottomColor = ColorHolder(0, 0, 0, 255)
        vertexHelper.begin(GL_TRIANGLE_STRIP)
        vertexHelper.put(Vec2d(fieldPos.first), topColor) // Top left
        vertexHelper.put(Vec2d(fieldPos.first.x, fieldPos.second.y), bottomColor) // Bottom left
        vertexHelper.put(Vec2d(fieldPos.second.x, fieldPos.first.y), topColor) // Top right
        vertexHelper.put(Vec2d(fieldPos.second), bottomColor) // Bottom right
        vertexHelper.end()

        RenderUtils2D.releaseGl()
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        // Outline
        RenderUtils2D.drawRectOutline(vertexHelper, Vec2d(fieldPos.first), Vec2d(fieldPos.second), 1.5f, GuiColors.outline)

        // Circle pointer
        val interpolatedSaturation = prevSaturation + (saturation - prevSaturation) * mc.renderPartialTicks
        val interpolatedBrightness = prevBrightness + (brightness - prevBrightness) * mc.renderPartialTicks
        val relativeBrightness = ((1.0f - (1.0f - interpolatedSaturation) * interpolatedBrightness) * 255.0f).toInt()
        val circleColor = ColorHolder(relativeBrightness, relativeBrightness, relativeBrightness)
        val circlePos = Vec2d((fieldPos.first.x + fieldHeight * interpolatedSaturation).toDouble(), fieldPos.first.y + fieldHeight * (1.0 - interpolatedBrightness))
        RenderUtils2D.drawCircleOutline(vertexHelper, circlePos, 4.0, 32, 1.5f, circleColor)
    }

    private fun drawHueSlider(vertexHelper: VertexHelper) {
        val color1 = ColorHolder(255, 0, 0) // 0.0
        val color2 = ColorHolder(255, 255, 0) // 0.1666
        val color3 = ColorHolder(0, 255, 0) // 0.3333
        val color4 = ColorHolder(0, 255, 255) // 0.5
        val color5 = ColorHolder(0, 0, 255) // 0.6666
        val color6 = ColorHolder(255, 0, 255) // 0.8333
        val height = (hueLinePos.second.y - hueLinePos.first.y) / 6.0

        // Hue slider
        RenderUtils2D.prepareGl()
        glLineWidth(16.0f)
        vertexHelper.begin(GL_LINE_STRIP)
        vertexHelper.put(Vec2d(hueLinePos.first), color1)
        vertexHelper.put(Vec2d(hueLinePos.first).add(0.0, height), color2)
        vertexHelper.put(Vec2d(hueLinePos.first).add(0.0, height * 2.0), color3)
        vertexHelper.put(Vec2d(hueLinePos.first).add(0.0, height * 3.0), color4)
        vertexHelper.put(Vec2d(hueLinePos.first).add(0.0, height * 4.0), color5)
        vertexHelper.put(Vec2d(hueLinePos.first).add(0.0, height * 5.0), color6)
        vertexHelper.put(Vec2d(hueLinePos.second), color1)
        vertexHelper.end()
        glLineWidth(1.0f)
        RenderUtils2D.releaseGl()

        // Outline
        RenderUtils2D.drawRectOutline(vertexHelper, Vec2d(huePos.first), Vec2d(huePos.second), 1.5f, GuiColors.outline)

        // Arrow pointer
        val interpolatedHue = prevHue + (hue - prevHue) * mc.renderPartialTicks
        val pointerPosY = (huePos.first.y + fieldHeight * interpolatedHue).toDouble()
        RenderUtils2D.drawTriangleFilled(vertexHelper, Vec2d(huePos.first.x - 6.0, pointerPosY - 2.0), Vec2d(huePos.first.x - 6.0, pointerPosY + 2.0), Vec2d(huePos.first.x - 2.0, pointerPosY), GuiColors.primary)
        RenderUtils2D.drawTriangleFilled(vertexHelper, Vec2d(huePos.second.x + 2.0, pointerPosY), Vec2d(huePos.second.x + 6.0, pointerPosY + 2.0), Vec2d(huePos.second.x + 6.0, pointerPosY - 2.0), GuiColors.primary)
    }

    private fun drawColorPreview(vertexHelper: VertexHelper) {
        RenderUtils2D.prepareGl()

        // Previous color
        val prevColor = setting?.value?.clone()?.apply { a = 255 }
        RenderUtils2D.drawRectFilled(vertexHelper, Vec2d(prevColorPos.first), Vec2d(prevColorPos.second), prevColor?: ColorHolder())
        RenderUtils2D.drawRectOutline(vertexHelper, Vec2d(prevColorPos.first), Vec2d(prevColorPos.second), 1.5f, GuiColors.outline)

        // Current color
        val currentColor = ColorHolder(r.value, g.value, b.value)
        RenderUtils2D.drawRectFilled(vertexHelper, Vec2d(currentColorPos.first), Vec2d(currentColorPos.second), currentColor)
        RenderUtils2D.drawRectOutline(vertexHelper, Vec2d(currentColorPos.first), Vec2d(currentColorPos.second), 1.5f, GuiColors.outline)

        // Previous hex

        RenderUtils2D.releaseGl()
    }

    private fun actionOk() {
        setting?.value = ColorHolder(r.value, g.value, b.value, a.value)
        actionCancel()
    }

    private fun actionCancel() {
        setting = null
        visible.value = false
    }

    private fun updateRGBFromHSB() {
        val color = Color.getHSBColor(hue, saturation, brightness)
        r.value = color.red
        g.value = color.green
        b.value = color.blue
    }

    private fun updateHSBFromRGB() {
        val floatArray = Color.RGBtoHSB(r.value, g.value, b.value, null)
        hue = floatArray[0]
        saturation = floatArray[1]
        brightness = floatArray[2]
    }

    private fun updatePos() {
        // Red slider
        sliderR.posY = 4.0f + draggableHeight
        sliderR.width.value = 128.0f

        // Green slider
        sliderG.posY = sliderR.posY + sliderR.height.value + 4.0f
        sliderG.width.value = 128.0f

        // Blue slider
        sliderB.posY = sliderG.posY + sliderG.height.value + 4.0f
        sliderB.width.value = 128.0f

        // Alpha slider
        sliderA.posY = sliderB.posY + sliderB.height.value + 4.0f
        sliderA.width.value = 128.0f

        // Okay button
        buttonOkay.posY = sliderA.posY + sliderA.height.value + 4.0f
        buttonOkay.width.value = 50.0f

        // Cancel button
        buttonCancel.posY = buttonOkay.posY + (buttonOkay.height.value + 4.0f) * 2.0f
        buttonCancel.width.value = 50.0f

        // Main window
        dockingH.value = Alignment.HAlign.CENTER
        dockingV.value = Alignment.VAlign.CENTER
        relativePosX.value = 0.0f
        relativePosY.value = 0.0f
        height.value = buttonCancel.posY + buttonCancel.height.value + 4.0f
        width.value = height.value - draggableHeight + 4.0f + 8.0f + 4.0f + 128.0f + 8.0f

        // PositionX of components
        sliderR.posX = width.value - 4.0f - 128.0f
        sliderG.posX = width.value - 4.0f - 128.0f
        sliderB.posX = width.value - 4.0f - 128.0f
        sliderA.posX = width.value - 4.0f - 128.0f
        buttonOkay.posX = width.value - 4.0f - 50.0f
        buttonCancel.posX = width.value - 4.0f - 50.0f

        // Variables
        fieldHeight = height.value - 8.0f - draggableHeight
        fieldPos = Pair(
                Vec2f(4.0f, 4.0f + draggableHeight),
                Vec2f(4.0f + fieldHeight, 4.0f + fieldHeight + draggableHeight)
        )
        huePos = Pair(
                Vec2f(4.0f + fieldHeight + 8.0f, 4.0f + draggableHeight),
                Vec2f(4.0f + fieldHeight + 8.0f + 8.0f, 4.0f + fieldHeight + draggableHeight)
        )
        hueLinePos = Pair(
                Vec2f(4.0f + fieldHeight + 8.0f + 4.0f, 4.0f + draggableHeight),
                Vec2f(4.0f + fieldHeight + 8.0f + 4.0f, 4.0f + fieldHeight + draggableHeight)
        )
        prevColorPos = Pair(
                Vec2f(sliderR.posX, buttonOkay.posY),
                Vec2f(sliderR.posX + 35.0f, buttonCancel.posY - 4.0f)
        )
        currentColorPos = Pair(
                Vec2f(sliderR.posX + 35.0f + 4.0f, buttonOkay.posY),
                Vec2f(sliderR.posX + 35.0f + 4.0f + 35.0f, buttonCancel.posY - 4.0f)
        )
    }

    init {
        visible.value = false
        updatePos()
    }
}