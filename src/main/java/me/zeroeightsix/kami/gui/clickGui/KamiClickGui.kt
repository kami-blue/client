package me.zeroeightsix.kami.gui.clickgui

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.gui.rgui.WindowComponent
import me.zeroeightsix.kami.gui.rgui.component.ModuleButton
import me.zeroeightsix.kami.gui.rgui.windows.ListWindow
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.module.modules.client.ClickGUI
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.graphics.GlStateUtils
import me.zeroeightsix.kami.util.graphics.RenderUtils2D
import me.zeroeightsix.kami.util.graphics.ShaderHelper
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.math.Vec2d
import me.zeroeightsix.kami.util.math.Vec2f
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11.*
import java.util.*

object KamiClickGui : GuiScreen() {
    private val windowList = LinkedList<WindowComponent>()

    private var lastClickPos = Vec2f(0.0f, 0.0f)
    private var hoveredWindow: WindowComponent? = null
        set(value) {
            if (value == field) return
            field?.onLeave(getRealMousePos())
            value?.onHover(getRealMousePos())
            field = value
        }
    private val blurShader = ShaderHelper(ResourceLocation("shaders/post/kawase_blur_6.json"), "final")

    init {
        val allButtons = ModuleManager.getModules().map { ModuleButton(it) }
        for ((index, category) in Module.Category.values().withIndex()) {
            val buttons = allButtons.filter { it.module.category == category }.toTypedArray()
            if (buttons.isNullOrEmpty()) continue
            windowList.add(ListWindow(category.categoryName, 90.0f * index, 64.0f, 80.0f, 256.0f, *buttons))
        }
    }

    init {
        listener<SafeTickEvent> { event ->
            if (event.phase != TickEvent.Phase.START) return@listener
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

    override fun initGui() {
        super.initGui()
        val scaledResolution = ScaledResolution(mc)
        width = scaledResolution.scaledWidth
        height = scaledResolution.scaledHeight
        for (window in windowList) window.onGuiInit()
    }

    override fun onGuiClosed() {
        hoveredWindow = null
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_ESCAPE || ClickGUI.bind.value.isDown(keyCode)) {
            ClickGUI.disable()
        }
    }

    override fun handleMouseInput() {
        val mousePos = getRealMousePos()
        if (Mouse.getEventButtonState()) lastClickPos = mousePos
        else hoveredWindow = topWindow
        hoveredWindow?.onMouseInput(mousePos)
        super.handleMouseInput()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        topWindow?.onClick(lastClickPos, mouseButton)
        windowList.sortBy { it.lastActiveTime }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        val mousePos = getRealMousePos()
        topWindow?.onRelease(mousePos, state)
        windowList.sortBy { it.lastActiveTime }
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        val mousePos = getRealMousePos()
        topWindow?.onDrag(mousePos, lastClickPos, clickedMouseButton)
    }

    private val topWindow get() = windowList.lastOrNull { it.isInWindow(lastClickPos) }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val vertexHelper = VertexHelper(GlStateUtils.useVbo())

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

        GlStateUtils.rescaleKami()
        for (window in windowList) {
            glPushMatrix()
            glTranslatef(window.renderPosX, window.renderPosY, 0.0f)
            window.onRender(vertexHelper)
            glPopMatrix()
        }
        GlStateUtils.rescaleMc()
    }

    fun getRealMousePos(): Vec2f {
        val scaleFactor = ClickGUI.getScaleFactorFloat()
        return Vec2f((Mouse.getX() / scaleFactor), (mc.displayHeight / scaleFactor - Mouse.getY() / scaleFactor - 1.0f))
    }
}