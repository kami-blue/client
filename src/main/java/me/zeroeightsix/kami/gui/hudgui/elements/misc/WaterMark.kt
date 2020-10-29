package me.zeroeightsix.kami.gui.hudgui.elements.misc

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.gui.hudgui.HudElement
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.module.modules.client.Capes

@HudElement.Info(
        category = HudElement.Category.MISC,
        description = "KAMI Blue watermark"
)
object WaterMark : LabelHud("Watermark") {

    override val closeable: Boolean get() = Capes.allCapes[mc.session.playerID] != null

    override fun onGuiInit() {
        super.onGuiInit()
        visible.value = visible.value
    }

    override fun updateText() {
        displayText.add(KamiMod.MODNAME)
        displayText.add(KamiMod.KAMI_KANJI)
        displayText.add(KamiMod.VER_SMALL)
    }

}