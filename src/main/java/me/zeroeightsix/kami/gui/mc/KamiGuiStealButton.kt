package me.zeroeightsix.kami.gui.mc

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.modules.player.ChestStealer
import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.client.gui.GuiButton

class KamiGuiStealButton(x: Int, y: Int) :
        GuiButton(6969, x, y, 50, 20, "Steal") {
    override fun mouseReleased(mouseX: Int, mouseY: Int) {
        val chestStealer = KamiMod.MODULE_MANAGER.getModuleT(ChestStealer::class.java)
        if (chestStealer.stealMode.value === ChestStealer.StealMode.MANUAL) {
            chestStealer.stealing = false
            playPressSound(Wrapper.getMinecraft().soundHandler)
        }
        super.mouseReleased(mouseX, mouseY)
    }
}