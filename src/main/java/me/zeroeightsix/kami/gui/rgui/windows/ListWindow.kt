package me.zeroeightsix.kami.gui.rgui.windows

import me.zeroeightsix.kami.gui.clickgui.KamiClickGui
import me.zeroeightsix.kami.gui.rgui.Component
import me.zeroeightsix.kami.gui.rgui.InteractiveComponent
import me.zeroeightsix.kami.module.modules.client.ClickGUI
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.graphics.GlStateUtils
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.math.Vec2f
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11.*
import java.util.*
import kotlin.math.max
import kotlin.math.roundToInt

open class ListWindow(
        name: String,
        posX: Float,
        posY: Float,
        width: Float,
        height: Float,
        saveToConfig: Boolean,
        vararg childrenIn: Component
) : TitledWindow(name, posX, posY, width, height, saveToConfig) {
    val children = LinkedList<Component>()

    override val minWidth = 80.0f
    override val minHeight = 200.0f
    override val maxWidth = 200.0f
    override val maxHeight get() = mc.displayHeight.toFloat()
    override val resizable: Boolean get() = hoveredChild == null

    protected val lineSpace = 3.0f
    var hoveredChild: Component? = null
        private set(value) {
            if (value == field) return
            (field as? InteractiveComponent)?.onLeave(KamiClickGui.getRealMousePos())
            (value as? InteractiveComponent)?.onHover(KamiClickGui.getRealMousePos())
            field = value
        }

    private val scrollTimer = TimerUtils.TickTimer()
    private var scrollSpeed = 0.0f

    private var scrollProgress = 0.0f
        set(value) {
            prevScrollProgress = field
            field = value
        }
    private var prevScrollProgress = 0.0f
    protected val renderScrollProgress
        get() = prevScrollProgress + (scrollProgress - prevScrollProgress) * mc.renderPartialTicks

    init {
        children.addAll(childrenIn)
        updateChild()
    }

    private fun updateChild() {
        var y = (if (draggableHeight != height.value) draggableHeight else 0.0f) + lineSpace
        for (child in children) {
            if (!child.visible.value) continue
            child.posX.value = lineSpace * 1.618f
            child.posY.value = y
            child.width.value = width.value - lineSpace * 3.236f
            y += child.height.value + lineSpace
        }
    }

    override fun onGuiInit() {
        super.onGuiInit()
        for (child in children) child.onGuiInit()
        updateChild()
    }

    override fun onResize() {
        super.onResize()
        updateChild()
    }

    override fun onTick() {
        super.onTick()
        if (children.isEmpty()) return
        val lastVisible = children.lastOrNull { it.visible.value }
        val maxScrollProgress = lastVisible?.let { max(it.posY.value + it.height.value + lineSpace - height.value, 0.01f) }?: draggableHeight

        scrollProgress = (scrollProgress + scrollSpeed)
        scrollSpeed *= 0.5f
        if (scrollTimer.tick(100L, false)) {
            if (scrollProgress < 0.0) {
                scrollSpeed = scrollProgress * -0.25f
            } else if (scrollProgress > maxScrollProgress) {
                scrollSpeed = (scrollProgress - maxScrollProgress) * -0.25f
            }
        }

        updateChild()
        for (child in children) child.onTick()
    }

    override fun onRender(vertexHelper: VertexHelper, absolutePos: Vec2f) {
        super.onRender(vertexHelper, absolutePos)
        GlStateUtils.scissor(
                ((renderPosX + lineSpace * 1.618) * ClickGUI.getScaleFactor()).roundToInt(),
                ((mc.displayHeight - (renderPosY + renderHeight) * ClickGUI.getScaleFactor())).roundToInt(),
                ((renderWidth - lineSpace * 3.236) * ClickGUI.getScaleFactor()).roundToInt(),
                ((renderHeight - draggableHeight) * ClickGUI.getScaleFactor()).roundToInt()
        )
        glEnable(GL_SCISSOR_TEST)
        glTranslatef(0.0f, -renderScrollProgress, 0.0f)
        for (child in children) {
            if (!child.visible.value) continue
            glPushMatrix()
            glTranslatef(child.posX.value, child.posY.value, 0.0f)
            child.onRender(vertexHelper, absolutePos.add(child.posX.value, child.posY.value))
            glPopMatrix()
        }
        glDisable(GL_SCISSOR_TEST)
    }

    override fun onMouseInput(mousePos: Vec2f) {
        super.onMouseInput(mousePos)
        val relativeMousePos = mousePos.subtract(posX.value, posY.value - renderScrollProgress)
        if (Mouse.getEventDWheel() != 0) {
            scrollTimer.reset()
            scrollSpeed -= Mouse.getEventDWheel() * 0.1f
            updateHovered(relativeMousePos)
        }
        if (mouseState != MouseState.DRAG) {
            updateHovered(relativeMousePos)
        }
    }

    private fun updateHovered(relativeMousePos: Vec2f) {
        hoveredChild = if (relativeMousePos.y < draggableHeight || relativeMousePos.x < lineSpace || relativeMousePos.x > renderWidth - lineSpace) null
        else children.firstOrNull { it.visible.value && relativeMousePos.y in it.posY.value..it.posY.value + it.height.value }
    }

    override fun onLeave(mousePos: Vec2f) {
        super.onLeave(mousePos)
        hoveredChild = null
    }

    override fun onClick(mousePos: Vec2f, buttonId: Int) {
        super.onClick(mousePos, buttonId)
        (hoveredChild as? InteractiveComponent)?.let {
            it.onClick(getRelativeMousePos(mousePos, it), buttonId)
        }
    }

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onClick(mousePos, buttonId)
        (hoveredChild as? InteractiveComponent)?.let {
            it.onRelease(getRelativeMousePos(mousePos, it), buttonId)
        }
    }

    override fun onDrag(mousePos: Vec2f, clickPos: Vec2f, buttonId: Int) {
        super.onDrag(mousePos, clickPos, buttonId)
        (hoveredChild as? InteractiveComponent)?.let {
            it.onDrag(getRelativeMousePos(mousePos, it), clickPos, buttonId)
        }
    }

    override fun onKeyInput(keyCode: Int, keyState: Boolean) {
        super.onKeyInput(keyCode, keyState)
        (hoveredChild as? InteractiveComponent)?.onKeyInput(keyCode, keyState)
    }

    private fun getRelativeMousePos(mousePos: Vec2f, component: InteractiveComponent) =
            mousePos.subtract(posX.value, posY.value - renderScrollProgress).subtract(component.posX.value, component.posY.value)
}