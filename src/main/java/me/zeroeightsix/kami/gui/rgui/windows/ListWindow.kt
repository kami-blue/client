package me.zeroeightsix.kami.gui.rgui.windows

import com.google.gson.annotations.Expose
import me.zeroeightsix.kami.gui.clickGui.KamiGuiClickGui
import me.zeroeightsix.kami.gui.rgui.Component
import me.zeroeightsix.kami.gui.rgui.InteractiveComponent
import me.zeroeightsix.kami.module.modules.client.ClickGUI
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.math.Vec2d
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11.*
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

open class ListWindow(
        name: String,
        posX: Double,
        posY: Double,
        width: Double,
        height: Double,
        vararg childrenIn: Component
) : TitledWindow(name, posX, posY, width, height) {
    @Expose
    private val children = LinkedList<Component>()

    override val minWidth = 100.0
    override val minHeight = 200.0
    override val maxWidth = 200.0
    override val maxHeight get() = mc.displayHeight.toDouble()
    private val lineSpace = 4.0
    private var hoveredChild: Component? = null
        set(value) {
            if (value == field) return
            (field as? InteractiveComponent)?.onLeave(KamiGuiClickGui.getRealMousePos())
            (value as? InteractiveComponent)?.onHover(KamiGuiClickGui.getRealMousePos())
            field = value
        }

    private val scrollTimer = TimerUtils.TickTimer()
    private var scrollSpeed = 0.0
    private var prevScrollProgress = 0.0
    private var scrollProgress = 0.0
        set(value) {
            prevScrollProgress = field
            field = value
        }
    private val renderScrollProgress
        get() = prevScrollProgress + (scrollProgress - prevScrollProgress) * mc.renderPartialTicks

    init {
        children.addAll(childrenIn)
        updateChild()
    }

    private fun updateChild() {
        var y = (if (draggableHeight != height) draggableHeight else 0.0) + lineSpace
        for (child in children) {
            child.posX = lineSpace
            child.posY = y
            child.width = width - lineSpace * 2.0
            y += child.height + lineSpace
        }
    }

    override fun onGuiInit() {
        super.onGuiInit()
        updateChild()
    }

    override fun onResize() {
        super.onResize()
        updateChild()
    }

    override fun onTick() {
        super.onTick()
        val maxHeight = max((children.last().posY + children.last.height + lineSpace) - (height), 0.01)
        scrollProgress = (scrollProgress + scrollSpeed)
        scrollSpeed *= 0.5
        if (scrollTimer.tick(100L, false)) {
            if (scrollProgress < 0.0) {
                scrollSpeed = scrollProgress * -0.25
            } else if (scrollProgress > maxHeight) {
                scrollSpeed = (scrollProgress - maxHeight) * -0.25
            }
        }
        updateChild()
        for (child in children) child.onTick()
    }

    override fun onRender(vertexHelper: VertexHelper) {
        super.onRender(vertexHelper)
        glScissor(
                ((renderPosX) * ClickGUI.getScaleFactor()).roundToInt(),
                ((mc.displayHeight - (renderPosY + renderHeight) * ClickGUI.getScaleFactor())).roundToInt(),
                ((renderWidth) * ClickGUI.getScaleFactor()).roundToInt(),
                ((renderHeight - draggableHeight) * ClickGUI.getScaleFactor()).roundToInt()
        )
        glEnable(GL_SCISSOR_TEST)
        glTranslated(0.0, -renderScrollProgress, 0.0)
        for (child in children) {
            glPushMatrix()
            glTranslated(child.posX, child.posY, 0.0)
            child.onRender(vertexHelper)
            glPopMatrix()
        }
        glDisable(GL_SCISSOR_TEST)
    }

    override fun onMouseInput(mousePos: Vec2d) {
        super.onMouseInput(mousePos)
        val relativeMousePos = mousePos.subtract(posX, posY - renderScrollProgress)
        if (Mouse.getEventDWheel() != 0) {
            scrollTimer.reset()
            scrollSpeed += Mouse.getEventDWheel() * ClickGUI.scrollSpeed.value
        }
        hoveredChild = children.firstOrNull { relativeMousePos.y in it.posY..it.posY + it.height }
    }

    override fun onLeave(mousePos: Vec2d) {
        super.onLeave(mousePos)
        hoveredChild = null
    }

    override fun onClick(mousePos: Vec2d, buttonId: Int) {
        super.onClick(mousePos, buttonId)
        (hoveredChild as? InteractiveComponent)?.let {
            val relativePos = mousePos.subtract(posX, posY - renderScrollProgress).subtract(it.posX, it.posY)
            it.onClick(relativePos, buttonId)
        }
    }

    override fun onRelease(mousePos: Vec2d, buttonId: Int) {
        super.onClick(mousePos, buttonId)
        (hoveredChild as? InteractiveComponent)?.let {
            val relativePos = mousePos.subtract(posX, posY - renderScrollProgress).subtract(it.posX, it.posY)
            it.onRelease(relativePos, buttonId)
        }
    }

    override fun onDrag(mousePos: Vec2d, clickPos: Vec2d, buttonId: Int) {
        super.onDrag(mousePos, clickPos, buttonId)
        (hoveredChild as? InteractiveComponent)?.let {
            val relativePos = mousePos.subtract(posX, posY - renderScrollProgress).subtract(it.posX, it.posY)
            it.onDrag(relativePos, clickPos, buttonId)
        }
    }
}