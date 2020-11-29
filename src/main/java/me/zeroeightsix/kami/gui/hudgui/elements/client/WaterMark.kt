package me.zeroeightsix.kami.gui.hudgui.elements.client

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.module.modules.client.Capes

object WaterMark : LabelHud(
    name = "Watermark",
    category = Category.CLIENT,
    description = "KAMI Blue watermark",
    enabledByDefault = true
) {

    override val closeable: Boolean get() = Capes.isPremium

    override fun onGuiInit() {
        super.onGuiInit()
        visible.value = visible.value
    }

    override fun updateText() {
        displayText.add(KamiMod.NAME, primaryColor.value)
        displayText.add(KamiMod.VERSION_SIMPLE, secondaryColor.value)
    }

    init {
        posX = 0.0f
        posY = 0.0f
    }
}