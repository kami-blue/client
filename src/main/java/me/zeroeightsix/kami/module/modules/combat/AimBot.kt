package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.EntityUtil.EntityPriority
import me.zeroeightsix.kami.util.EntityUtil.faceEntity
import me.zeroeightsix.kami.util.EntityUtil.getPrioritizedTarget
import me.zeroeightsix.kami.util.EntityUtil.getTargetList
import net.minecraft.item.ItemBow

/**
 * Created by Dewy on the 16th of April, 2020
 * Updated by Xiaro on 10/07/20
 */
@Module.Info(
        name = "AimBot",
        description = "Automatically aims at entities for you.",
        category = Module.Category.COMBAT
)
class AimBot : Module() {
    private val priority = register(Settings.e<EntityPriority>("Priority", EntityPriority.DISTANCE))
    private val range = register(Settings.floatBuilder("Range").withValue(16.0f).withMinimum(4.0f).withMaximum(24.0f).build())
    private val useBow = register(Settings.booleanBuilder("UseBow").withValue(true).build())
    private val ignoreWalls = register(Settings.booleanBuilder("IgnoreWalls").withValue(true).build())
    private val players = register(Settings.booleanBuilder("Players").withValue(true).build())
    private val friends = register(Settings.booleanBuilder("Friends").withValue(false).withVisibility { players.value == true }.build())
    private val sleeping = register(Settings.booleanBuilder("Sleeping").withValue(false).withVisibility { players.value == true }.build())
    private val mobs = register(Settings.b("Mobs", false))
    private val passive = register(Settings.booleanBuilder("PassiveMobs").withValue(false).withVisibility { mobs.value }.build())
    private val neutral = register(Settings.booleanBuilder("NeutralMobs").withValue(false).withVisibility { mobs.value }.build())
    private val hostile = register(Settings.booleanBuilder("HostileMobs").withValue(false).withVisibility { mobs.value }.build())

    override fun onUpdate() {
        if (mc.player == null || KamiMod.MODULE_MANAGER.getModuleT(Aura::class.java).isEnabled) return

        if (useBow.value) {
            var bowSlot = 0
            while (bowSlot in 0..8 && mc.player.inventory.getStackInSlot(bowSlot).getItem() !is ItemBow) {
                bowSlot++
            }
            if (bowSlot != 9) {
                mc.player.inventory.currentItem = bowSlot
                mc.playerController.syncCurrentPlayItem()
            }
        }
        val player = arrayOf(players.value, friends.value, sleeping.value)
        val mob = arrayOf(mobs.value, passive.value, neutral.value, hostile.value)
        val targetList = getTargetList(player, mob, ignoreWalls.value, false, range.value)
        if (targetList.isEmpty()) return
        faceEntity(getPrioritizedTarget(targetList, priority.value))
    }
}