package me.zeroeightsix.kami.gui.hudgui

import me.zeroeightsix.kami.event.KamiEventBus
import me.zeroeightsix.kami.gui.rgui.windows.BasicWindow
import me.zeroeightsix.kami.setting.GuiConfig
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.math.Vec2f
import org.kamiblue.commons.utils.DisplayEnum

open class HudElement(
    name: String,
    val alias: Array<String> = emptyArray(),
    val category: Category,
    val description: String,
    val alwaysListening: Boolean = false,
    val enabledByDefault: Boolean = false
) : BasicWindow(name, 20.0f, 20.0f, 100.0f, 50.0f, SettingGroup.HUD_GUI) {

    val settingList get() = GuiConfig.getGroupOrPut("HudGui").getGroupOrPut(originalName).getSettings()

    override fun onGuiInit() {
        super.onGuiInit()
        if (alwaysListening || visible.value) KamiEventBus.subscribe(this)
    }

    override fun onClosed() {
        super.onClosed()
        if (alwaysListening || visible.value) KamiEventBus.subscribe(this)
    }

    final override fun onTick() {
        super.onTick()
    }

    final override fun onRender(vertexHelper: VertexHelper, absolutePos: Vec2f) {
        super.onRender(vertexHelper, absolutePos)
        renderHud(vertexHelper)
    }

    open fun renderHud(vertexHelper: VertexHelper) {}

    init {
        visible.valueListeners.add { _, it ->
            if (it) KamiEventBus.subscribe(this)
            else if (!alwaysListening) KamiEventBus.unsubscribe(this)
        }

        if (!enabledByDefault) visible.value = false
    }

    enum class Category(override val displayName: String) : DisplayEnum {
        CLIENT("Client"),
        COMBAT("Combat"),
        PLAYER("Player"),
        WORLD("World"),
        MISC("Misc")
    }

}