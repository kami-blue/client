package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.Friends
import me.zeroeightsix.kami.util.MathsUtils
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemBow
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.MathHelper
import kotlin.math.atan2

/**
 * Created by Dewy on the 16th of April, 2020
 */
@Module.Info(
        name = "AimBot",
        description = "Automatically aims at entities for you.",
        category = Module.Category.COMBAT
)
class AimBot : Module() {
    private val range = register(Settings.integerBuilder("Range").withMinimum(4).withMaximum(24).withValue(16))
    private val useBow = register(Settings.booleanBuilder("Use Bow").withValue(true))
    private val ignoreWalls = register(Settings.booleanBuilder("Ignore Walls").withValue(true))
    private val targetPlayers = register(Settings.booleanBuilder("Target Players").withValue(true))
    private val targetFriends = register(Settings.booleanBuilder("Friends").withValue(false).withVisibility { targetPlayers.value == true })
    private val targetSleeping = register(Settings.booleanBuilder("Sleeping").withValue(false).withVisibility { targetPlayers.value == true })
    private val targetMobs = register(Settings.booleanBuilder("Target Mobs").withValue(false))
    private val targetHostileMobs = register(Settings.booleanBuilder("Hostile").withValue(true).withVisibility { targetMobs.value == true })
    private val targetPassiveMobs = register(Settings.booleanBuilder("Passive").withValue(false).withVisibility { targetMobs.value == true })

    override fun onUpdate() {
        if (KamiMod.MODULE_MANAGER.getModuleT(Aura::class.java).isEnabled) {
            return
        }

        if (useBow.value) {
            var bowSlot = 0
            for (i in 0..9) {
                val potentialBow = mc.player.inventory.getStackInSlot(i)
                if (potentialBow.getItem() is ItemBow) {
                    bowSlot = mc.player.inventory.getSlotFor(potentialBow)
                }
            }
            mc.player.inventory.currentItem = bowSlot
            mc.playerController.syncCurrentPlayItem()
        }

        for (entity in mc.world.loadedEntityList) {
            if (entity is EntityLivingBase
                    && (entity !is EntityPlayerSP && mc.player.getDistance(entity) <= range.value && entity.health > 0)) {
                if (!ignoreWalls.value) {
                    if (!mc.player.canEntityBeSeen(entity)) {
                        return
                    }
                }
                if (targetMobs.value) {
                    if (targetHostileMobs.value && entity.soundCategory == SoundCategory.HOSTILE) {
                        faceEntity(entity)
                    }
                    if (targetPassiveMobs.value && entity is EntityAnimal) {
                        faceEntity(entity)
                    }
                }
                if (targetPlayers.value) {
                    if (entity.isPlayerSleeping && entity is EntityPlayer && targetSleeping.value) {
                        faceEntity(entity)
                    }
                    if (!targetFriends.value) {
                        for (friend in Friends.friends.value) {
                            if (friend.username != entity.name) {
                                faceEntity(entity)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun faceEntity(entity: Entity) {
        val diffX = entity.posX - mc.player.posX
        val diffZ = entity.posZ - mc.player.posZ
        val diffY = mc.player.posY + mc.player.getEyeHeight().toDouble() - (entity.posY + entity.eyeHeight.toDouble())

        val xz = MathHelper.sqrt(diffX * diffX + diffZ * diffZ).toDouble()
        val yaw = MathsUtils.normalizeAngle(atan2(diffZ, diffX) * 180.0 / Math.PI - 90.0f).toFloat()
        val pitch = MathsUtils.normalizeAngle(-atan2(diffY, xz) * 180.0 / Math.PI).toFloat()

        mc.player.setPositionAndRotation(mc.player.posX, mc.player.posY, mc.player.posZ, yaw, -pitch)
    }
}