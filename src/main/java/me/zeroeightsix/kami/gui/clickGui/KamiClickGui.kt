package me.zeroeightsix.kami.gui.clickGui

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.gui.rgui.WindowComponent
import me.zeroeightsix.kami.gui.rgui.component.ModuleButton
import me.zeroeightsix.kami.gui.rgui.windows.BasicWindow
import me.zeroeightsix.kami.gui.rgui.windows.ListWindow
import me.zeroeightsix.kami.gui.rgui.windows.TitledWindow
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.module.modules.client.ClickGUI
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.graphics.GlStateUtils
import me.zeroeightsix.kami.util.graphics.ShaderHelper
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.math.Vec2d
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

    private var lastClickPos = Vec2d(0.0, 0.0)
    private var hoveredWindow: WindowComponent? = null
        set(value) {
            if (value == field) return
            field?.onLeave(getRealMousePos())
            value?.onHover(getRealMousePos())
            field = value
        }
    private val blurShader = ShaderHelper(ResourceLocation("shaders/post/blur.json"))

    init {
        windowList.add(TitledWindow("Test1", 128.0, 128.0, 256.0, 256.0))
        windowList.add(TitledWindow("Test2", 0.0, 0.0, 384.0, 384.0))
        windowList.add(TitledWindow("Test3", 256.0, 256.0, 384.0, 384.0))
        windowList.add(BasicWindow("Test4", 256.0, 256.0, 64.0, 32.0))
        windowList.add(BasicWindow("Test5", 256.0, 256.0, 128.0, 64.0))
        val movementModules = ModuleManager.getModules().filter { it.category == Module.Category.MOVEMENT }.map { ModuleButton(it) }.toTypedArray()
        println(windowList.add(ListWindow("Movement", 256.0, 256.0, 100.0, 512.0, *movementModules)))
    }

    init {
        listener<SafeTickEvent> { event ->
            if (event.phase != TickEvent.Phase.START) return@listener
            for (window in windowList) {
                window.onTick()
            }
            blurShader.shader?.let {
                for (shader in it.listShaders) {
                    shader.shaderManager.getShaderUniform("Radius")?.set(ClickGUI.backgroundBlur.value)
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
        println(windowList.size)
    }

    override fun onGuiClosed() {
        hoveredWindow = null
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_ESCAPE || ClickGUI.bind.value.isDown(keyCode)) {
            mc.displayGuiScreen(null)
            mc.setIngameFocus()
        }
    }

    override fun handleMouseInput() {
        val mousePos = getRealMousePos()
        if (Mouse.getEventButtonState()) lastClickPos = mousePos
        else hoveredWindow = getSortedWindowList().lastOrNull { isInWindow(it, mousePos) }
        hoveredWindow?.onMouseInput(mousePos)
        super.handleMouseInput()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        getSortedWindowList().lastOrNull { isInWindow(it, lastClickPos) }?.onClick(lastClickPos, mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        val mousePos = getRealMousePos()
        getSortedWindowList().lastOrNull { isInWindow(it, lastClickPos) }?.onRelease(mousePos, state)
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        val mousePos = getRealMousePos()
        getSortedWindowList().lastOrNull { isInWindow(it, lastClickPos) }?.onDrag(mousePos, lastClickPos, clickedMouseButton)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        blurShader.shader?.render(partialTicks)
        mc.getFramebuffer().bindFramebuffer(true)

        val vertexHelper = VertexHelper(GlStateUtils.useVbo())
        GlStateUtils.rescaleKami()
        GlStateUtils.depth(false)
        for (window in getSortedWindowList()) {
            glPushMatrix()
            glTranslated(window.renderPosX, window.renderPosY, 0.0)
            window.onRender(vertexHelper)
            GlStateUtils.blend(false)
            glPopMatrix()
        }
        GlStateUtils.depth(true)
        GlStateUtils.rescaleMc()
    }

    private fun getSortedWindowList() = windowList.sortedBy { it.lastActiveTime }

    fun getRealMousePos(): Vec2d {
        val scaleFactor = ClickGUI.getScaleFactor()
        return Vec2d((Mouse.getX() / scaleFactor), (mc.displayHeight / scaleFactor - Mouse.getY() / scaleFactor - 1.0))
    }

    private fun isInWindow(window: WindowComponent, mousePos: Vec2d): Boolean {
        return mousePos.x in window.preDragPos.x - 5.0..window.preDragPos.x + window.preDragSize.x + 5.0
                && mousePos.y in window.preDragPos.y - 5.0..window.preDragPos.y + window.preDragSize.y + 5.0
    }
}