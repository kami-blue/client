package me.zeroeightsix.kami.gui.rgui

import me.zeroeightsix.kami.setting.GuiConfig.setting
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.math.Vec2f

open class Component(
        name: String,
        posX: Float,
        posY: Float,
        width: Float,
        height: Float,
        val settingGroup: SettingGroup
) {

    // Basic info
    val originalName = name
    val name = setting("Name", name)
    val posX = setting("PosX", posX, 0.0f..69420.911f, 0.1f, consumer = { _, it -> it.coerceIn(0.0f, mc.displayWidth - 40.0f)})
    val posY = setting("PosY", posY, 0.0f..69420.911f, 0.1f, consumer = { _, it -> it.coerceIn(0.0f, mc.displayHeight - 40.0f)})
    val width = setting("Width", width, 0.0f..69420.911f, 0.1f, consumer = { _, it -> it.coerceIn(0.0f, mc.displayWidth.toFloat()) })
    val height = setting("Height", height, 0.0f..69420.911f, 0.1f, consumer = { _, it -> it.coerceIn(0.0f, mc.displayHeight.toFloat()) })
    val visible = setting("Visible", true, consumer = { _, it -> it || !closeable })

    // Extra info
    protected val mc = Wrapper.minecraft
    open val minWidth = 16.0f
    open val minHeight = 16.0f
    open val maxWidth = -1.0f
    open val maxHeight = -1.0f
    val closeable: Boolean get() = true

    // Rendering info
    var prevPosX = 0.0f; protected set
    var prevPosY = 0.0f; protected set
    val renderPosX get() = prevPosX + (posX.value - prevPosX) * mc.renderPartialTicks
    val renderPosY get() = prevPosY + (posY.value - prevPosY) * mc.renderPartialTicks

    var prevWidth = 0.0f; protected set
    var prevHeight = 0.0f; protected set
    val renderWidth get() = prevWidth + (width.value - prevWidth) * mc.renderPartialTicks
    open val renderHeight get() = prevHeight + (height.value - prevHeight) * mc.renderPartialTicks

    // Update methods
    open fun onDisplayed() {}

    open fun onClosed() {}

    open fun onGuiInit() {
        updatePrevPos()
        updatePrevSize()
    }

    open fun onTick() {
        updatePrevPos()
        updatePrevSize()
    }

    private fun updatePrevPos() {
        prevPosX = posX.value
        prevPosY = posY.value
    }

    private fun updatePrevSize() {
        prevWidth = width.value
        prevHeight = height.value
    }

    open fun onRender(vertexHelper: VertexHelper, absolutePos: Vec2f) {}

    enum class SettingGroup(val groupName: String?) {
        NONE(null),
        CLICK_GUI("ClickGui"),
        HUD_GUI("HudGui")
    }

}