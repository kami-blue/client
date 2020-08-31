package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.manager.mangers.CombatManager
import me.zeroeightsix.kami.manager.mangers.PlayerPacketManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.misc.AutoTool.Companion.equipBestWeapon
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BaritoneUtils.pause
import me.zeroeightsix.kami.util.BaritoneUtils.unpause
import me.zeroeightsix.kami.util.LagCompensator
import me.zeroeightsix.kami.util.math.RotationUtils
import me.zeroeightsix.kami.util.math.RotationUtils.faceEntity
import me.zeroeightsix.kami.util.math.Vec2f
import net.minecraft.entity.Entity
import net.minecraft.init.Items
import net.minecraft.util.EnumHand

/**
 * Created by 086 on 12/12/2017.
 * Updated by hub on 31 October 2019
 * Updated by bot-debug on 10/04/20
 * Baritone compat added by dominikaaaa on 18/05/20
 * Updated by Xiaro on 29/08/20
 */
@Module.Info(
        name = "Aura",
        category = Module.Category.COMBAT,
        description = "Hits entities around you",
        modulePriority = 50
)
class Aura : Module() {
    private val delayMode = register(Settings.e<WaitMode>("Mode", WaitMode.DELAY))
    private val multi = register(Settings.b("Multi", false))
    private val spoofRotation = register(Settings.booleanBuilder("SpoofRotation").withValue(true).withVisibility { !multi.value }.build())
    private val lockView = register(Settings.booleanBuilder("LockView").withValue(false).withVisibility { !multi.value }.build())
    private val waitTick = register(Settings.floatBuilder("SpamDelay").withMinimum(0.1f).withValue(2.0f).withMaximum(40.0f).withVisibility { delayMode.value == WaitMode.SPAM }.build())
    private val eat = register(Settings.b("WhileEating", true))
    private val sync = register(Settings.b("TPSSync", false))
    private val pauseBaritone: Setting<Boolean> = register(Settings.b("PauseBaritone", true))
    private val timeAfterAttack = register(Settings.integerBuilder("ResumeDelay").withRange(1, 10).withValue(3).withVisibility { pauseBaritone.value }.build())
    private val autoTool = register(Settings.b("AutoWeapon", true))
    private val prefer = register(Settings.e<HitMode>("Prefer", HitMode.SWORD))
    private val disableOnDeath = register(Settings.b("DisableOnDeath", false))

    private var startTime: Long = 0
    private var tickCount = 0
    var active = false // returned to AutoEat

    private enum class WaitMode {
        DELAY, SPAM
    }

    enum class HitMode {
        SWORD, AXE, NONE
    }

    override fun isActive(): Boolean {
        return active && isEnabled
    }

    override fun onUpdate() {
        if (mc.player.isDead) {
            if (mc.player.isDead && disableOnDeath.value) disable()
            return
        }
        if (CombatManager.getTopPriority() > modulePriority) return
        if (autoTool.value) equipBestWeapon(prefer.value)
        if (multi.value) {
            val targetList = CombatManager.targetList
            if (targetList.isEmpty()) {
                unpauseBaritone()
                return
            }
            if (canAttack()) for (target in targetList) {
                attack(target)
            }
        } else {
            val target = CombatManager.target
            if (target == null) {
                unpauseBaritone()
                return
            }
            if (spoofRotation.value) {
                val rotation = RotationUtils.getRotationToEntityClosed(target)
                val packet = PlayerPacketManager.PlayerPacket(rotating = true, rotation = Vec2f(rotation.x.toFloat(), rotation.y.toFloat()))
                PlayerPacketManager.addPacket(this, packet)
            }
            if (lockView.value) faceEntity(target)
            if (canAttack()) attack(target)
        }
        pauseBaritone()
    }

    override fun onDisable() {
        unpause()
    }

    private fun canAttack(): Boolean {
        if (!eat.value) {
            val shield = mc.player.heldItemOffhand.getItem() == Items.SHIELD && mc.player.activeHand == EnumHand.OFF_HAND
            if (mc.player.isHandActive && !shield) return false
        }
        val adjustTicks = if (!sync.value) 0f else (LagCompensator.adjustTicks)
        return if (delayMode.value == WaitMode.DELAY) {
            (mc.player.getCooledAttackStrength(adjustTicks) >= 1f)
        } else {
            if (tickCount < waitTick.value) {
                tickCount++
                false
            } else {
                tickCount = 0
                true
            }
        }
    }

    private fun attack(e: Entity) {
        mc.playerController.attackEntity(mc.player, e)
        mc.player.swingArm(EnumHand.MAIN_HAND)
    }

    private fun pauseBaritone() {
        if (!active) {
            active = true
            if (pauseBaritone.value) {
                startTime = 0L
                pause()
            }
        }
    }

    private fun unpauseBaritone() {
        if (active && canResume()) {
            active = false
            unpause()
        }
    }

    private fun canResume(): Boolean {
        if (startTime == 0L) startTime = System.currentTimeMillis()
        return if (startTime + timeAfterAttack.value * 1000 <= System.currentTimeMillis()) { // 1 timeout = 1 second = 1000 ms
            startTime = System.currentTimeMillis()
            true
        } else false
    }
}