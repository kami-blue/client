package me.zeroeightsix.kami.gui

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.gui.rgui.WindowComponent
import me.zeroeightsix.kami.gui.rgui.windows.ColorPicker
import me.zeroeightsix.kami.gui.rgui.windows.SettingWindow
import me.zeroeightsix.kami.module.modules.client.ClickGUI
import me.zeroeightsix.kami.module.modules.client.GuiColors
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.graphics.*
import me.zeroeightsix.kami.util.graphics.font.FontRenderAdapter
import me.zeroeightsix.kami.util.math.Vec2d
import me.zeroeightsix.kami.util.math.Vec2f
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11.*
import java.util.*
import kotlin.collections.HashMap

abstract class AbstractKamiGui<S: SettingWindow<*>, E: Any> : GuiScreen() {

    // Window
    val windowList = LinkedHashSet<WindowComponent>()
    protected var lastClickedWindow: WindowComponent? = null
    protected var hoveredWindow: WindowComponent? = null
        set(value) {
            if (value == field) return
            field?.onLeave(getRealMousePos())
            value?.onHover(getRealMousePos())
            field = value
        }
    protected val settingMap = HashMap<E, S>()
    protected var settingWindow: S? = null

    // Mouse
    protected var lastEventButton = -1
    protected var lastClickPos = Vec2f(0.0f, 0.0f)

    // Searching
    protected var typedString = ""
    protected var lastTypedTime = 0L
    protected var prevStringWidth = 0.0f
    protected var stringWidth = 0.0f
        set(value) {
            prevStringWidth = renderStringPosX
            field = value
        }
    protected val renderStringPosX
        get() = AnimationUtils.exponent(AnimationUtils.toDeltaTimeFloat(lastTypedTime), 250.0f, prevStringWidth, stringWidth)

    // Shader
    private val blurShader = ShaderHelper(ResourceLocation("shaders/post/kawase_blur_6.json"), "final")


    init {
        mc = Wrapper.minecraft
        windowList.add(ColorPicker)

        listener<SafeTickEvent> { event ->
            if (event.phase != TickEvent.Phase.START) return@listener
            for (window in windowList) window.onTick()

            blurShader.shader?.let {
                val multiplier = ClickGUI.blur.value / 1.0f
                for (shader in it.listShaders) {
                    shader.shaderManager.getShaderUniform("multiplier")?.set(multiplier)
                }
            }
        }
    }

    fun displaySettingWindow(element: E) {
        val mousePos = getRealMousePos()
        settingMap.getOrPut(element) {
            newSettingWindow(element, mousePos)
        }.apply {
            posX = mousePos.x
            posY = mousePos.y
        }.also {
            lastClickedWindow = it
            settingWindow = it
            windowList.add(it)
            it.onGuiInit()
            it.onDisplayed()
        }
    }

    abstract fun newSettingWindow(element: E, mousePos: Vec2f): S

    // Gui init
    fun onDisplayed() {
        for (window in windowList) window.onDisplayed()
    }

    override fun initGui() {
        super.initGui()
        val scaledResolution = ScaledResolution(mc)
        width = scaledResolution.scaledWidth
        height = scaledResolution.scaledHeight
        for (window in windowList) window.onGuiInit()
    }

    override fun onGuiClosed() {
        lastClickedWindow = null
        hoveredWindow = null
        typedString = ""
        lastTypedTime = 0L
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
    // End of keyboard input

    // Rendering
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val vertexHelper = VertexHelper(GlStateUtils.useVbo())
        drawBackground(vertexHelper, partialTicks)
        drawWindows(vertexHelper)
        GlStateUtils.rescaleMc()
        drawTypedString()
    }

    private fun drawBackground(vertexHelper: VertexHelper, partialTicks: Float) {
        // Blur effect
        if (ClickGUI.blur.value > 0.0f) {
            glPushMatrix()
            blurShader.shader?.render(partialTicks)
            mc.getFramebuffer().bindFramebuffer(true)
            blurShader.getFrameBuffer("final")?.framebufferRenderExt(mc.displayWidth, mc.displayHeight, false)
            glPopMatrix()
        }

        // Darkened background
        if (ClickGUI.darkness.value > 0.0f) {
            GlStateUtils.rescaleActual()
            val color = ColorHolder(0, 0, 0, (ClickGUI.darkness.value * 255.0f).toInt())
            RenderUtils2D.drawRectFilled(vertexHelper, posEnd = Vec2d(mc.displayWidth.toDouble(), mc.displayHeight.toDouble()), color = color)
        }
    }

    private fun drawWindows(vertexHelper: VertexHelper) {
        GlStateUtils.rescaleKami()
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.depthMask(true)
        GlStateManager.depthFunc(GL_LEQUAL)
        GlStateUtils.depth(true)

        for (window in windowList) {
            if (!window.visible.value) continue
            glPushMatrix()
            glTranslatef(window.renderPosX, window.renderPosY, 0.0f)
            window.onRender(vertexHelper, Vec2f(window.renderPosX, window.renderPosY))
            glPopMatrix()
        }

        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        GlStateUtils.depth(false)
    }

    private fun drawTypedString() {
        if (typedString.isNotBlank() && System.currentTimeMillis() - lastTypedTime <= 5000L) {
            val scaledResolution = ScaledResolution(mc)
            val posX = scaledResolution.scaledWidth / 2.0f - renderStringPosX / 2.0f
            val posY = scaledResolution.scaledHeight / 2.0f - FontRenderAdapter.getFontHeight(3.0f) / 2.0f
            val color = GuiColors.text
            color.a = AnimationUtils.halfSineDec(AnimationUtils.toDeltaTimeFloat(lastTypedTime), 5000.0f, 0.0f, 255.0f).toInt()
            FontRenderAdapter.drawString(typedString, posX, posY, color = color, scale = 1.666f)
        }
    }
    // End of rendering


    companion object {

        fun getRealMousePos(): Vec2f {
            val scaleFactor = ClickGUI.getScaleFactorFloat()
            return Vec2f((Mouse.getX() / scaleFactor), (Wrapper.minecraft.displayHeight / scaleFactor - Mouse.getY() / scaleFactor - 1.0f))
        }

    }
}