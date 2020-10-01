package me.zeroeightsix.kami.gui.rgui

import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.graphics.VertexHelper

abstract class Component {
    // Basic info of the component
    abstract var name: String; protected set
    abstract var width: Double; protected set
    abstract var height: Double; protected set

    // Extra info
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

    protected val mc = Wrapper.minecraft
}