package me.zeroeightsix.kami.gui.clickgui

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.gui.rgui.WindowComponent
import me.zeroeightsix.kami.gui.rgui.component.ModuleButton
import me.zeroeightsix.kami.gui.rgui.windows.ListWindow
import me.zeroeightsix.kami.gui.rgui.windows.SettingWindow
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
import kotlin.collections.HashMap

object KamiClickGui : GuiScreen() {
    private val windowList = LinkedList<WindowComponent>()
    private val settingMap = HashMap<Module, SettingWindow>()

    private var lastEventButton = -1
    private var lastClickPos = Vec2f(0.0f, 0.0f)
    private var lastClickedWindow: WindowComponent? = null
    private var hoveredWindow: WindowComponent? = null
        set(value) {
            if (value == field) return
            field?.onLeave(getRealMousePos())
            value?.onHover(getRealMousePos())
            field = value
        }
    private var settingWindow: SettingWindow? = null
    private val blurShader = ShaderHelper(ResourceLocation("shaders/post/kawase_blur_6.json"), "final")

    init {
        val allButtons = ModuleManager.getModules().map { ModuleButton(it) }
        var posX = 10.0f

        for (category in Module.Category.values()) {
            val buttons = allButtons.filter { it.module.category == category }.toTypedArray()
            if (buttons.isNullOrEmpty()) continue
            windowList.add(ListWindow(category.categoryName, posX, 10.0f, 100.0f, 256.0f, true, *buttons))
            posX += 110.0f
        }

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

    fun displaySettingWindow(module: Module) {
        val mousePos = getRealMousePos()
        settingMap.getOrPut(module) {
            SettingWindow(module, mousePos.x, mousePos.y)
        }.apply {
            posX.value = mousePos.x
            posY.value = mousePos.y
        }.also {
            lastClickedWindow = it
            settingWindow = it
            windowList.add(it)
            it.onGuiInit()
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

        settingWindow?.let {
            if (lastClickedWindow != it) {
                windowList.remove(it)
                settingWindow = null
            }
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        with(topWindow) {
            this?.onClick(lastClickPos, mouseButton)
            lastClickedWindow = this
        }
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
            window.onRender(vertexHelper, Vec2f(window.renderPosX, window.renderPosY))
            glPopMatrix()
        }
        GlStateUtils.rescaleMc()
    }

    fun getRealMousePos(): Vec2f {
        val scaleFactor = ClickGUI.getScaleFactorFloat()
        return Vec2f((Mouse.getX() / scaleFactor), (mc.displayHeight / scaleFactor - Mouse.getY() / scaleFactor - 1.0f))
    }
}