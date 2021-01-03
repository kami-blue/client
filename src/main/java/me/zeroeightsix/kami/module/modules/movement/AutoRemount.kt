package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.TickTimer
import me.zeroeightsix.kami.util.TimeUnit
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityBoat
import net.minecraft.entity.passive.*
import net.minecraft.util.EnumHand
import org.kamiblue.event.listener.listener

@Module.Info(
        name = "AutoRemount",
        description = "Automatically remounts your horse",
        category = Module.Category.MOVEMENT
)
object AutoRemount : Module() {
    private val boat = register(Settings.b("Boats", true))
    private val horse = register(Settings.b("Horse", true))
    private val skeletonHorse = register(Settings.b("SkeletonHorse", true))
    private val donkey = register(Settings.b("Donkey", true))
    private val mule = register(Settings.b("Mule", true))
    private val pig = register(Settings.b("Pig", true))
    private val llama = register(Settings.b("Llama", true))
    private val range = register(Settings.floatBuilder("Range").withValue(2.0f).withRange(1.0f, 5.0f).withStep(0.5f))
    private val remountDelay = register(Settings.integerBuilder("RemountDelay").withValue(5).withRange(0, 10))

    private var remountTimer = TickTimer(TimeUnit.TICKS)

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