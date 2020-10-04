package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.manager.mangers.CombatManager
import me.zeroeightsix.kami.manager.mangers.PlayerPacketManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.LagCompensator
import me.zeroeightsix.kami.util.combat.CombatUtils
import me.zeroeightsix.kami.util.math.RotationUtils
import me.zeroeightsix.kami.util.math.RotationUtils.faceEntityClosest
import me.zeroeightsix.kami.util.math.Vec2f
import net.minecraft.entity.Entity
import net.minecraft.util.EnumHand

@Module.Info(
        name = "Aura",
        category = Module.Category.COMBAT,
        description = "Hits entities around you",
        modulePriority = 50
)
object Aura : Module() {
    private val delayMode = register(Settings.e<WaitMode>("Mode", WaitMode.DELAY))
    private val multi = register(Settings.b("Multi", false))
    private val lockView = register(Settings.booleanBuilder("LockView").withValue(false).withVisibility { !multi.value })
    private val spoofRotation = register(Settings.booleanBuilder("SpoofRotation").withValue(true).withVisibility { !multi.value && !lockView.value })
    private val waitTick = register(Settings.floatBuilder("SpamDelay").withMinimum(0.1f).withValue(2.0f).withMaximum(40.0f).withVisibility { delayMode.value == WaitMode.SPAM })
    val range = register(Settings.floatBuilder("Range").withValue(5f).withRange(0f, 10f))
    private val tpsSync = register(Settings.b("TPSSync", false))
    private val autoTool = register(Settings.b("AutoWeapon", true))
    private val prefer = register(Settings.enumBuilder(CombatUtils.PreferWeapon::class.java).withName("Prefer").withValue(CombatUtils.PreferWeapon.SWORD).withVisibility { autoTool.value })
    private val disableOnDeath = register(Settings.b("DisableOnDeath", false))

    private var inactiveTicks = 0
    private var tickCount = 0

    private enum class WaitMode {
        DELAY, SPAM
    }

    override fun isActive(): Boolean {
        return inactiveTicks <= 20 && isEnabled
    }

    override fun onUpdate() {
        inactiveTicks++
        if (mc.player.isDead) {
            if (mc.player.isDead && disableOnDeath.value) disable()
            return
        }
        if (!CombatManager.isOnTopPriority(this) || CombatSetting.pause) return
        val targetList = CombatManager.targetList
        val target = CombatManager.target

        if (multi.value && targetList.isNotEmpty()) {
            inactiveTicks = 0
            if (canAttack()) {
                for (entity in targetList) {
                    if (mc.player.getDistance(entity) > range.value) continue
                    attack(entity)
                }
            }
        } else if (target != null && mc.player.getDistance(target) < range.value) {
            inactiveTicks = 0
            if (lockView.value) {
                faceEntityClosest(target)
            } else if (spoofRotation.value) {
                val rotation = Vec2f(RotationUtils.getRotationToEntityClosest(target))
                PlayerPacketManager.addPacket(this, PlayerPacketManager.PlayerPacket(rotating = true, rotation = rotation))
            }
            if (canAttack()) attack(target)
        }
    }

    private fun canAttack(): Boolean {
        val adjustTicks = if (!tpsSync.value) 0f else LagCompensator.adjustTicks
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
        if (autoTool.value) CombatUtils.equipBestWeapon(prefer.value as CombatUtils.PreferWeapon)
        mc.playerController.attackEntity(mc.player, e)
        mc.player.swingArm(EnumHand.MAIN_HAND)
    }
}