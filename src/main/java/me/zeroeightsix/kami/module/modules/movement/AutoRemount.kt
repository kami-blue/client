package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityBoat
import net.minecraft.entity.passive.*
import net.minecraft.util.EnumHand

@Module.Info(
        name = "AutoRemount",
        description = "Automatically remounts your horse",
        category = Module.Category.MOVEMENT
)
object AutoRemount : Module() {
    private val boat = setting("Boats", true)
    private val horse = setting("Horse", true)
    private val skeletonHorse = setting("SkeletonHorse", true)
    private val donkey = setting("Donkey", true)
    private val mule = setting("Mule", true)
    private val pig = setting("Pig", true)
    private val llama = setting("Llama", true)
    private val range = setting("Range", 2.0f, 1.0f..5.0f, 0.5f)
    private val remountDelay = setting("RemountDelay", 5, 0..10, 1)

    private var remountTimer = TimerUtils.TickTimer(TimerUtils.TimeUnit.TICKS)

    init {
        listener<SafeTickEvent> {
            // we don't need to do anything if we're already riding.
            if (mc.player.isRiding) {
                remountTimer.reset()
                return@listener
            }
            if (remountTimer.tick(remountDelay.value.toLong())) {
                mc.world.loadedEntityList.stream()
                        .filter { entity: Entity -> isValidEntity(entity) }
                        .min(compareBy { mc.player.getDistance(it) })
                        .ifPresent { mc.playerController.interactWithEntity(mc.player, it, EnumHand.MAIN_HAND) }
            }
        }
    }

    private fun isValidEntity(entity: Entity): Boolean {
        if (entity.getDistance(mc.player) > range.value) return false
        return entity is EntityBoat && boat.value
                || entity is EntityAnimal && !entity.isChild // FBI moment
                && (entity is EntityHorse && horse.value
                || entity is EntitySkeletonHorse && skeletonHorse.value
                || entity is EntityDonkey && donkey.value
                || entity is EntityMule && mule.value
                || entity is EntityPig && entity.saddled && pig.value
                || entity is EntityLlama && llama.value)
    }
}