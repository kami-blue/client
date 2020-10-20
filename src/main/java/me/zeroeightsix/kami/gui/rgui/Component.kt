package me.zeroeightsix.kami.gui.rgui

import com.google.gson.annotations.Expose
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.math.Vec2f
import java.util.*

abstract class Component {
    // Basic info
    val id: UUID = UUID.randomUUID()
    @Expose open var name = id.toString(); protected set
    @Expose open var posX = 0.0f
    @Expose open var posY = 0.0f
    @Expose open var width = 0.0f
    @Expose open var height = 0.0f
    @Expose var visible = true

    // Extra info
    protected val mc = Wrapper.minecraft
    open val minWidth = 16.0f
    open val minHeight = 16.0f
    open val maxWidth = -1.0f
    open val maxHeight = -1.0f

    // Rendering info
    var prevPosX = 0.0f; protected set
    var prevPosY = 0.0f; protected set
    val renderPosX get() = prevPosX + (posX - prevPosX) * mc.renderPartialTicks
    val renderPosY get() = prevPosY + (posY - prevPosY) * mc.renderPartialTicks
    var prevWidth = 0.0f; protected set
    var prevHeight = 0.0f; protected set
    val renderWidth get() = prevWidth + (width - prevWidth) * mc.renderPartialTicks
    val renderHeight get() = prevHeight + (height - prevHeight) * mc.renderPartialTicks

    // Update methods
    open fun onGuiInit() {
        updatePrevSize()
    }

    open fun onTick() {
        updatePrevSize()
    }

    private fun updatePrevSize() {
        prevWidth = width
        prevHeight = height
    }

    open fun onRender(vertexHelper: VertexHelper, absolutePos: Vec2f) {}

    override fun equals(other: Any?) = other === this || other is Component && other.id == id

    override fun hashCode() = id.hashCode()
}