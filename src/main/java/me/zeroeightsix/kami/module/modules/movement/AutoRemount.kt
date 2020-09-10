package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.TimerUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityBoat
import net.minecraft.entity.passive.*
import net.minecraft.util.EnumHand

/**
 * @author dominikaaaa
 * Created by dominikaaaa on 05/04/20
 * updated by ionar2 on 04/05/20
 * Updated by dominikaaaa on 05/06/20
 * Updated by Xiaro on 09/09/20
 */
@Module.Info(
        name = "AutoRemount",
        description = "Automatically remounts your horse",
        category = Module.Category.MOVEMENT
)
class AutoRemount : Module() {
    private val boat = register(Settings.b("Boats", true))
    private val horse = register(Settings.b("Horse", true))
    private val skeletonHorse = register(Settings.b("SkeletonHorse", true))
    private val donkey = register(Settings.b("Donkey", true))
    private val mule = register(Settings.b("Mule", true))
    private val pig = register(Settings.b("Pig", true))
    private val llama = register(Settings.b("Llama", true))
    private val range = register(Settings.floatBuilder("Range").withMinimum(1.0f).withValue(2.0f).withMaximum(10.0f))
    private val remountDelay = register(Settings.integerBuilder("RemountDelay").withValue(5).withRange(0, 10))

    private var remountTimer = TimerUtils.TickTimer(TimerUtils.TimeUnit.TICKS)

    override fun onUpdate() {
        // we don't need to do anything if we're already riding.
        if (mc.player.isRiding) {
            remountTimer.reset()
            return
        }
        if (remountTimer.tick(remountDelay.value.toLong())) {
            mc.world.loadedEntityList.stream()
                    .filter { entity: Entity -> isValidEntity(entity) }
                    .min(compareBy { mc.player.getDistance(it) })
                    .ifPresent { mc.playerController.interactWithEntity(mc.player, it, EnumHand.MAIN_HAND) }
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