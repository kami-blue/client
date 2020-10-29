package me.zeroeightsix.kami.gui.hudgui

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.graphics.font.TextComponent
import me.zeroeightsix.kami.util.math.Vec2d
import net.minecraftforge.fml.common.gameevent.TickEvent

abstract class LabelHud(name: String) : HudElement(name) {

    override val resizable get() = false
    override val maxWidth: Float get() = displayText.getWidth() + 2.0f
    override val maxHeight: Float get() = displayText.getHeight(2)

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
        displayText.draw(Vec2d(1.0, 0.0))
    }

}