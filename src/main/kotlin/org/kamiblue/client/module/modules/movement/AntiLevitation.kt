package org.kamiblue.client.module.modules.movement

import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.init.MobEffects
import net.minecraftforge.fml.common.gameevent.TickEvent

internal object AntiLevitation : Module(
    name = "AntiLevitation",
    description = "Removes levitation potion effect",
    category = Category.MOVEMENT
) {
    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (player.isPotionActive(MobEffects.LEVITATION)) {
                player.removeActivePotionEffect(MobEffects.LEVITATION)
            }
        }
    }
}