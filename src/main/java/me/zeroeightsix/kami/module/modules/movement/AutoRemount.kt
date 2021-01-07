package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.TickTimer
import me.zeroeightsix.kami.util.TimeUnit
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityBoat
import net.minecraft.entity.passive.*
import net.minecraft.util.EnumHand
import net.minecraftforge.fml.common.gameevent.TickEvent

object AutoRemount : Module(
    name = "AutoRemount",
    description = "Automatically remounts your horse",
    category = Category.MOVEMENT
) {
    private val boat = setting("Boats", true)
    private val horse = setting("Horse", true)
    private val skeletonHorse = setting("SkeletonHorse", true)
    private val donkey = setting("Donkey", true)
    private val mule = setting("Mule", true)
    private val pig = setting("Pig", true)
    private val llama = setting("Llama", true)
    private val range = setting("Range", 2.0f, 1.0f..5.0f, 0.5f)
    private val remountDelay = setting("RemountDelay", 5, 0..10, 1)

    private var remountTimer = TickTimer(TimeUnit.TICKS)

    init {
        safeListener<TickEvent.ClientTickEvent> {
            // we don't need to do anything if we're already riding.
            if (player.isRiding) {
                remountTimer.reset()
                return@safeListener
            }
            if (remountTimer.tick(remountDelay.value.toLong())) {
                world.loadedEntityList.stream()
                        .filter { entity: Entity -> isValidEntity(entity) }
                        .min(compareBy { player.getDistance(it) })
                        .ifPresent { playerController.interactWithEntity(player, it, EnumHand.MAIN_HAND) }
            }
        }
    }

    private fun SafeClientEvent.isValidEntity(entity: Entity): Boolean {
        if (entity.getDistance(player) > range.value) return false
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