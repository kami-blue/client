package me.zeroeightsix.kami.gui.rgui

import me.zeroeightsix.kami.util.graphics.Alignment
import me.zeroeightsix.kami.util.math.Vec2f
import kotlin.math.max
import kotlin.math.min

abstract class WindowComponent : InteractiveComponent() {
    // Interactive info
    open val draggableHeight get() = height
    var lastActiveTime: Long = System.currentTimeMillis(); protected set
    var preDragPos = Vec2f(0.0f, 0.0f); private set
    var preDragSize = Vec2f(0.0f, 0.0f); private set

    open fun onResize() {}
    open fun onReposition() {}

    override fun onGuiInit() {
        super.onGuiInit()
        updatePrevPos()
        updatePreDrag()
    }

    override fun onTick() {
        super.onTick()
        updatePrevPos()
    }

    private fun updatePrevPos() {
        prevPosX = posX
        prevPosY = posY
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
    }

    private fun updatePreDrag() {
        preDragPos = Vec2f(posX, posY)
        preDragSize = Vec2f(width, height)
    }

    override fun onDrag(mousePos: Vec2f, clickPos: Vec2f, buttonId: Int) {
        super.onDrag(mousePos, clickPos, buttonId)
        val relativeClickPos = clickPos.subtract(preDragPos)
        val centerSplitterH = min(10.0, preDragSize.x / 3.0)
        val centerSplitterV = min(10.0, preDragSize.y / 3.0)

        val horizontalSide = when (relativeClickPos.x) {
            in -5.0..centerSplitterH -> Alignment.HAlign.LEFT
            in centerSplitterH..preDragSize.x - centerSplitterH -> Alignment.HAlign.CENTER
            in preDragSize.x - centerSplitterH..preDragSize.x + 5.0 -> Alignment.HAlign.RIGHT
            else -> null
        }

        val centerSplitterVCenter = if (draggableHeight != height && horizontalSide == Alignment.HAlign.CENTER) 2.5 else min(15.0, preDragSize.x / 3.0)
        val verticalSide = when (relativeClickPos.y) {
            in -5.0..centerSplitterVCenter -> Alignment.VAlign.TOP
            in centerSplitterVCenter..preDragSize.y - centerSplitterV -> Alignment.VAlign.CENTER
            in preDragSize.y - centerSplitterV..preDragSize.y + 5.0 -> Alignment.VAlign.BOTTOM
            else -> null
        }

        val draggedDist = mousePos.subtract(clickPos)

        if (horizontalSide != null && verticalSide != null) {
            if (horizontalSide != Alignment.HAlign.CENTER || verticalSide != Alignment.VAlign.CENTER) {

                when (horizontalSide) {
                    Alignment.HAlign.LEFT -> {
                        var newWidth = max(preDragSize.x - draggedDist.x, minWidth)
                        if (maxWidth != -1.0f) newWidth = min(newWidth, maxWidth)

                        posX += width - newWidth
                        width = newWidth
                    }
                    Alignment.HAlign.RIGHT -> {
                        var newWidth = max(preDragSize.x + draggedDist.x, minWidth)
                        if (maxWidth != -1.0f) newWidth = min(newWidth, maxWidth)

                        width = newWidth
                    }
                    else -> {
                        // Nothing lol
                    }
                }

                when (verticalSide) {
                    Alignment.VAlign.TOP -> {
                        var newHeight = max(preDragSize.y - draggedDist.y, minHeight)
                        if (maxHeight != -1.0f) newHeight = min(newHeight, maxHeight)

                        posY += height - newHeight
                        height = newHeight
                    }
                    Alignment.VAlign.BOTTOM -> {
                        var newHeight = max(preDragSize.y + draggedDist.y, minHeight)
                        if (maxHeight != -1.0f) newHeight = min(newHeight, maxHeight)

                        height = newHeight
                    }
                    else -> {
                        // Nothing lol
                    }
                }

                onResize()
            } else if (relativeClickPos.y <= draggableHeight) {
                posX = (preDragPos.x + draggedDist.x).coerceIn(0.0f, mc.displayWidth - width)
                posY = (preDragPos.y + draggedDist.y).coerceIn(0.0f, mc.displayHeight - height)

                onReposition()
            } else {
                // TODO
            }
        }
    }

    fun isInWindow(mousePos: Vec2f): Boolean {
        return mousePos.x in preDragPos.x - 5.0f..preDragPos.x + preDragSize.x + 5.0f
                && mousePos.y in preDragPos.y - 5.0f..preDragPos.y + preDragSize.y + 5.0f
    }
}