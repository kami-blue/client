package me.zeroeightsix.kami.gui.hudgui

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.setting.GuiConfig.setting
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.graphics.font.FontRenderAdapter
import me.zeroeightsix.kami.util.graphics.font.TextComponent
import me.zeroeightsix.kami.util.math.Vec2d
import net.minecraftforge.fml.common.gameevent.TickEvent

abstract class LabelHud(
    name: String,
    alias: Array<String> = emptyArray(),
    category: Category,
    description: String,
    alwaysListening: Boolean = false,
    enabledByDefault: Boolean = false
) : HudElement(name, alias, category, description, alwaysListening, enabledByDefault) {

    override val resizable get() = false

    override val minWidth: Float get() = FontRenderAdapter.getFontHeight()
    override val minHeight: Float get() = FontRenderAdapter.getFontHeight()
    override val maxWidth: Float get() = displayText.getWidth() + 2.0f
    override val maxHeight: Float get() = displayText.getHeight(2)

    protected val primaryColor = setting("PrimaryColor", ColorHolder(255, 255, 255), false)
    protected val secondaryColor = setting("SecondaryColor", ColorHolder(155, 144, 255), false)

    protected val displayText = TextComponent()

    init {
        listener<SafeTickEvent> {
            if (it.phase != TickEvent.Phase.END || !visible.value) return@listener
            displayText.clear()
            updateText()

            width.value = maxWidth
            height.value = maxHeight
        }
    }

    abstract fun updateText()

    override fun renderHud(vertexHelper: VertexHelper) {
        super.renderHud(vertexHelper)
        displayText.draw(
            Vec2d((width.value * dockingH.value.multiplier).toDouble(), (height.value * dockingV.value.multiplier).toDouble()),
            horizontalAlign = dockingH.value,
            verticalAlign = dockingV.value
        )
    }

}