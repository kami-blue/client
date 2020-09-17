package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.module.Module
import net.minecraft.init.MobEffects
import net.minecraft.potion.Potion

@Module.Info(
        name = "AntiLevitation",
        description = "Removes levitation potion effect",
        category = Module.Category.MOVEMENT
)
object AntiLevitation : Module() {
    override fun onUpdate() {
        if (mc.player.isPotionActive(MobEffects.LEVITATION)) {
            mc.player.removeActivePotionEffect(Potion.getPotionFromResourceLocation("levitation"))
        }
    }
}