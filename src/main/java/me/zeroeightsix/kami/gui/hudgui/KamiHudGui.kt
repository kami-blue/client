package me.zeroeightsix.kami.gui.hudgui

import me.zeroeightsix.kami.event.events.RenderOverlayEvent
import me.zeroeightsix.kami.gui.AbstractKamiGui
import me.zeroeightsix.kami.gui.hudgui.component.HudButton
import me.zeroeightsix.kami.gui.hudgui.elements.client.WaterMark
import me.zeroeightsix.kami.gui.hudgui.window.HudSettingWindow
import me.zeroeightsix.kami.gui.rgui.Component
import me.zeroeightsix.kami.gui.rgui.windows.ListWindow
import me.zeroeightsix.kami.module.modules.client.Hud
import me.zeroeightsix.kami.module.modules.client.HudEditor
import me.zeroeightsix.kami.util.graphics.GlStateUtils
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.math.Vec2f
import org.kamiblue.event.listener.listener
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11.*
import java.util.*

object KamiHudGui : AbstractKamiGui<HudSettingWindow, HudElement>() {

    override val alwaysTicking = true
    private val hudWindows = EnumMap<HudElement.Category, ListWindow>(HudElement.Category::class.java)

    init {
        var posX = 10.0f

        for (category in HudElement.Category.values()) {
            val window = ListWindow(category.displayName, posX, 10.0f, 100.0f, 256.0f, Component.SettingGroup.HUD_GUI)
            windowList.add(window)
            hudWindows[category] = window
            posX += 110.0f
        }
    }

    internal fun register(hudElement: HudElement) {
        hudWindows[hudElement.category]!!.children.add(hudElement)
        windowList.add(hudElement)
    }

    internal fun unregister(hudElement: HudElement) {
        hudWindows[hudElement.category]!!.children.remove(hudElement)
        windowList.remove(hudElement)
    }

    override fun onGuiClosed() {
        super.onGuiClosed()
        setHudButtonVisibility { true }
    }

    override fun newSettingWindow(element: HudElement, mousePos: Vec2f): HudSettingWindow {
        return HudSettingWindow(element, mousePos.x, mousePos.y)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_ESCAPE || HudEditor.bind.value.isDown(keyCode) && !searching) {
            HudEditor.disable()
        } else {
            super.keyTyped(typedChar, keyCode)

            val string = typedString.replace(" ", "")

            if (string.isNotEmpty()) {
                setHudButtonVisibility { hudButton ->
                    hudButton.hudElement.componentName.contains(string, true)
                        || hudButton.hudElement.alias.any { it.contains(string, true) }
                }
            } else {
                setHudButtonVisibility { true }
            }
        }
    }

    private fun setHudButtonVisibility(function: (HudButton) -> Boolean) {
        windowList.filterIsInstance<ListWindow>().forEach {
            for (child in it.children) {
                if (child !is HudButton) continue
                child.visible = function(child)
            }
        }
    }

    init {
        listener<RenderOverlayEvent>(0) {
            if (mc?.world == null || mc?.player == null || mc?.currentScreen == this || mc?.gameSettings?.showDebugInfo != false) return@listener

            val vertexHelper = VertexHelper(GlStateUtils.useVbo())
            GlStateUtils.rescaleKami()

            if (Hud.isEnabled) {
                for (window in windowList) {
                    if (window !is HudElement || !window.visible) continue
                    renderHudElement(vertexHelper, window)
                }
            } else if (WaterMark.visible) {
                renderHudElement(vertexHelper, WaterMark)
            }

            GlStateUtils.rescaleMc()
            GlStateUtils.depth(true)
        }
    }

    private fun renderHudElement(vertexHelper: VertexHelper, window: HudElement) {
        glPushMatrix()
        glTranslatef(window.renderPosX, window.renderPosY, 0.0f)

        if (Hud.hudFrame) window.renderFrame(vertexHelper)

        glScalef(window.scale, window.scale, window.scale)
        window.renderHud(vertexHelper)

        glPopMatrix()
    }

}