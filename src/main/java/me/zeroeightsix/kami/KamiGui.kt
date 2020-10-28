package me.zeroeightsix.kami

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.gui.rgui.WindowComponent
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
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11.*

open class KamiGui<T : WindowComponent> : GuiScreen() {

    // Window
    val windowList = LinkedHashSet<T>()
    protected var lastClickedWindow: T? = null
    protected var hoveredWindow: T? = null
        set(value) {
            if (value == field) return
            field?.onLeave(getRealMousePos())
            value?.onHover(getRealMousePos())
            field = value
        }

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
        listener<SafeTickEvent> { event ->
            if (event.phase != TickEvent.Phase.END) return@listener
            for (window in windowList) {
                window.onTick()
            }

            blurShader.shader?.let {
                val multiplier = ClickGUI.blur.value / 1.0f
                for (shader in it.listShaders) {
                    shader.shaderManager.getShaderUniform("multiplier")?.set(multiplier)
                }
            }
        }
    }

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
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        with(topWindow) {
            this?.onClick(lastClickPos, mouseButton)
            lastClickedWindow = this
        }
        updateWindowOrder()
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        val mousePos = getRealMousePos()
        topWindow?.onRelease(mousePos, state)
        updateWindowOrder()
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        val mousePos = getRealMousePos()
        topWindow?.onDrag(mousePos, lastClickPos, clickedMouseButton)
    }

    private fun updateWindowOrder() {
        val cacheList = windowList.sortedBy { it.lastActiveTime }
        windowList.clear()
        windowList.addAll(cacheList)
    }

    private val topWindow get() = windowList.lastOrNull { it.isInWindow(lastClickPos) }
    // End of mouse input


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
            val posY = scaledResolution.scaledHeight / 2.0f - FontRenderAdapter.getFontHeight(1.666f) / 2.0f
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