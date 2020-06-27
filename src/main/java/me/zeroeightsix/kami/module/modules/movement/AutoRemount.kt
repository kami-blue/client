package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityBoat
import net.minecraft.entity.passive.*
import net.minecraft.util.EnumHand
import java.util.*

/**
 * @author dominikaaaa
 * Created by dominikaaaa on 05/04/20
 * updated by ionar2 on 04/05/20
 * Updated by dominikaaaa on 05/06/20
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
    private val range = register(Settings.floatBuilder("Range").withMinimum(1.0f).withValue(2.0f).withMaximum(10.0f).build())

    override fun onUpdate() {
        // we don't need to do anything if we're already riding.
        if (mc.player.isRiding) return

        mc.world.loadedEntityList.stream()
                .filter { entity: Entity -> isValidEntity(entity) }
                .min(Comparator.comparing { en: Entity? -> mc.player.getDistance(en) })
                .ifPresent { entity: Entity? -> mc.playerController.interactWithEntity(mc.player, entity, EnumHand.MAIN_HAND) }
    }

    private fun isValidEntity(entity: Entity): Boolean {
        if (mc.player.isRiding) return false

        if (entity.getDistance(mc.player) > range.value) return false

        if (entity is AbstractHorse) {
            val horse = entity

            /// no animal abuse done in this module, no thanks.
            if (entity.isChild) return false
        }

        if (entity is EntityBoat && boat.value) return true

        if (entity is EntityHorse && horse.value) return true

        if (entity is EntitySkeletonHorse && skeletonHorse.value) return true

        if (entity is EntityDonkey && donkey.value) return true

        if (entity is EntityMule && mule.value) return true

        if (entity is EntityPig && pig.value) {
            val pig = entity
            return entity.saddled
        }

        return entity is EntityLlama && llama.value
    }
}