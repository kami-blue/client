package me.zeroeightsix.kami.gui.rgui.windows

import com.google.gson.annotations.Expose
import me.zeroeightsix.kami.gui.clickGui.KamiGuiClickGui
import me.zeroeightsix.kami.gui.rgui.Component
import me.zeroeightsix.kami.gui.rgui.InteractiveComponent
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.math.Vec2d
import org.lwjgl.opengl.GL11.*

open class ListWindow(
        name: String,
        posX: Double,
        posY: Double,
        width: Double,
        height: Double,
        vararg childrenIn: Component
) : TitledWindow(name, posX, posY, width, height) {
    @Expose
    private val children = LinkedHashMap<Component, Vec2d>()
    var lineSpace = 4.0
    private var hoveredChild: Component? = null
        set(value) {
            if (value == field) return
            (field as? InteractiveComponent)?.onLeave(KamiGuiClickGui.getRealMousePos())
            (value as? InteractiveComponent)?.onHover(KamiGuiClickGui.getRealMousePos())
            field = value
        }

    init {
        updateChild(childrenIn.toList())
    }

    private fun updateChild(list: List<Component>) {
        var y = (if (draggableHeight != height) draggableHeight else 0.0) + lineSpace
        for (child in list) {
            children[child] = Vec2d(lineSpace, y)
            child.width = width - lineSpace * 2.0
            y += child.height + lineSpace
        }
    }

    override fun onGuiInit() {
        super.onGuiInit()
        updateChild(children.keys.toList())
    }

    override fun onResize() {
        super.onResize()
        updateChild(children.keys.toList())
    }

    override fun onTick() {
        super.onTick()
        updateChild(children.keys.toList())
        for (child in children.keys) child.onTick()
    }

    override fun onRender(vertexHelper: VertexHelper) {
        super.onRender(vertexHelper)
        for ((child, pos) in children) {
            glPushMatrix()
            glTranslated(pos.x, pos.y, 0.0)
            child.onRender(vertexHelper)
            glPopMatrix()
        }
    }

    override fun onMouseInput(mousePos: Vec2d) {
        super.onMouseInput(mousePos)
        val relativeMousePos = mousePos.subtract(posX, posY)
        hoveredChild = children.entries.firstOrNull { relativeMousePos.y >= it.value.y && relativeMousePos.y <= it.value.y + it.key.height }?.key
    }

    override fun onLeave(mousePos: Vec2d) {
        super.onLeave(mousePos)
        hoveredChild = null
    }

    override fun onClick(mousePos: Vec2d, buttonId: Int) {
        super.onClick(mousePos, buttonId)
        (hoveredChild as? InteractiveComponent)?.let {
            val relativePos = mousePos.subtract(posX, posY).subtract(children[it]!!)
            it.onClick(relativePos, buttonId)
        }
    }

    override fun onRelease(mousePos: Vec2d, buttonId: Int) {
        super.onClick(mousePos, buttonId)
        (hoveredChild as? InteractiveComponent)?.let {
            val relativePos = mousePos.subtract(posX, posY).subtract(children[it]!!)
            it.onRelease(relativePos, buttonId)
        }
    }

    override fun onDrag(mousePos: Vec2d, clickPos: Vec2d, buttonId: Int) {
        super.onDrag(mousePos, clickPos, buttonId)
        (hoveredChild as? InteractiveComponent)?.let {
            val relativePos = mousePos.subtract(posX, posY).subtract(children[it]!!)
            it.onDrag(relativePos, clickPos, buttonId)
        }
    }
}