package me.zeroeightsix.kami.gui.rgui

import me.zeroeightsix.kami.util.graphics.Alignment
import me.zeroeightsix.kami.util.math.Vec2d
import kotlin.math.max
import kotlin.math.min

abstract class WindowComponent : InteractiveComponent() {
    // Basic info of the Window
    abstract var posX: Double
    abstract var posY: Double

    // Interactive info
    override var maxWidth: Double = -1.0
    override var maxHeight: Double = -1.0
    open val draggableHeight get() = height
    var lastActiveTime: Long = System.currentTimeMillis(); private set
    var preDragPos = Vec2d(0.0, 0.0); private set
    var preDragSize = Vec2d(0.0, 0.0); private set

    // Render info
    var prevPosX = 0.0; private set
    var prevPosY = 0.0; private set
    val renderPosX get() = prevPosX + (posX - prevPosX) * mc.renderPartialTicks
    val renderPosY get() = prevPosY + (posY - prevPosY) * mc.renderPartialTicks

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

    override fun onClick(mousePos: Vec2d, buttonId: Int) {
        super.onClick(mousePos, buttonId)
        updatePreDrag()
        lastActiveTime = System.currentTimeMillis()
    }

    override fun onRelease(mousePos: Vec2d, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        updatePreDrag()
        lastActiveTime = System.currentTimeMillis()
    }

    private fun updatePreDrag() {
        preDragPos = Vec2d(posX, posY)
        preDragSize = Vec2d(width, height)
    }

    override fun onDrag(mousePos: Vec2d, clickPos: Vec2d, buttonId: Int) {
        super.onDrag(mousePos, clickPos, buttonId)
        val relativeClickPos = clickPos.subtract(preDragPos)
        val horizontalSide = when (relativeClickPos.x) {
            in -2.5..2.5 -> Alignment.HAlign.LEFT
            in 2.5..preDragSize.x - 2.5 -> Alignment.HAlign.CENTER
            in preDragSize.x - 2.5..preDragSize.x + 2.5 -> Alignment.HAlign.RIGHT
            else -> null
        }

        val verticalSide = when (relativeClickPos.y) {
            in -2.5..2.5 -> Alignment.VAlign.TOP
            in 2.5..preDragSize.y - 2.5 -> Alignment.VAlign.CENTER
            in preDragSize.y - 2.5..preDragSize.y + 2.5 -> Alignment.VAlign.BOTTOM
            else -> null
        }

        val draggedDist = mousePos.subtract(clickPos)

        if (horizontalSide != null && verticalSide != null) {
            if (horizontalSide != Alignment.HAlign.CENTER || verticalSide != Alignment.VAlign.CENTER) {

                when (horizontalSide) {
                    Alignment.HAlign.LEFT -> {
                        if (preDragSize.x - draggedDist.x >= minWidth) {
                            width = preDragSize.x - draggedDist.x
                            posX = preDragPos.x + draggedDist.x
                        }
                    }
                    Alignment.HAlign.RIGHT -> {
                        if (preDragSize.x + draggedDist.x >= minWidth) {
                            width = preDragSize.x + draggedDist.x
                        }
                    }
                    else -> {
                        // Nothing lol
                    }
                }

                when (verticalSide) {
                    Alignment.VAlign.TOP -> {
                        if (preDragSize.y - draggedDist.y >= minHeight) {
                            height = preDragSize.y - draggedDist.y
                            posY = preDragPos.y + draggedDist.y
                        }
                    }
                    Alignment.VAlign.BOTTOM -> {
                        if (preDragSize.y + draggedDist.y >= minHeight) {
                            height = preDragSize.y + draggedDist.y
                        }
                    }
                    else -> {
                        // Nothing lol
                    }
                }

                width = max(width, minWidth)
                height = max(height, minHeight)
                if (maxWidth != -1.0) width = min(width, maxWidth)
                if (maxHeight != -1.0) height = min(height, maxHeight + draggableHeight)

                onResize()
            } else if (relativeClickPos.y <= draggableHeight) {
                posX = preDragPos.x + draggedDist.x
                posY = preDragPos.y + draggedDist.y

                onReposition()
            } else {
                // TODO
            }
        }
    }
}