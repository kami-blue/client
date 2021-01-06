package me.zeroeightsix.kami.gui.hudgui

import me.zeroeightsix.kami.event.events.RenderOverlayEvent
import me.zeroeightsix.kami.gui.AbstractKamiGui
import me.zeroeightsix.kami.gui.GuiManager
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

object KamiHudGui : AbstractKamiGui<HudSettingWindow, HudElement>() {

    override val alwaysTicking = true

    init {
        val allButtons = GuiManager.hudElementsMap.values.map { HudButton(it) }
        var posX = 10.0f

        for (category in HudElement.Category.values()) {
            val buttons = allButtons.filter { it.hudElement.category == category }.toTypedArray()
            if (buttons.isNullOrEmpty()) continue
            windowList.add(ListWindow(category.displayName, posX, 10.0f, 100.0f, 256.0f, Component.SettingGroup.HUD_GUI, *buttons))
            posX += 110.0f
        }

        windowList.addAll(GuiManager.hudElementsMap.values)
    }

    override fun newSettingWindow(element: HudElement, mousePos: Vec2f): HudSettingWindow {
        return HudSettingWindow(element, mousePos.x, mousePos.y)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_ESCAPE || HudEditor.bind.value.isDown(keyCode)) {
            HudEditor.disable()
        } else {
            super.keyTyped(typedChar, keyCode)
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
        window.renderHud(vertexHelper)
        glPopMatrix()
    }

}