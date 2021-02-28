package org.kamiblue.client.gui.rgui

import org.kamiblue.client.setting.GuiConfig.setting
import org.kamiblue.client.setting.configs.AbstractConfig
import org.kamiblue.client.util.graphics.AnimationUtils
import org.kamiblue.client.util.graphics.font.HAlign
import org.kamiblue.client.util.graphics.font.VAlign
import org.kamiblue.client.util.math.Vec2f
import org.kamiblue.commons.interfaces.Nameable
import kotlin.math.max
import kotlin.math.min

open class WindowComponent(
    name: String,
    posX: Float,
    posY: Float,
    width: Float,
    height: Float,
    settingGroup: SettingGroup,
    config: AbstractConfig<out Nameable>
) : InteractiveComponent(name, posX, posY, width, height, settingGroup, config) {

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
                handleResizeX(horizontalSide, draggedDist)
                handleResizeY(verticalSide, draggedDist)

                onResize()
            } else if (draggableHeight == height || relativeClickPos.y <= draggableHeight) {
                posX = (preDragPos.x + draggedDist.x).coerceIn(1.0f, mc.displayWidth - width - 1.0f)
                posY = (preDragPos.y + draggedDist.y).coerceIn(1.0f, mc.displayHeight - height - 1.0f)

                onReposition()
            } else {
                // TODO
            }
        }
    }

    private fun handleResizeX(horizontalSide: HAlign, draggedDist: Vec2f) {
        when (horizontalSide) {
            HAlign.LEFT -> {
                val draggedX = max(draggedDist.x,1.0f - preDragPos.x)
                var newWidth = max(preDragSize.x - draggedX, minWidth)

                if (maxWidth != -1.0f) newWidth = min(newWidth, maxWidth)
                newWidth = min(newWidth, scaledDisplayWidth - 2.0f)

                val prevWidth = width
                width = newWidth
                posX += prevWidth - newWidth
            }
            HAlign.RIGHT -> {
                val draggedX = min(draggedDist.x, preDragPos.x + preDragSize.x - 1.0f)
                var newWidth = max(preDragSize.x + draggedX, minWidth)

                if (maxWidth != -1.0f) newWidth = min(newWidth, maxWidth)
                newWidth = min(newWidth, scaledDisplayWidth - posX - 2.0f)

                width = newWidth
            }
            else -> {
                // Nothing lol
            }
        }
    }

    private fun handleResizeY(verticalSide: VAlign, draggedDist: Vec2f) {
        when (verticalSide) {
            VAlign.TOP -> {
                val draggedY = max(draggedDist.y,1.0f - preDragPos.y)
                var newHeight = max(preDragSize.y - draggedY, minHeight)

                if (maxHeight != -1.0f) newHeight = min(newHeight, maxHeight)
                newHeight = min(newHeight, scaledDisplayHeight - 2.0f)

                val prevHeight = height
                height = newHeight
                posY += prevHeight - newHeight
            }
            VAlign.BOTTOM -> {
                val draggedY = min(draggedDist.y, preDragPos.y + preDragSize.y - 1.0f)
                var newHeight = max(preDragSize.y + draggedY, minHeight)

                if (maxHeight != -1.0f) newHeight = min(newHeight, maxHeight)
                newHeight = min(newHeight, scaledDisplayHeight - posY - 2.0f)

                height = newHeight
            }
            else -> {
                // Nothing lol
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