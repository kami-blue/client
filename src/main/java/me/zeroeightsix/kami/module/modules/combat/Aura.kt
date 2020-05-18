package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.misc.AutoTool.Companion.equipBestWeapon
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BaritoneUtils.pause
import me.zeroeightsix.kami.util.BaritoneUtils.unpause
import me.zeroeightsix.kami.util.EntityUtil
import me.zeroeightsix.kami.util.Friends
import me.zeroeightsix.kami.util.LagCompensator
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.util.EnumHand
import net.minecraft.util.math.Vec3d

/**
 * Created by 086 on 12/12/2017.
 * Updated by hub on 31 October 2019
 * Updated by bot-debug on 10/04/20
 * Baritone compat added by dominikaaaa on 18/05/20
 */
@Module.Info(
        name = "Aura",
        category = Module.Category.COMBAT,
        description = "Hits entities around you"
)
class Aura : Module() {
    private val delayMode = register(Settings.e<WaitMode>("Mode", WaitMode.DELAY))
    private val autoSpamDelay = register(Settings.booleanBuilder("Auto Spam Delay").withValue(true).withVisibility { delayMode.value == WaitMode.SPAM }.build())
    private val waitTick = register(Settings.doubleBuilder("Spam Delay").withMinimum(0.1).withValue(2.0).withMaximum(20.0).withVisibility { !autoSpamDelay.value && delayMode.value == WaitMode.SPAM }.build())
    private val eat = register(Settings.b("While Eating", true))
    private val multi = register(Settings.b("Multi", true))
    private val attackPlayers = register(Settings.b("Players", true))
    private val attackMobs = register(Settings.b("Mobs", false))
    private val attackAnimals = register(Settings.b("Animals", false))
    private val hitRange = register(Settings.d("Hit Range", 5.5))
    private val ignoreWalls = register(Settings.b("Ignore Walls", true))
    private val sync = register(Settings.b("TPS Sync", false))
    val pauseBaritone: Setting<Boolean> = register(Settings.b("Pause Baritone", true))
    private val timeAfterAttack = register(Settings.integerBuilder("Wait to Resume").withRange(1, 10).withValue(3).withVisibility { pauseBaritone.value }.build())
    private val autoTool = register(Settings.b("Auto Weapon", true))
    private val prefer = register(Settings.e<HitMode>("Prefer", HitMode.SWORD))
    private val disableOnDeath = register(Settings.b("Disable On Death", false))

    private var waitCounter = 0
    private var hasAttacked = false // only update isAttacking once during multi
    var isAttacking = false // returned to TemporaryPauseProcess

    enum class HitMode {
        SWORD, AXE, NONE
    }

    private enum class WaitMode {
        DELAY, SPAM
    }

    override fun onUpdate() {
        if (mc.player == null || mc.player.isDead) {
            if (disableOnDeath.value) disable()
            return
        }

        val autoWaitTick = 20.0f - LagCompensator.INSTANCE.tickRate
        val canAttack = mc.player.getCooledAttackStrength(if (sync.value) -autoWaitTick else 0.0f) >= 1

        if (!eat.value) {
            val shield = mc.player.heldItemOffhand.getItem() == Items.SHIELD && mc.player.activeHand == EnumHand.OFF_HAND
            if (mc.player.isHandActive && !shield) {
                return
            }
        }

        if (delayMode.value == WaitMode.DELAY) {
            if (mc.player.getCooledAttackStrength(lagComp) < 1) {
                return
            } else if (mc.player.ticksExisted % 2 != 0) {
                return
            }
        }

        // this goes after delay, because otherwise it would unpause baritone while in the middle of an attack
        if (isAttacking && shouldResetTimer()) {
            isAttacking = false
            if (pauseBaritone.value) unpause()
        }

        if (autoSpamDelay.value) {
            if (delayMode.value == WaitMode.SPAM && autoWaitTick > 0) {
                if (sync.value) {
                    waitCounter = if (waitCounter < autoWaitTick) {
                        waitCounter++
                        return
                    } else {
                        0
                    }
                } else {
                    if (!canAttack) return
                }
            }
        } else {
            if (delayMode.value == WaitMode.SPAM && waitTick.value > 0) {
                waitCounter = if (waitCounter < waitTick.value) {
                    waitCounter++
                    return
                } else {
                    0
                }
            }
        }

        hasAttacked = false // reset for baritone-multi compat.
        for (target in mc.world.loadedEntityList) {
            if (!EntityUtil.isLiving(target)) continue
            if (target === mc.player) continue
            if (mc.player.getDistance(target) > hitRange.value) continue
            if ((target as EntityLivingBase).health <= 0) continue
            if (delayMode.value == WaitMode.DELAY && target.hurtTime != 0) continue
            if (!ignoreWalls.value && !mc.player.canEntityBeSeen(target) && !canEntityFeetBeSeen(target)) continue  // If walls is on & you can't see the feet or head of the target, skip. 2 raytraces needed

            if (attackPlayers.value && target is EntityPlayer && !Friends.isFriend(target.getName())) {
                if (autoTool.value) equipBestWeapon(prefer.value)
                attack(target)
                if (!multi.value) return
            } else {
                if (if (EntityUtil.isPassive(target)) attackAnimals.value else EntityUtil.isMobAggressive(target) && attackMobs.value) {
                    if (autoTool.value) equipBestWeapon(prefer.value)
                    attack(target)
                    if (!multi.value) return
                }
            }
        }
    }

    private fun attack(e: Entity) {
        if (!isAttacking && !hasAttacked && pauseBaritone.value) {
            pause()
        }
        isAttacking = true
        hasAttacked = true
        mc.playerController.attackEntity(mc.player, e)
        mc.player.swingArm(EnumHand.MAIN_HAND)
    }

    private val lagComp: Float
        get() = if (delayMode.value == WaitMode.DELAY) {
            -(20 - LagCompensator.INSTANCE.tickRate)
        } else 0.0f

    private fun canEntityFeetBeSeen(entityIn: Entity): Boolean {
        return mc.world.rayTraceBlocks(Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), Vec3d(entityIn.posX, entityIn.posY, entityIn.posZ), false, true, false) == null
    }

    private var startTime: Long = 0
    private fun shouldResetTimer(): Boolean {
        if (startTime == 0L) startTime = System.currentTimeMillis()
        if (startTime + timeAfterAttack.value * 1000 <= System.currentTimeMillis()) { // 1 timeout = 1 second = 1000 ms
            startTime = System.currentTimeMillis()
            return true
        }
        return false
    }
}