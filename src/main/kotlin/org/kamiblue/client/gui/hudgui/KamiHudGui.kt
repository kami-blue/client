package org.kamiblue.client.gui.hudgui

import org.kamiblue.client.event.events.RenderOverlayEvent
import org.kamiblue.client.gui.AbstractKamiGui
import org.kamiblue.client.gui.GuiManager
import org.kamiblue.client.gui.hudgui.component.HudButton
import org.kamiblue.client.gui.hudgui.elements.client.WaterMark
import org.kamiblue.client.gui.hudgui.window.HudSettingWindow
import org.kamiblue.client.gui.rgui.Component
import org.kamiblue.client.gui.rgui.windows.ListWindow
import org.kamiblue.client.module.modules.client.Hud
import org.kamiblue.client.module.modules.client.HudEditor
import org.kamiblue.client.util.graphics.GlStateUtils
import org.kamiblue.client.util.graphics.VertexHelper
import org.kamiblue.client.util.math.Vec2f
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
                    hudButton.hudElement.name.contains(string, true)
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