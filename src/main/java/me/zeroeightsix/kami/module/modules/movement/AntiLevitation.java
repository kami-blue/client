package me.zeroeightsix.kami.module.modules.movement;

import me.zeroeightsix.kami.module.Module;
import net.minecraft.potion.Potion;

/**
 * Created by 0x2E | PretendingToCode
 */
@Module.Info(
        name = "AntiLevitation",
        description = "Removes levitation potion effect",
        category = Module.Category.MOVEMENT
)
public class AntiLevitation extends Module {

    @Override
    public void onUpdate() {
        if (mc.player.isPotionActive(Potion.getPotionFromResourceLocation("levitation"))) {
            mc.player.removeActivePotionEffect(Potion.getPotionFromResourceLocation("levitation"));
        }
    }
}
