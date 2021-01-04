package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.init.MobEffects
import net.minecraftforge.fml.common.gameevent.TickEvent

@Module.Info(
        name = "AntiLevitation",
        description = "Removes levitation potion effect",
        category = Module.Category.MOVEMENT
)
object AntiLevitation : Module() {
    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (player.isPotionActive(MobEffects.LEVITATION)) {
                player.removeActivePotionEffect(MobEffects.LEVITATION)
            }
        }
    }
}