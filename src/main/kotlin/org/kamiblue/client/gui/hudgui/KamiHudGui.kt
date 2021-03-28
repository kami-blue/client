package org.kamiblue.client.gui.hudgui

import net.minecraftforge.fml.common.gameevent.InputEvent
import org.kamiblue.client.event.events.RenderOverlayEvent
import org.kamiblue.client.gui.AbstractKamiGui
import org.kamiblue.client.gui.clickgui.KamiClickGui
import org.kamiblue.client.gui.hudgui.component.HudButton
import org.kamiblue.client.gui.hudgui.elements.client.WaterMark
import org.kamiblue.client.gui.hudgui.window.HudSettingWindow
import org.kamiblue.client.gui.rgui.Component
import org.kamiblue.client.gui.rgui.windows.ListWindow
import org.kamiblue.client.module.modules.client.ClickGUI
import org.kamiblue.client.module.modules.client.Hud
import org.kamiblue.client.module.modules.client.HudEditor
import org.kamiblue.client.util.graphics.GlStateUtils
import org.kamiblue.client.util.graphics.VertexHelper
import org.kamiblue.client.util.math.Vec2f
import org.kamiblue.event.listener.listener
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11.*
import java.util.*
import kotlin.collections.LinkedHashSet

object KamiHudGui : AbstractKamiGui<HudSettingWindow, AbstractHudElement>() {

    override val alwaysTicking = true
    private val hudWindows = EnumMap<AbstractHudElement.Category, ListWindow>(AbstractHudElement.Category::class.java)

    init {
        var posX = 0.0f
        var posY = 0.0f
        val screenWidth = KamiClickGui.mc.displayWidth / ClickGUI.getScaleFactorFloat()

        for (category in AbstractHudElement.Category.values()) {
            val window = ListWindow(category.displayName, posX, 0.0f, 90.0f, 300.0f, Component.SettingGroup.HUD_GUI)
            windowList.add(window)
            hudWindows[category] = window

            posX += 90.0f

            if (posX > screenWidth) {
                posX = 0.0f
                posY += 100.0f
            }
        }

        listener<InputEvent.KeyInputEvent> {
            val eventKey = Keyboard.getEventKey()

            if (eventKey == Keyboard.KEY_NONE || Keyboard.isKeyDown(Keyboard.KEY_F3)) return@listener

            for (child in windowList) {
                if (child !is AbstractHudElement) continue
                if (!child.bind.isDown(eventKey)) continue
                child.visible = !child.visible
            }
        }
    }

    internal fun register(hudElement: AbstractHudElement) {
        val button = HudButton(hudElement)
        hudWindows[hudElement.category]!!.children.add(button)
        windowList.add(hudElement)
    }

    internal fun unregister(hudElement: AbstractHudElement) {
        hudWindows[hudElement.category]!!.children.removeIf { it is HudButton && it.hudElement == hudElement }
        windowList.remove(hudElement)
    }

    override fun onGuiClosed() {
        super.onGuiClosed()
        setHudButtonVisibility { true }
    }

    override fun newSettingWindow(element: AbstractHudElement, mousePos: Vec2f): HudSettingWindow {
        return HudSettingWindow(element, mousePos.x, mousePos.y)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_ESCAPE || HudEditor.bind.value.isDown(keyCode) && !searching && settingWindow?.listeningChild == null) {
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
                    if (window !is AbstractHudElement || !window.visible) continue
                    renderHudElement(vertexHelper, window)
                }
            } else if (WaterMark.visible) {
                renderHudElement(vertexHelper, WaterMark)
            }

            GlStateUtils.rescaleMc()
            GlStateUtils.depth(true)
        }
    }

    private fun renderHudElement(vertexHelper: VertexHelper, window: AbstractHudElement) {
        glPushMatrix()
        glTranslatef(window.renderPosX, window.renderPosY, 0.0f)

        if (Hud.hudFrame) window.renderFrame(vertexHelper)

        glScalef(window.scale, window.scale, window.scale)
        window.renderHud(vertexHelper)

        glPopMatrix()
    }

}