package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.misc.AutoTool.Companion.equipBestWeapon
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BaritoneUtils.pause
import me.zeroeightsix.kami.util.BaritoneUtils.unpause
import me.zeroeightsix.kami.util.EntityUtil.EntityPriority
import me.zeroeightsix.kami.util.EntityUtil.faceEntity
import me.zeroeightsix.kami.util.EntityUtil.getPrioritizedTarget
import me.zeroeightsix.kami.util.EntityUtil.getTargetList
import me.zeroeightsix.kami.util.LagCompensator
import net.minecraft.entity.Entity
import net.minecraft.init.Items
import net.minecraft.util.EnumHand

/**
 * Created by 086 on 12/12/2017.
 * Updated by hub on 31 October 2019
 * Updated by bot-debug on 10/04/20
 * Baritone compat added by dominikaaaa on 18/05/20
 * Updated by Xiaro on 11/07/20
 */
@Module.Info(
        name = "Aura",
        category = Module.Category.COMBAT,
        description = "Hits entities around you"
)
class Aura : Module() {
    private val delayMode = register(Settings.e<WaitMode>("Mode", WaitMode.DELAY))
    private val priority = register(Settings.e<EntityPriority>("Priority", EntityPriority.DISTANCE))
    private val multi = register(Settings.b("Multi", true))
    private val lockView = register(Settings.booleanBuilder("LockView").withValue(false).withVisibility { !multi.value }.build())
    private val waitTick = register(Settings.floatBuilder("SpamDelay").withMinimum(0.1f).withValue(2.0f).withMaximum(40.0f).withVisibility { delayMode.value == WaitMode.SPAM }.build())
    private val eat = register(Settings.b("WhileEating", true))
    private val players = register(Settings.b("Players", true))
    private val friends = register(Settings.booleanBuilder("Friends").withValue(false).withVisibility { players.value }.build())
    private val sleeping = register(Settings.booleanBuilder("Sleeping").withValue(false).withVisibility { players.value }.build())
    private val mobs = register(Settings.b("Mobs", false))
    private val passive = register(Settings.booleanBuilder("PassiveMobs").withValue(false).withVisibility { mobs.value }.build())
    private val neutral = register(Settings.booleanBuilder("NeutralMobs").withValue(false).withVisibility { mobs.value }.build())
    private val hostile = register(Settings.booleanBuilder("HostileMobs").withValue(false).withVisibility { mobs.value }.build())
    private val range = register(Settings.f("Range", 5.5f))
    private val ignoreWalls = register(Settings.b("IgnoreWalls", true))
    private val sync = register(Settings.b("TPSSync", false))
    val pauseBaritone: Setting<Boolean> = register(Settings.b("PauseBaritone", true))
    private val timeAfterAttack = register(Settings.integerBuilder("ResumeDelay").withRange(1, 10).withValue(3).withVisibility { pauseBaritone.value }.build())
    private val autoTool = register(Settings.b("AutoWeapon", true))
    private val prefer = register(Settings.e<HitMode>("Prefer", HitMode.SWORD))
    private val disableOnDeath = register(Settings.b("DisableOnDeath", false))

    private var waitCounter = 0
    private var startTime: Long = 0
    var isAttacking = false // returned to TemporaryPauseProcess

    private enum class WaitMode {
        DELAY, SPAM
    }

    enum class HitMode {
        SWORD, AXE, NONE
    }

    override fun onUpdate() {
        if (mc.player == null || mc.player.isDead) {
            if (mc.player.isDead && disableOnDeath.value) disable()
            return
        }

        waitCounter++
        val player = arrayOf(players.value, friends.value, sleeping.value)
        val mob = arrayOf(mobs.value, passive.value, neutral.value, hostile.value)
        val targetList = getTargetList(player, mob, ignoreWalls.value, delayMode.value == WaitMode.SPAM, range.value)
        if (targetList.isNotEmpty()) {
            /* Pausing baritone and other stuff */
            if (pauseBaritone.value && !isAttacking) {
                isAttacking = true
                startTime = 0L
                pause()
            }

            if (autoTool.value) equipBestWeapon(prefer.value)
            if (multi.value) {
                if (canAttack()) for (i in targetList.indices) {
                    attack(targetList[i])
                }
            } else {
                val target = getPrioritizedTarget(targetList, priority.value)
                if (lockView.value) faceEntity(target)
                if (canAttack()) attack(target)
            }
        } else if (isAttacking && canResume()) {
            isAttacking = false
            unpause()
        }
    }

    override fun onEnable() {
        waitCounter = 0
    }

    private fun canAttack(): Boolean {
        if (!eat.value) {
            val shield = mc.player.heldItemOffhand.getItem() == Items.SHIELD && mc.player.activeHand == EnumHand.OFF_HAND
            if (mc.player.isHandActive && !shield) return false
        }

        val preSyncWaitTime = if (delayMode.value == WaitMode.DELAY) mc.player.cooldownPeriod * 2 else waitTick.value
        val syncedWaitTick = if (sync.value) preSyncWaitTime / ((LagCompensator.INSTANCE.tickRate - 1.0f) / 20.0f) else preSyncWaitTime
        return if (waitCounter >= syncedWaitTick) {
            waitCounter = 0
            true
        } else false
    }

    private fun attack(e: Entity) {
        mc.playerController.attackEntity(mc.player, e)
        mc.player.swingArm(EnumHand.MAIN_HAND)
    }

    private fun canResume(): Boolean {
        if (startTime == 0L) startTime = System.currentTimeMillis()
        return if (startTime + timeAfterAttack.value * 1000 <= System.currentTimeMillis()) { // 1 timeout = 1 second = 1000 ms
            startTime = System.currentTimeMillis()
            true
        } else false
    }
}