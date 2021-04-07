package org.kamiblue.client.gui.rgui.windows

import net.minecraft.client.renderer.GlStateManager
import org.kamiblue.client.gui.AbstractKamiGui
import org.kamiblue.client.gui.rgui.component.Button
import org.kamiblue.client.gui.rgui.component.SettingSlider
import org.kamiblue.client.gui.rgui.component.Slider
import org.kamiblue.client.module.modules.client.GuiColors
import org.kamiblue.client.setting.GuiConfig.setting
import org.kamiblue.client.setting.settings.impl.other.ColorSetting
import org.kamiblue.client.util.color.ColorHolder
import org.kamiblue.client.util.graphics.RenderUtils2D
import org.kamiblue.client.util.graphics.VertexHelper
import org.kamiblue.client.util.graphics.font.HAlign
import org.kamiblue.client.util.graphics.font.VAlign
import org.kamiblue.client.util.math.Vec2d
import org.kamiblue.client.util.math.Vec2f
import org.lwjgl.opengl.GL11.*
import java.awt.Color

object ColorPicker : TitledWindow("Color Picker", 0.0f, 0.0f, 200.0f, 200.0f, SettingGroup.NONE) {

    override val resizable: Boolean get() = false
    override val minimizable: Boolean get() = false

    var setting: ColorSetting? = null
    var originalColor: ColorHolder? = null
    private var hoveredChild: Slider? = null
        set(value) {
            if (value == field) return
            field?.onLeave(AbstractKamiGui.getRealMousePos())
            value?.onHover(AbstractKamiGui.getRealMousePos())
            field = value
        }
    private var listeningChild: Slider? = null

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
    private val buttonOkay = Button("Okay", { actionOk() })
    private val buttonCancel = Button("Cancel", { actionCancel() })

    private val components = arrayOf(sliderR, sliderG, sliderB, sliderA, buttonOkay, buttonCancel)

    override fun onDisplayed() {
        super.onDisplayed()
        updatePos()
        setting?.let {
            originalColor = it.value
            r.value = it.value.r
            g.value = it.value.g
            b.value = it.value.b
            if (it.hasAlpha) a.value = it.value.a
            sliderA.visible = it.hasAlpha
            updateHSBFromRGB()
        }
        lastActiveTime = System.currentTimeMillis() + 1000L
        for (component in components) component.onDisplayed()
    }

    override fun onTick() {
        super.onTick()
        if (visible) {
            prevHue = hue
            prevSaturation = saturation
            prevBrightness = brightness
            for (component in components) component.onTick()
            if (hoveredChild != null) updateHSBFromRGB()
            if (listeningChild?.listening == false) listeningChild = null
        }
    }

    override fun onMouseInput(mousePos: Vec2f) {
        super.onMouseInput(mousePos)

        hoveredChild = components.firstOrNull {
            it.visible
                && preDragMousePos.x in it.posX..it.posX + it.width
                && preDragMousePos.y in it.posY..it.posY + it.height
        }?.also {
            it.onMouseInput(mousePos)
        }
    }

    override fun onClick(mousePos: Vec2f, buttonId: Int) {
        super.onClick(mousePos, buttonId)
        val relativeMousePos = mousePos.minus(posX, posY)

        hoveredChild?.let {
            it.onClick(relativeMousePos.minus(it.posX, it.posY), buttonId)
        } ?: run {
            updateValues(relativeMousePos, relativeMousePos)
        }
    }

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        val relativeMousePos = mousePos.minus(posX, posY)

        hoveredChild?.let {
            it.onRelease(relativeMousePos.minus(it.posX, it.posY), buttonId)
            if (it.listening) listeningChild = it
        } ?: run {
            updateValues(relativeMousePos, relativeMousePos)
        }
    }

    override fun onDrag(mousePos: Vec2f, clickPos: Vec2f, buttonId: Int) {
        super.onDrag(mousePos, clickPos, buttonId)
        val relativeMousePos = mousePos.minus(posX, posY)
        val relativeClickPos = clickPos.minus(posX, posY)

        hoveredChild?.let {
            it.onDrag(relativeMousePos.minus(it.posX, it.posY), clickPos, buttonId)
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

        setting?.value = ColorHolder(r.value, g.value, b.value, a.value)
    }

    private fun isInPair(mousePos: Vec2f, pair: Pair<Vec2f, Vec2f>) =
        mousePos.x in pair.first.x..pair.second.x && mousePos.y in pair.first.y..pair.second.y

    override fun onKeyInput(keyCode: Int, keyState: Boolean) {
        super.onKeyInput(keyCode, keyState)
        listeningChild?.onKeyInput(keyCode, keyState)
    }

    override fun onRender(vertexHelper: VertexHelper, absolutePos: Vec2f) {
        super.onRender(vertexHelper, absolutePos)

        drawColorField(vertexHelper)
        drawHueSlider(vertexHelper)
        drawColorPreview(vertexHelper)

        for (component in components) {
            if (!component.visible) continue
            glPushMatrix()
            glTranslatef(component.renderPosX, component.renderPosY, 0.0f)
            component.onRender(vertexHelper, absolutePos.plus(component.renderPosX, component.renderPosY))
            glPopMatrix()
        }
    }

    override fun onPostRender(vertexHelper: VertexHelper, absolutePos: Vec2f) {
        super.onPostRender(vertexHelper, absolutePos)

        for (component in components) {
            if (!component.visible) continue
            glPushMatrix()
            glTranslatef(component.renderPosX, component.renderPosY, 0.0f)
            component.onPostRender(vertexHelper, absolutePos.plus(component.renderPosX, component.renderPosY))
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
        vertexHelper.put(fieldPos.first.toVec2d(), leftColor) // Top left
        vertexHelper.put(Vec2f(fieldPos.first.x, fieldPos.second.y).toVec2d(), leftColor) // Bottom left
        vertexHelper.put(Vec2f(fieldPos.second.x, fieldPos.first.y).toVec2d(), rightColor) // Top right
        vertexHelper.put(fieldPos.second.toVec2d(), rightColor) // Bottom right
        vertexHelper.end()

        // Brightness
        val topColor = ColorHolder(0, 0, 0, 0)
        val bottomColor = ColorHolder(0, 0, 0, 255)
        vertexHelper.begin(GL_TRIANGLE_STRIP)
        vertexHelper.put(fieldPos.first.toVec2d(), topColor) // Top left
        vertexHelper.put(Vec2f(fieldPos.first.x, fieldPos.second.y).toVec2d(), bottomColor) // Bottom left
        vertexHelper.put(Vec2f(fieldPos.second.x, fieldPos.first.y).toVec2d(), topColor) // Top right
        vertexHelper.put(fieldPos.second.toVec2d(), bottomColor) // Bottom right
        vertexHelper.end()

        RenderUtils2D.releaseGl()
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        // Outline
        RenderUtils2D.drawRectOutline(vertexHelper, fieldPos.first.toVec2d(), fieldPos.second.toVec2d(), 1.5f, GuiColors.outline)

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
        vertexHelper.put(hueLinePos.first.toVec2d(), color1)
        vertexHelper.put(hueLinePos.first.toVec2d().plus(0.0, height), color2)
        vertexHelper.put(hueLinePos.first.toVec2d().plus(0.0, height * 2.0), color3)
        vertexHelper.put(hueLinePos.first.toVec2d().plus(0.0, height * 3.0), color4)
        vertexHelper.put(hueLinePos.first.toVec2d().plus(0.0, height * 4.0), color5)
        vertexHelper.put(hueLinePos.first.toVec2d().plus(0.0, height * 5.0), color6)
        vertexHelper.put(hueLinePos.second.toVec2d(), color1)
        vertexHelper.end()
        glLineWidth(1.0f)
        RenderUtils2D.releaseGl()

        // Outline
        RenderUtils2D.drawRectOutline(vertexHelper, huePos.first.toVec2d(), huePos.second.toVec2d(), 1.5f, GuiColors.outline)

        // Arrow pointer
        val interpolatedHue = prevHue + (hue - prevHue) * mc.renderPartialTicks
        val pointerPosY = (huePos.first.y + fieldHeight * interpolatedHue).toDouble()
        RenderUtils2D.drawTriangleFilled(vertexHelper, Vec2d(huePos.first.x - 6.0, pointerPosY - 2.0), Vec2d(huePos.first.x - 6.0, pointerPosY + 2.0), Vec2d(huePos.first.x - 2.0, pointerPosY), GuiColors.primary)
        RenderUtils2D.drawTriangleFilled(vertexHelper, Vec2d(huePos.second.x + 2.0, pointerPosY), Vec2d(huePos.second.x + 6.0, pointerPosY + 2.0), Vec2d(huePos.second.x + 6.0, pointerPosY - 2.0), GuiColors.primary)
    }

    private fun drawColorPreview(vertexHelper: VertexHelper) {
        RenderUtils2D.prepareGl()

        RenderUtils2D.drawRectFilled(vertexHelper, prevColorPos.first.toVec2d(), prevColorPos.second.toVec2d(), originalColor
            ?: ColorHolder())
        RenderUtils2D.drawRectOutline(vertexHelper, prevColorPos.first.toVec2d(), prevColorPos.second.toVec2d(), 1.5f, GuiColors.outline)

        // Current color
        val currentColor = setting?.value ?: ColorHolder()
        RenderUtils2D.drawRectFilled(vertexHelper, currentColorPos.first.toVec2d(), currentColorPos.second.toVec2d(), currentColor)
        RenderUtils2D.drawRectOutline(vertexHelper, currentColorPos.first.toVec2d(), currentColorPos.second.toVec2d(), 1.5f, GuiColors.outline)

        // Previous hex

        RenderUtils2D.releaseGl()
    }

    private fun actionOk() {
        setting?.value = ColorHolder(r.value, g.value, b.value, a.value) // Make sure that everything is in sync.
        closeScreen()
    }

    private fun actionCancel() {
        setting?.value = originalColor!!
        closeScreen()
    }

    private fun closeScreen() {
        setting = null
        visible = false
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
        sliderR.width = 128.0f

        // Green slider
        sliderG.posY = sliderR.posY + sliderR.height + 4.0f
        sliderG.width = 128.0f

        // Blue slider
        sliderB.posY = sliderG.posY + sliderG.height + 4.0f
        sliderB.width = 128.0f

        // Alpha slider
        sliderA.posY = sliderB.posY + sliderB.height + 4.0f
        sliderA.width = 128.0f

        // Okay button
        buttonOkay.posY = sliderA.posY + sliderA.height + 4.0f
        buttonOkay.width = 50.0f

        // Cancel button
        buttonCancel.posY = buttonOkay.posY + (buttonOkay.height + 4.0f) * 2.0f
        buttonCancel.width = 50.0f

        // Main window
        dockingH = HAlign.CENTER
        dockingV = VAlign.CENTER
        relativePosX = 0.0f
        relativePosY = 0.0f
        height = buttonCancel.posY + buttonCancel.height + 4.0f
        width = height - draggableHeight + 4.0f + 8.0f + 4.0f + 128.0f + 8.0f

        // PositionX of components
        sliderR.posX = width - 4.0f - 128.0f
        sliderG.posX = width - 4.0f - 128.0f
        sliderB.posX = width - 4.0f - 128.0f
        sliderA.posX = width - 4.0f - 128.0f
        buttonOkay.posX = width - 4.0f - 50.0f
        buttonCancel.posX = width - 4.0f - 50.0f

        // Variables
        fieldHeight = height - 8.0f - draggableHeight
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
        visible = false
        updatePos()
    }
}