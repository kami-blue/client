package me.zeroeightsix.kami.gui.mc

import me.zeroeightsix.kami.module.modules.player.ChestStealer
import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.client.gui.GuiButton

class KamiGuiStoreButton(x: Int, y: Int) :
    GuiButton(420420, x, y, 50, 20, "Store") {
    override fun mouseReleased(mouseX: Int, mouseY: Int) {
        if (ChestStealer.stealMode.value === ChestStealer.StealMode.MANUAL) {
            ChestStealer.storing = false
            playPressSound(Wrapper.minecraft.soundHandler)
        }
        super.mouseReleased(mouseX, mouseY)
    }
}