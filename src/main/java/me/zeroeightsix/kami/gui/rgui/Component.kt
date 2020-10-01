package me.zeroeightsix.kami.gui.rgui

import com.google.gson.annotations.Expose
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.graphics.VertexHelper
import java.util.*

abstract class Component {
    // Basic info
    val id: UUID = UUID.randomUUID()
    @Expose open var name = id.toString(); protected set
    @Expose open var width = 0.0; protected set
    @Expose open var height = 0.0; protected set

    // Extra info
    protected val mc = Wrapper.minecraft
    open val minWidth: Double = 10.0
    open val minHeight: Double = 10.0
    abstract var maxWidth: Double
    abstract var maxHeight: Double

    // Rendering info
    var prevWidth = 0.0; protected set
    var prevHeight = 0.0; protected set
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

    open fun onRender(vertexHelper: VertexHelper) {}

    override fun equals(other: Any?) = other === this || other is Component && other.id == id

    override fun hashCode() = id.hashCode()
}