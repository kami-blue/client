package me.zeroeightsix.kami.gui.mc

import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.module.modules.player.ChestStealer
import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.client.gui.GuiButton

class KamiGuiStealButton(x: Int, y: Int) :
        GuiButton(696969, x, y, 50, 20, "Steal") {
    override fun mouseReleased(mouseX: Int, mouseY: Int) {
        val chestStealer = ModuleManager.getModuleT(ChestStealer::class.java) ?: return
        if (chestStealer.stealMode.value === ChestStealer.StealMode.MANUAL) {
            chestStealer.stealing = false
            playPressSound(Wrapper.minecraft.soundHandler)
        }
        super.mouseReleased(mouseX, mouseY)
    }
}