package me.zeroeightsix.kami.gui.hudgui.elements.client

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.module.modules.client.Capes

object WaterMark : LabelHud(
    name = "Watermark",
    category = Category.CLIENT,
    description = "KAMI Blue watermark",
    enabledByDefault = true
) {

    override val hudWidth: Float get() = (displayText.getWidth() + 2.0f) / scale
    override val hudHeight: Float get() = displayText.getHeight(2)  / scale

    override val closeable: Boolean get() = Capes.isPremium

    override fun onGuiInit() {
        super.onGuiInit()
        visible = visible
    }

    override fun SafeClientEvent.updateText() {
        displayText.add(KamiMod.NAME, primaryColor)
        displayText.add(KamiMod.VERSION_SIMPLE, secondaryColor)
    }

    init {
        posX = 0.0f
        posY = 0.0f
    }
}