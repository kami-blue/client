package org.kamiblue.client.gui

import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.events.RenderOverlayEvent
import org.kamiblue.client.gui.rgui.WindowComponent
import org.kamiblue.client.gui.rgui.windows.ColorPicker
import org.kamiblue.client.gui.rgui.windows.SettingWindow
import org.kamiblue.client.mixin.extension.listShaders
import org.kamiblue.client.module.modules.client.ClickGUI
import org.kamiblue.client.module.modules.client.GuiColors
import org.kamiblue.client.util.TimedFlag
import org.kamiblue.client.util.Wrapper
import org.kamiblue.client.util.color.ColorHolder
import org.kamiblue.client.util.graphics.*
import org.kamiblue.client.util.graphics.font.FontRenderAdapter
import org.kamiblue.client.util.math.Vec2d
import org.kamiblue.client.util.math.Vec2f
import org.kamiblue.client.util.threads.safeListener
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11.*
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.min

abstract class AbstractKamiGui<S : SettingWindow<*>, E : Any> : GuiScreen() {

    open val alwaysTicking = false

    // Window
    val windowList = LinkedHashSet<WindowComponent>()
    private var lastClickedWindow: WindowComponent? = null
    private var hoveredWindow: WindowComponent? = null
        set(value) {
            if (value == field) return
            field?.onLeave(getRealMousePos())
            value?.onHover(getRealMousePos())
            field = value
        }
    private val settingMap = HashMap<E, S>()
    protected var settingWindow: S? = null

    // Mouse
    private var lastEventButton = -1
    private var lastClickPos = Vec2f(0.0f, 0.0f)

    // Searching
    protected var typedString = ""
    protected var lastTypedTime = 0L
    protected var prevStringWidth = 0.0f
    protected var stringWidth = 0.0f
        set(value) {
            prevStringWidth = renderStringPosX
            field = value
        }
    private val renderStringPosX
        get() = AnimationUtils.exponent(AnimationUtils.toDeltaTimeFloat(lastTypedTime), 250.0f, prevStringWidth, stringWidth)
    val searching
        get() = typedString.isNotEmpty()

    // Shader
    private val blurShader = ShaderHelper(ResourceLocation("shaders/post/kawase_blur_6.json"), "final")

    // Animations
    private var displayed = TimedFlag(false)
    private val fadeMultiplier
        get() = if (displayed.value) {
            if (ClickGUI.fadeInTime > 0.0f) {
                AnimationUtils.exponentInc(AnimationUtils.toDeltaTimeFloat(displayed.lastUpdateTime), ClickGUI.fadeInTime * 1000.0f)
            } else {
                1.0f
            }
        } else {
            if (ClickGUI.fadeOutTime > 0.0f) {
                AnimationUtils.exponentDec(AnimationUtils.toDeltaTimeFloat(displayed.lastUpdateTime), ClickGUI.fadeOutTime * 1000.0f)
            } else {
                0.0f
            }
        }


    init {
        mc = Wrapper.minecraft
        windowList.add(ColorPicker)

        safeListener<TickEvent.ClientTickEvent> { event ->
            if (event.phase != TickEvent.Phase.START) return@safeListener

            blurShader.shader?.let {
                val multiplier = ClickGUI.blur * fadeMultiplier
                for (shader in it.listShaders) {
                    shader.shaderManager.getShaderUniform("multiplier")?.set(multiplier)
                }
            }

            if (displayed.value || alwaysTicking) {
                for (window in windowList) window.onTick()
            }
        }

        safeListener<RenderOverlayEvent>(-69420) {
            if (!displayed.value && fadeMultiplier > 0.0f) {
                drawScreen(0, 0, mc.renderPartialTicks)
            }
        }
    }

    fun displaySettingWindow(element: E) {
        val mousePos = getRealMousePos()

        settingMap.getOrPut(element) {
            newSettingWindow(element, mousePos)
        }.apply {
            lastClickedWindow = this
            settingWindow = this
            windowList.add(this)

            val screenWidth = mc.displayWidth / ClickGUI.getScaleFactorFloat()
            val screenHeight = mc.displayHeight / ClickGUI.getScaleFactorFloat()

            posX = if (mousePos.x + this.width <= screenWidth) {
                mousePos.x
            } else {
                mousePos.x - this.width
            }

            posY = min(mousePos.y, screenHeight - this.height)

            onGuiInit()
            onDisplayed()
        }
    }

    abstract fun newSettingWindow(element: E, mousePos: Vec2f): S

    // Gui init
    open fun onDisplayed() {
        lastClickedWindow = null
        lastEventButton = -1

        displayed.value = true

        for (window in windowList) window.onDisplayed()
    }

    override fun initGui() {
        super.initGui()

        val scaledResolution = ScaledResolution(mc)
        width = scaledResolution.scaledWidth + 16
        height = scaledResolution.scaledHeight + 16

        for (window in windowList) window.onGuiInit()
    }

    override fun onGuiClosed() {
        lastClickedWindow = null
        hoveredWindow = null

        typedString = ""
        lastTypedTime = 0L

        displayed.value = false

        for (window in windowList) window.onClosed()
        updateSettingWindow()
    }
    // End of gui init


    // Mouse input
    override fun handleMouseInput() {
        val mousePos = getRealMousePos()
        val eventButton = Mouse.getEventButton()

        when {
            // Click
            Mouse.getEventButtonState() -> {
                lastClickPos = mousePos
                lastEventButton = eventButton
            }
            // Release
            eventButton != -1 -> {
                lastEventButton = -1
            }
            // Drag
            lastEventButton != -1 -> {

            }
            // Move
            else -> {
                hoveredWindow = windowList.lastOrNull { it.isInWindow(mousePos) }
            }
        }

        hoveredWindow?.onMouseInput(mousePos)
        super.handleMouseInput()
        updateSettingWindow()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        with(hoveredWindow) {
            this?.onClick(lastClickPos, mouseButton)
            lastClickedWindow = this
        }
        updateWindowOrder()
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        val mousePos = getRealMousePos()
        hoveredWindow?.onRelease(mousePos, state)
        updateWindowOrder()
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        val mousePos = getRealMousePos()
        hoveredWindow?.onDrag(mousePos, lastClickPos, clickedMouseButton)
    }

    private fun updateSettingWindow() {
        settingWindow?.let {
            if (lastClickedWindow != it && lastClickedWindow != ColorPicker) {
                it.onClosed()
                windowList.remove(it)
                settingWindow = null
            }
        }
    }

    private fun updateWindowOrder() {
        val cacheList = windowList.sortedBy { it.lastActiveTime }
        windowList.clear()
        windowList.addAll(cacheList)
    }
    // End of mouse input

    // Keyboard input
    override fun handleKeyboardInput() {
        super.handleKeyboardInput()
        val keyCode = Keyboard.getEventKey()
        val keyState = Keyboard.getEventKeyState()

        hoveredWindow?.onKeyInput(keyCode, keyState)
        if (settingWindow != hoveredWindow) settingWindow?.onKeyInput(keyCode, keyState)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (settingWindow?.listeningChild != null) return
        when {
            keyCode == Keyboard.KEY_BACK || keyCode == Keyboard.KEY_DELETE -> {
                typedString = ""
                lastTypedTime = 0L
                stringWidth = 0.0f
                prevStringWidth = 0.0f
            }
            typedChar.isLetter() || typedChar == ' ' -> {
                typedString += typedChar
                stringWidth = FontRenderAdapter.getStringWidth(typedString, 2.0f)
                lastTypedTime = System.currentTimeMillis()
            }
        }
    }
    // End of keyboard input

    // Rendering
    final override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val scale = ClickGUI.getScaleFactorFloat()
        val scaledResolution = ScaledResolution(mc)
        val multiplier = fadeMultiplier
        val vertexHelper = VertexHelper(GlStateUtils.useVbo())

        drawBackground(vertexHelper, partialTicks)

        GlStateUtils.rescaleKami()
        glTranslatef(0.0f, -(mc.displayHeight / scale * (1.0f - multiplier)), 0.0f)
        drawWindows(vertexHelper)

        GlStateUtils.rescaleMc()
        glTranslatef(0.0f, -(scaledResolution.scaledHeight * (1.0f - multiplier)), 0.0f)
        drawTypedString()

        GlStateUtils.depth(false)
    }

    private fun drawBackground(vertexHelper: VertexHelper, partialTicks: Float) {
        // Blur effect
        if (ClickGUI.blur > 0.0f) {
            glPushMatrix()
            blurShader.shader?.render(partialTicks)
            mc.framebuffer.bindFramebuffer(true)
            blurShader.getFrameBuffer("final")?.framebufferRenderExt(mc.displayWidth, mc.displayHeight, false)
            glPopMatrix()
        }

        // Darkened background
        if (ClickGUI.darkness > 0.0f) {
            GlStateUtils.rescaleActual()
            val color = ColorHolder(0, 0, 0, (ClickGUI.darkness * 255.0f * fadeMultiplier).toInt())
            RenderUtils2D.drawRectFilled(vertexHelper, posEnd = Vec2d(mc.displayWidth.toDouble(), mc.displayHeight.toDouble()), color = color)
        }
    }

    private fun drawWindows(vertexHelper: VertexHelper) {
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        drawEachWindow {
            it.onRender(vertexHelper, Vec2f(it.renderPosX, it.renderPosY))
        }

        drawEachWindow {
            it.onPostRender(vertexHelper, Vec2f(it.renderPosX, it.renderPosY))
        }

        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
    }

    private fun drawEachWindow(renderBlock: (WindowComponent) -> Unit) {
        for (window in windowList) {
            if (!window.visible) continue
            glPushMatrix()
            glTranslatef(window.renderPosX, window.renderPosY, 0.0f)
            renderBlock(window)
            glPopMatrix()
        }
    }

    private fun drawTypedString() {
        if (typedString.isNotBlank() && System.currentTimeMillis() - lastTypedTime <= 5000L) {
            val scaledResolution = ScaledResolution(mc)
            val posX = scaledResolution.scaledWidth / 2.0f - renderStringPosX / 2.0f
            val posY = scaledResolution.scaledHeight / 2.0f - FontRenderAdapter.getFontHeight(2.0f) / 2.0f
            val color = GuiColors.text
            color.a = AnimationUtils.halfSineDec(AnimationUtils.toDeltaTimeFloat(lastTypedTime), 5000.0f, 0.0f, 255.0f).toInt()
            FontRenderAdapter.drawString(typedString, posX, posY, color = color, scale = 2.0f)
        }
    }
    // End of rendering

    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    companion object {
        fun getRealMousePos(): Vec2f {
            val scaleFactor = ClickGUI.getScaleFactorFloat()
            return Vec2f(
                Mouse.getX() / scaleFactor - 1.0f,
                (Wrapper.minecraft.displayHeight - 1 - Mouse.getY()) / scaleFactor
            )
        }
    }
}