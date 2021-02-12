package org.kamiblue.client.plugin.api

import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.util.graphics.VertexHelper
import org.kamiblue.client.util.graphics.font.TextComponent
import org.kamiblue.client.util.math.Vec2d
import org.kamiblue.client.util.threads.safeAsyncListener

abstract class PluginLabelHud(
    pluginMain: Plugin,
    name: String,
    alias: Array<String> = emptyArray(),
    category: Category,
    description: String,
    alwaysListening: Boolean = false,
    enabledByDefault: Boolean = false
) : PluginHudElement(pluginMain, name, alias, category, description, alwaysListening, enabledByDefault) {

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
