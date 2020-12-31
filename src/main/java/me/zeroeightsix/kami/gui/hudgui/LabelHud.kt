package me.zeroeightsix.kami.gui.hudgui

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.graphics.font.FontRenderAdapter
import me.zeroeightsix.kami.util.graphics.font.TextComponent
import me.zeroeightsix.kami.util.math.Vec2d
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.asyncListener

abstract class LabelHud(
    name: String,
    alias: Array<String> = emptyArray(),
    category: Category,
    description: String,
    alwaysListening: Boolean = false,
    enabledByDefault: Boolean = false
) : HudElement(name, alias, category, description, alwaysListening, enabledByDefault) {

    override val minWidth: Float get() = FontRenderAdapter.getFontHeight()
    override val minHeight: Float get() = FontRenderAdapter.getFontHeight()
    override val maxWidth: Float get() = displayText.getWidth() + 2.0f
    override val maxHeight: Float get() = displayText.getHeight(2)

    protected val displayText = TextComponent()

    init {
        asyncListener<SafeTickEvent> {
            if (it.phase != TickEvent.Phase.END) return@asyncListener
            displayText.clear()
            updateText()
        }
    }

    abstract fun updateText()

    override fun renderHud(vertexHelper: VertexHelper) {
        super.renderHud(vertexHelper)
        displayText.draw(
            Vec2d((width * dockingH.multiplier).toDouble(), (height * dockingV.multiplier).toDouble()),
            horizontalAlign = dockingH,
            verticalAlign = dockingV
        )
    }

}