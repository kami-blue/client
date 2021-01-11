package me.zeroeightsix.kami.gui.hudgui

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.graphics.font.TextComponent
import me.zeroeightsix.kami.util.math.Vec2d
import me.zeroeightsix.kami.util.threads.safeAsyncListener
import me.zeroeightsix.kami.util.translation.TranslationKey
import net.minecraftforge.fml.common.gameevent.TickEvent

abstract class LabelHud(
    alias: Array<String> = emptyArray(),
    category: Category,
    alwaysListening: Boolean = false,
    enabledByDefault: Boolean = false
) : HudElement( alias, category, alwaysListening, enabledByDefault) {

    override val hudWidth: Float get() = displayText.getWidth() + 2.0f
    override val hudHeight: Float get() = displayText.getHeight(2)

    protected val displayText = TextComponent()

    init {
        safeAsyncListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.END) return@safeAsyncListener
            displayText.clear()
            updateText()
        }
    }

    abstract fun SafeClientEvent.updateText()

    override fun renderHud(vertexHelper: VertexHelper) {
        super.renderHud(vertexHelper)
        displayText.draw(
            Vec2d((width * dockingH.multiplier).toDouble(), (height * dockingV.multiplier).toDouble()),
            horizontalAlign = dockingH,
            verticalAlign = dockingV
        )
    }

}