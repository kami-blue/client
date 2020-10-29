package me.zeroeightsix.kami.gui.rgui

import me.zeroeightsix.kami.module.modules.client.ClickGUI
import me.zeroeightsix.kami.setting.GuiConfig.setting
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.graphics.Alignment
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.math.Vec2f

open class Component(
        name: String,
        posXIn: Float,
        posYIn: Float,
        widthIn: Float,
        heightIn: Float,
        val settingGroup: SettingGroup
) {

    // Basic info
    val originalName = name
    val name = setting("Name", name, { false })
    val visible = setting("Visible", true, { false }, { _, it -> it || !closeable })

    private val relativePosX = setting("PosX", posXIn, -69420.911f..69420.911f, 0.1f, { false },
            { _, it -> absToRelativeX(relativeToAbsX(it).coerceIn(0.0f, mc.displayWidth / ClickGUI.getScaleFactorFloat() - width.value * dockingH.value.multiplier)) })
    private val relativePosY = setting("PosY", posYIn, -69420.911f..69420.911f, 0.1f, { false },
            { _, it -> absToRelativeY(relativeToAbsY(it).coerceIn(0.0f, mc.displayHeight / ClickGUI.getScaleFactorFloat() - height.value * dockingV.value.multiplier)) })

    val width = setting("Width", widthIn, 0.0f..69420.911f, 0.1f, { false }, { _, it -> it.coerceIn(0.0f, mc.displayWidth / ClickGUI.getScaleFactorFloat()) })
    val height = setting("Height", heightIn, 0.0f..69420.911f, 0.1f, { false }, { _, it -> it.coerceIn(0.0f, mc.displayHeight / ClickGUI.getScaleFactorFloat()) })

    val dockingH = setting("DockingH", Alignment.HAlign.LEFT)
    val dockingV = setting("DockingV", Alignment.VAlign.TOP)

    var posX: Float = 0.0f
        get() {
            return relativeToAbsX(relativePosX.value)
        }
        set(value) {
            field = absToRelativeX(value)
            relativePosX.value = field
        }

    var posY: Float = 0.0f
        get() {
            return relativeToAbsY(relativePosY.value)
        }
        set(value) {
            field = absToRelativeY(value)
            relativePosY.value = field
        }

    init {
        dockingH.listeners.add { posX = prevPosX }
        dockingV.listeners.add { posY = prevPosY }
    }

    private fun relativeToAbsX(xIn: Float) = xIn + mc.displayWidth / ClickGUI.getScaleFactorFloat() * dockingH.value.multiplier - width.value * dockingH.value.multiplier
    private fun relativeToAbsY(yIn: Float) = yIn + mc.displayHeight / ClickGUI.getScaleFactorFloat() * dockingV.value.multiplier - height.value * dockingV.value.multiplier
    private fun absToRelativeX(xIn: Float) = xIn - mc.displayWidth / ClickGUI.getScaleFactorFloat() * dockingH.value.multiplier + width.value * dockingH.value.multiplier
    private fun absToRelativeY(yIn: Float) = yIn - mc.displayHeight / ClickGUI.getScaleFactorFloat() * dockingV.value.multiplier + height.value * dockingV.value.multiplier

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
    val renderPosX get() = prevPosX + (posX - prevPosX) * mc.renderPartialTicks
    val renderPosY get() = prevPosY + (posY - prevPosY) * mc.renderPartialTicks

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
        prevPosX = posX
        prevPosY = posY
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