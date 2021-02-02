package org.kamiblue.client.gui.rgui

import org.kamiblue.client.KamiMod
import org.kamiblue.client.module.modules.client.ClickGUI
import org.kamiblue.client.setting.GuiConfig.setting
import org.kamiblue.client.util.Wrapper
import org.kamiblue.client.util.graphics.VertexHelper
import org.kamiblue.client.util.graphics.font.HAlign
import org.kamiblue.client.util.graphics.font.VAlign
import org.kamiblue.client.util.math.Vec2f
import kotlin.math.max

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
    var name by setting("Name", name, { false })
    protected val visibleSetting = setting("Visible", true, { false }, { _, it -> it || !closeable })
    var visible by visibleSetting

    protected val dockingHSetting = setting("DockingH", HAlign.LEFT)
    protected val dockingVSetting = setting("DockingV", VAlign.TOP)

    var width by setting("Width", widthIn, 0.0f..69420.911f, 0.1f, { false }, { _, it -> it.coerceIn(minWidth, max(scaledWidth, minWidth)) })
    var height by setting("Height", heightIn, 0.0f..69420.911f, 0.1f, { false }, { _, it -> it.coerceIn(minHeight, max(scaledHeight, minHeight)) })

    protected var relativePosX by setting("PosX", posXIn, -69420.911f..69420.911f, 0.1f, { false },
        { _, it -> if (this is WindowComponent && KamiMod.isReady()) absToRelativeX(relativeToAbsX(it).coerceIn(2.0f, max(scaledWidth - width - 2.0f, 2.0f))) else it })
    protected var relativePosY by setting("PosY", posYIn, -69420.911f..69420.911f, 0.1f, { false },
        { _, it -> if (this is WindowComponent && KamiMod.isReady()) absToRelativeY(relativeToAbsY(it).coerceIn(2.0f, max(scaledHeight - height - 2.0f, 2.0f))) else it })

    var dockingH by dockingHSetting
    var dockingV by dockingVSetting

    var posX: Float
        get() {
            return relativeToAbsX(relativePosX)
        }
        set(value) {
            if (!KamiMod.isReady()) return
            relativePosX = absToRelativeX(value)
        }

    var posY: Float
        get() {
            return relativeToAbsY(relativePosY)
        }
        set(value) {
            if (!KamiMod.isReady()) return
            relativePosY = absToRelativeY(value)
        }

    init {
        dockingHSetting.listeners.add { posX = prevPosX }
        dockingVSetting.listeners.add { posY = prevPosY }
    }

    // Extra info
    protected val mc = Wrapper.minecraft
    open val minWidth = 1.0f
    open val minHeight = 1.0f
    open val maxWidth = -1.0f
    open val maxHeight = -1.0f
    open val closeable: Boolean get() = true

    // Rendering info
    var prevPosX = 0.0f; protected set
    var prevPosY = 0.0f; protected set
    val renderPosX get() = prevPosX + prevDockWidth + (posX + dockWidth - (prevPosX + prevDockWidth)) * mc.renderPartialTicks - dockWidth
    val renderPosY get() = prevPosY + prevDockHeight + (posY + dockHeight - (prevPosY + prevDockHeight)) * mc.renderPartialTicks - dockHeight

    var prevWidth = 0.0f; protected set
    var prevHeight = 0.0f; protected set
    val renderWidth get() = prevWidth + (width - prevWidth) * mc.renderPartialTicks
    open val renderHeight get() = prevHeight + (height - prevHeight) * mc.renderPartialTicks

    private fun relativeToAbsX(xIn: Float) = xIn + scaledWidth * dockingH.multiplier - dockWidth
    private fun relativeToAbsY(yIn: Float) = yIn + scaledHeight * dockingV.multiplier - dockHeight
    private fun absToRelativeX(xIn: Float) = xIn - scaledWidth * dockingH.multiplier + dockWidth
    private fun absToRelativeY(yIn: Float) = yIn - scaledHeight * dockingV.multiplier + dockHeight

    private val scaledWidth get() = mc.displayWidth / ClickGUI.getScaleFactorFloat()
    private val scaledHeight get() = mc.displayHeight / ClickGUI.getScaleFactorFloat()
    private val dockWidth get() = width * dockingH.multiplier
    private val dockHeight get() = height * dockingV.multiplier
    private val prevDockWidth get() = prevWidth * dockingH.multiplier
    private val prevDockHeight get() = prevHeight * dockingV.multiplier

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
        prevWidth = width
        prevHeight = height
    }

    open fun onRender(vertexHelper: VertexHelper, absolutePos: Vec2f) {}

    open fun onPostRender(vertexHelper: VertexHelper, absolutePos: Vec2f) {}

    enum class SettingGroup(val groupName: String) {
        NONE(""),
        CLICK_GUI("click_gui"),
        HUD_GUI("hud_gui")
    }

}