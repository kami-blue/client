package me.zeroeightsix.kami.module.modules.sdashb.combat;

import me.zeroeightsix.kami.module.Module;
import net.minecraft.item.ItemExpBottle;

/***
 * @author Unknown, LGPL licensed
 */
@Module.Info(name = "FastXP", category = Module.Category.COMBAT, description = "Makes XP Faster for PvP")
public class FastXP extends Module {
    public void onUpdate() {
        if (mc.player.inventory.getCurrentItem().getItem() instanceof ItemExpBottle) {
            mc.rightClickDelayTimer = 0;
        }
    }
}
