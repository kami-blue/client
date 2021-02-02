package org.kamiblue.client.gui.clickgui

import org.kamiblue.client.gui.AbstractKamiGui
import org.kamiblue.client.gui.clickgui.component.ModuleButton
import org.kamiblue.client.gui.clickgui.window.ModuleSettingWindow
import org.kamiblue.client.gui.rgui.Component
import org.kamiblue.client.gui.rgui.windows.ListWindow
import org.kamiblue.client.module.AbstractModule
import org.kamiblue.client.module.ModuleManager
import org.kamiblue.client.module.modules.client.ClickGUI
import me.zeroeightsix.kami.util.math.Vec2f
import org.lwjgl.input.Keyboard

object KamiClickGui : AbstractKamiGui<ModuleSettingWindow, AbstractModule>() {

    private val moduleWindows = ArrayList<ListWindow>()

    init {
        val allButtons = ModuleManager.modules
            .groupBy { it.category.displayName }
            .mapValues { (_, modules) -> modules.map { ModuleButton(it) } }

        var posX = 10.0f

        for ((category, buttons) in allButtons) {
            val window = ListWindow(category, posX, 10.0f, 100.0f, 300.0f, Component.SettingGroup.CLICK_GUI)

            window.children.addAll(buttons)
            moduleWindows.add(window)

            posX += 110.0f
        }

        windowList.addAll(moduleWindows)
    }

    override fun onDisplayed() {
        val allButtons = ModuleManager.modules
            .groupBy { it.category.displayName }
            .mapValues { (_, modules) -> modules.map { ModuleButton(it) } }

        moduleWindows.forEach { window ->
            window.children.clear()
            allButtons[window.originalName]?.let { window.children.addAll(it) }
        }

        super.onDisplayed()
    }

    override fun onGuiClosed() {
        super.onGuiClosed()
        setModuleButtonVisibility { true }
    }

    override fun newSettingWindow(element: AbstractModule, mousePos: Vec2f): ModuleSettingWindow {
        return ModuleSettingWindow(element, mousePos.x, mousePos.y)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_ESCAPE || keyCode == ClickGUI.bind.value.key && !searching) {
            ClickGUI.disable()
        } else {
            super.keyTyped(typedChar, keyCode)

            val string = typedString.replace(" ", "")

            if (string.isNotEmpty()) {
                setModuleButtonVisibility { moduleButton ->
                    moduleButton.module.name.contains(string, true)
                        || moduleButton.module.alias.any { it.contains(string, true) }
                }
            } else {
                setModuleButtonVisibility { true }
            }
        }
    }

    private fun setModuleButtonVisibility(function: (ModuleButton) -> Boolean) {
        windowList.filterIsInstance<ListWindow>().forEach {
            for (child in it.children) {
                if (child !is ModuleButton) continue
                child.visible = function(child)
            }
        }
    }
}