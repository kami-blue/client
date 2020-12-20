package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import net.minecraft.init.MobEffects
import org.kamiblue.event.listener.listener

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