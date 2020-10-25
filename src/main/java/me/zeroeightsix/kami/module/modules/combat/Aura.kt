package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.manager.managers.CombatManager
import me.zeroeightsix.kami.manager.managers.PlayerPacketManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.TpsCalculator
import me.zeroeightsix.kami.util.combat.CombatUtils
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.math.RotationUtils
import me.zeroeightsix.kami.util.math.RotationUtils.faceEntityClosest
import me.zeroeightsix.kami.util.math.Vec2f
import net.minecraft.entity.Entity
import net.minecraft.util.EnumHand
import net.minecraftforge.fml.common.gameevent.TickEvent

@CombatManager.CombatModule
@Module.Info(
        name = "Aura",
        category = Module.Category.COMBAT,
        description = "Hits entities around you",
        modulePriority = 50
)
object Aura : Module() {
    private val delayMode = setting("Mode", WaitMode.DELAY)
    private val lockView = setting("LockView", false)
    private val spoofRotation = setting("SpoofRotation", true, { !lockView.value })
    private val waitTick = setting("SpamDelay", 2.0f, 1.0f..40.0f, 0.5f, { delayMode.value == WaitMode.SPAM })
    val range = setting("Range", 5f, 0f..8f, 0.25f)
    private val tpsSync = setting("TPSSync", false)
    private val autoTool = setting("AutoWeapon", true)
    private val prefer = setting("Prefer", CombatUtils.PreferWeapon.SWORD, { autoTool.value })
    private val disableOnDeath = setting("DisableOnDeath", false)

    private var inactiveTicks = 0
    private var tickCount = 0

    private enum class WaitMode {
        DELAY, SPAM
    }

    override fun isActive(): Boolean {
        return inactiveTicks <= 20 && isEnabled
    }

    init {
        listener<SafeTickEvent> {
            if (it.phase != TickEvent.Phase.START) return@listener

            inactiveTicks++
            if (mc.player.isDead) {
                if (mc.player.isDead && disableOnDeath.value) disable()
                return@listener
            }
            if (!CombatManager.isOnTopPriority(this) || CombatSetting.pause) return@listener
            val target = CombatManager.target

            if (target != null && mc.player.getDistance(target) < range.value) {
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
    }

    private fun canAttack(): Boolean {
        val adjustTicks = if (!tpsSync.value) 0f else TpsCalculator.adjustTicks
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
        if (autoTool.value) CombatUtils.equipBestWeapon(prefer.value)
        mc.playerController.attackEntity(mc.player, e)
        mc.player.swingArm(EnumHand.MAIN_HAND)
    }
}