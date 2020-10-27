package me.zeroeightsix.kami.gui.clickgui

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.gui.rgui.WindowComponent
import me.zeroeightsix.kami.gui.rgui.component.ModuleButton
import me.zeroeightsix.kami.gui.rgui.windows.ListWindow
import me.zeroeightsix.kami.gui.rgui.windows.SettingWindow
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.module.modules.client.ClickGUI
import me.zeroeightsix.kami.module.modules.client.GuiColors
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

    private var typedString = ""
    private var lastTypedTime = 0L
    private var prevStringWidth = 0.0f
    private var stringWidth = 0.0f
        set(value) {
            prevStringWidth = renderStringPosX
            field = value
        }
    private val renderStringPosX
        get() = AnimationUtils.exponent(AnimationUtils.toDeltaTimeFloat(lastTypedTime), 250.0f, prevStringWidth, stringWidth)

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
        typedString = ""
        lastTypedTime = 0L
        setModuleVisibility{ true }
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

    override fun handleKeyboardInput() {
        super.handleKeyboardInput()
        val keyCode = Keyboard.getEventKey()
        val keyState = Keyboard.getEventKeyState()

        hoveredWindow?.onKeyInput(keyCode, keyState)

        if (keyCode == Keyboard.KEY_BACK || keyCode == Keyboard.KEY_DELETE) {
            typedString = ""
            lastTypedTime = 0L
            stringWidth = 0.0f
            prevStringWidth = 0.0f

            setModuleVisibility { true }
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_ESCAPE || ClickGUI.bind.value.isDown(keyCode)) {
            ClickGUI.disable()
        } else {
            when {
                typedChar.isLetter() || typedChar == ' ' -> {
                    typedString += typedChar
                    stringWidth = FontRenderAdapter.getStringWidth(typedString, 2.0f)
                    lastTypedTime = System.currentTimeMillis()

                    setModuleVisibility{ it.name.value.contains(typedString, true) }
                }
            }
        }
    }

    private fun setModuleVisibility(function: (ModuleButton) -> Boolean) {
        windowList.filterIsInstance<ListWindow>().forEach {
            for (child in it.children) {
                if (child !is ModuleButton) continue
                child.visible.value = function(child)
            }
        }
    }

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
            val posY = scaledResolution.scaledHeight / 2.0f - FontRenderAdapter.getFontHeight(2.0f) / 2.0f
            val color = GuiColors.text
            color.a = AnimationUtils.halfSineDec(AnimationUtils.toDeltaTimeFloat(lastTypedTime), 5000.0f, 0.0f, 255.0f).toInt()
            FontRenderAdapter.drawString(typedString, posX, posY, color = color, scale = 2.0f)
        }
    }

    fun getRealMousePos(): Vec2f {
        val scaleFactor = ClickGUI.getScaleFactorFloat()
        return Vec2f((Mouse.getX() / scaleFactor), (mc.displayHeight / scaleFactor - Mouse.getY() / scaleFactor - 1.0f))
    }
}