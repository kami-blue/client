package me.zeroeightsix.kami.gui.rgui

import me.zeroeightsix.kami.setting.GuiConfig.setting
import me.zeroeightsix.kami.util.graphics.AnimationUtils
import me.zeroeightsix.kami.util.graphics.font.HAlign
import me.zeroeightsix.kami.util.graphics.font.VAlign
import me.zeroeightsix.kami.util.math.Vec2f
import kotlin.math.max
import kotlin.math.min

open class WindowComponent(
    name: String,
    posX: Float,
    posY: Float,
    width: Float,
    height: Float,
    settingGroup: SettingGroup
) : InteractiveComponent(name, posX, posY, width, height, settingGroup) {

    // Basic info
    private val minimizedSetting = setting("Minimized", false,
        { false }, { _, input -> System.currentTimeMillis() - minimizedTime > 300L && input }
    )
    var minimized by minimizedSetting

    // Interactive info
    open val draggableHeight get() = height
    var lastActiveTime: Long = System.currentTimeMillis(); protected set
    var preDragMousePos = Vec2f.ZERO; private set
    var preDragPos = Vec2f.ZERO; private set
    var preDragSize = Vec2f.ZERO; private set

    // Render info
    private var minimizedTime = 0L
    private val renderMinimizeProgress: Float
        get() {
            val deltaTime = AnimationUtils.toDeltaTimeFloat(minimizedTime)
            return if (minimized) AnimationUtils.halfSineDec(deltaTime, 200.0f)
            else AnimationUtils.halfSineInc(deltaTime, 200.0f)
        }
    override val renderHeight: Float
        get() = max(super.renderHeight * renderMinimizeProgress, draggableHeight)

    open val resizable get() = true
    open val minimizable get() = false

    init {
        minimizedSetting.valueListeners.add { prev, it ->
            if (it != prev) minimizedTime = System.currentTimeMillis()
        }
    }

    open fun onResize() {}
    open fun onReposition() {}

    override fun onDisplayed() {
        super.onDisplayed()
        if (!minimized) {
            minimized = true
            minimized = false
        }
    }

    override fun onGuiInit() {
        super.onGuiInit()
        updatePreDrag(null)
    }

    override fun onMouseInput(mousePos: Vec2f) {
        super.onMouseInput(mousePos)
        if (mouseState != MouseState.DRAG) updatePreDrag(mousePos.minus(posX, posY))
    }

    override fun onClick(mousePos: Vec2f, buttonId: Int) {
        super.onClick(mousePos, buttonId)
        lastActiveTime = System.currentTimeMillis()
    }

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        lastActiveTime = System.currentTimeMillis()
        if (minimizable && buttonId == 1 && mousePos.y - posY < draggableHeight) minimized = !minimized
        if (mouseState != MouseState.DRAG) updatePreDrag(mousePos.minus(posX, posY))
    }

    private fun updatePreDrag(mousePos: Vec2f?) {
        mousePos?.let { preDragMousePos = it }
        preDragPos = Vec2f(posX, posY)
        preDragSize = Vec2f(width, height)
    }

    override fun onDrag(mousePos: Vec2f, clickPos: Vec2f, buttonId: Int) {
        super.onDrag(mousePos, clickPos, buttonId)

        val relativeClickPos = clickPos.minus(preDragPos)
        val centerSplitterH = min(10.0, preDragSize.x / 3.0)
        val centerSplitterV = min(10.0, preDragSize.y / 3.0)

        val horizontalSide = when (relativeClickPos.x) {
            in -2.0..centerSplitterH -> HAlign.LEFT
            in centerSplitterH..preDragSize.x - centerSplitterH -> HAlign.CENTER
            in preDragSize.x - centerSplitterH..preDragSize.x + 2.0 -> HAlign.RIGHT
            else -> null
        }

        val centerSplitterVCenter = if (draggableHeight != height && horizontalSide == HAlign.CENTER) 2.5 else min(15.0, preDragSize.x / 3.0)
        val verticalSide = when (relativeClickPos.y) {
            in -2.0..centerSplitterVCenter -> VAlign.TOP
            in centerSplitterVCenter..preDragSize.y - centerSplitterV -> VAlign.CENTER
            in preDragSize.y - centerSplitterV..preDragSize.y + 2.0 -> VAlign.BOTTOM
            else -> null
        }

        val draggedDist = mousePos.minus(clickPos)

        if (horizontalSide != null && verticalSide != null) {
            if (resizable && !minimized && (horizontalSide != HAlign.CENTER || verticalSide != VAlign.CENTER)) {

                when (horizontalSide) {
                    HAlign.LEFT -> {
                        var newWidth = max(preDragSize.x - draggedDist.x, minWidth)
                        if (maxWidth != -1.0f) newWidth = min(newWidth, maxWidth)

                        posX += width - newWidth
                        width = newWidth
                    }
                    HAlign.RIGHT -> {
                        var newWidth = max(preDragSize.x + draggedDist.x, minWidth)
                        if (maxWidth != -1.0f) newWidth = min(newWidth, maxWidth)

                        width = newWidth
                    }
                    else -> {
                        // Nothing lol
                    }
                }

                when (verticalSide) {
                    VAlign.TOP -> {
                        var newHeight = max(preDragSize.y - draggedDist.y, minHeight)
                        if (maxHeight != -1.0f) newHeight = min(newHeight, maxHeight)

                        posY += height - newHeight
                        height = newHeight
                    }
                    VAlign.BOTTOM -> {
                        var newHeight = max(preDragSize.y + draggedDist.y, minHeight)
                        if (maxHeight != -1.0f) newHeight = min(newHeight, maxHeight)

                        height = newHeight
                    }
                    else -> {
                        // Nothing lol
                    }
                }

                onResize()
            } else if (draggableHeight == height || relativeClickPos.y <= draggableHeight) {
                posX = (preDragPos.x + draggedDist.x).coerceIn(0.0f, mc.displayWidth - width)
                posY = (preDragPos.y + draggedDist.y).coerceIn(0.0f, mc.displayHeight - height)

                onReposition()
            } else {
                // TODO
            }
        }
    }

    fun isInWindow(mousePos: Vec2f): Boolean {
        return visible && mousePos.x in preDragPos.x - 2.0f..preDragPos.x + preDragSize.x + 2.0f
            && mousePos.y in preDragPos.y - 2.0f..preDragPos.y + max(preDragSize.y * renderMinimizeProgress, draggableHeight) + 2.0f
    }

    init {
        with({ updatePreDrag(null) }) {
            dockingHSetting.listeners.add(this)
            dockingVSetting.listeners.add(this)
        }
    }

}