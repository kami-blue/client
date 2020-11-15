package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.init.MobEffects

@Module.Info(
        name = "AntiLevitation",
        description = "Removes levitation potion effect",
        category = Module.Category.MOVEMENT
)
object AntiLevitation : Module() {
    init {
        listener<SafeTickEvent> {
            if (mc.player.isPotionActive(MobEffects.LEVITATION)) {
                mc.player.removeActivePotionEffect(MobEffects.LEVITATION)
            }
        }
    }
}