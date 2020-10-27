package me.zeroeightsix.kami.gui.rgui

import me.zeroeightsix.kami.setting.GuiConfig.setting
import me.zeroeightsix.kami.util.graphics.Alignment
import me.zeroeightsix.kami.util.graphics.AnimationUtils
import me.zeroeightsix.kami.util.math.Vec2f
import kotlin.math.max
import kotlin.math.min

abstract class WindowComponent(
        name: String,
        posX: Float,
        posY: Float,
        width: Float,
        height: Float,
        saveToConfig: Boolean
) : InteractiveComponent(name, posX, posY, width, height, saveToConfig) {

    // Basic info
    val minimized = setting("Minimized", false,
            consumer = { _, input -> System.currentTimeMillis() - minimizedTime > 300L && input }
    )

    // Interactive info
    open val draggableHeight get() = height.value
    var lastActiveTime: Long = System.currentTimeMillis(); protected set
    var preDragPos = Vec2f(0.0f, 0.0f); private set
    var preDragSize = Vec2f(0.0f, 0.0f); private set

    // Render info
    private var minimizedTime = 0L
    private val renderMinimizeProgress: Float
        get() {
            val deltaTime = AnimationUtils.toDeltaTimeFloat(minimizedTime)
            return if (minimized.value) AnimationUtils.halfSineDec(deltaTime, 300.0f)
            else AnimationUtils.halfSineInc(deltaTime, 300.0f)
        }
    override val renderHeight: Float
        get() = max(super.renderHeight * renderMinimizeProgress, draggableHeight)

    open val resizable get() = true
    open val minimizable get() = false

    init {
        minimized.valueListeners.add { prev, it ->
            if (it != prev) minimizedTime = System.currentTimeMillis()
        }
    }

    open fun onResize() {}
    open fun onReposition() {}

    override fun onGuiInit() {
        super.onGuiInit()
        updatePreDrag()
    }

    override fun onClick(mousePos: Vec2f, buttonId: Int) {
        super.onClick(mousePos, buttonId)
        updatePreDrag()
        lastActiveTime = System.currentTimeMillis()
    }

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        updatePreDrag()
        lastActiveTime = System.currentTimeMillis()
        if (minimizable && buttonId == 1 && mousePos.y - posY.value < draggableHeight) minimized.value = !minimized.value
    }

    private fun updatePreDrag() {
        preDragPos = Vec2f(posX.value, posY.value)
        preDragSize = Vec2f(width.value, height.value)
    }

    override fun onDrag(mousePos: Vec2f, clickPos: Vec2f, buttonId: Int) {
        super.onDrag(mousePos, clickPos, buttonId)

        if (!resizable) return
        val relativeClickPos = clickPos.subtract(preDragPos)
        val centerSplitterH = min(10.0, preDragSize.x / 3.0)
        val centerSplitterV = min(10.0, preDragSize.y / 3.0)

        val horizontalSide = when (relativeClickPos.x) {
            in -5.0..centerSplitterH -> Alignment.HAlign.LEFT
            in centerSplitterH..preDragSize.x - centerSplitterH -> Alignment.HAlign.CENTER
            in preDragSize.x - centerSplitterH..preDragSize.x + 5.0 -> Alignment.HAlign.RIGHT
            else -> null
        }

        val centerSplitterVCenter = if (draggableHeight != height.value && horizontalSide == Alignment.HAlign.CENTER) 2.5 else min(15.0, preDragSize.x / 3.0)
        val verticalSide = when (relativeClickPos.y) {
            in -5.0..centerSplitterVCenter -> Alignment.VAlign.TOP
            in centerSplitterVCenter..preDragSize.y - centerSplitterV -> Alignment.VAlign.CENTER
            in preDragSize.y - centerSplitterV..preDragSize.y + 5.0 -> Alignment.VAlign.BOTTOM
            else -> null
        }

        val draggedDist = mousePos.subtract(clickPos)

        if (horizontalSide != null && verticalSide != null) {
            if (!minimized.value && (horizontalSide != Alignment.HAlign.CENTER || verticalSide != Alignment.VAlign.CENTER)) {

                when (horizontalSide) {
                    Alignment.HAlign.LEFT -> {
                        var newWidth = max(preDragSize.x - draggedDist.x, minWidth)
                        if (maxWidth != -1.0f) newWidth = min(newWidth, maxWidth)

                        posX.value += width.value - newWidth
                        width.value = newWidth
                    }
                    Alignment.HAlign.RIGHT -> {
                        var newWidth = max(preDragSize.x + draggedDist.x, minWidth)
                        if (maxWidth != -1.0f) newWidth = min(newWidth, maxWidth)

                        width.value = newWidth
                    }
                    else -> {
                        // Nothing lol
                    }
                }

                when (verticalSide) {
                    Alignment.VAlign.TOP -> {
                        var newHeight = max(preDragSize.y - draggedDist.y, minHeight)
                        if (maxHeight != -1.0f) newHeight = min(newHeight, maxHeight)

                        posY.value += height.value - newHeight
                        height.value = newHeight
                    }
                    Alignment.VAlign.BOTTOM -> {
                        var newHeight = max(preDragSize.y + draggedDist.y, minHeight)
                        if (maxHeight != -1.0f) newHeight = min(newHeight, maxHeight)

                        height.value = newHeight
                    }
                    else -> {
                        // Nothing lol
                    }
                }

                onResize()
            } else if (relativeClickPos.y <= draggableHeight) {
                posX.value = (preDragPos.x + draggedDist.x).coerceIn(0.0f, mc.displayWidth - width.value)
                posY.value = (preDragPos.y + draggedDist.y).coerceIn(0.0f, mc.displayHeight - height.value)

                onReposition()
            } else {
                // TODO
            }
        }
    }

    fun isInWindow(mousePos: Vec2f): Boolean {
        return mousePos.x in preDragPos.x - 5.0f..preDragPos.x + preDragSize.x + 5.0f
                && mousePos.y in preDragPos.y - 5.0f..preDragPos.y + max(preDragSize.y * renderMinimizeProgress, draggableHeight) + 5.0f
    }
}