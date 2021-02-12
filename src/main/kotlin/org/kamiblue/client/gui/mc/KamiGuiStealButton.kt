package org.kamiblue.client.gui.mc

import net.minecraft.client.gui.GuiButton
import org.kamiblue.client.module.modules.player.ChestStealer
import org.kamiblue.client.util.Wrapper

class KamiGuiStealButton(x: Int, y: Int) :
    GuiButton(696969, x, y, 50, 20, "Steal") {
    override fun mouseReleased(mouseX: Int, mouseY: Int) {
        if (ChestStealer.mode.value === ChestStealer.Mode.MANUAL) {
            ChestStealer.stealing = false
            playPressSound(Wrapper.minecraft.soundHandler)
        }
        super.mouseReleased(mouseX, mouseY)
    }
}