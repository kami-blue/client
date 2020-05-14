package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.item.*
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.util.math.BlockPos

/**
 * Created by dominikaaaa on 23/10/2019
 * @author dominikaaaa
 * Updated by dominikaaaa on 03/12/19
 * Updated by d1gress/Qther on 4/12/19
 *
 * Bowspam code from https://github.com/seppukudevelopment/seppuku/blob/5586365/src/main/java/me/rigamortis/seppuku/impl/module/combat/FastBowModule.java
 */
@Module.Info(
        name = "FastUse",
        category = Module.Category.PLAYER,
        description = "Use items faster"
)
class Fastuse : Module() {
    private val delay = register(Settings.integerBuilder("Delay").withMinimum(0).withMaximum(20).withValue(0).build())
    private val all = register(Settings.b("All", false))
    private val bow = register(Settings.booleanBuilder().withName("Bow").withValue(true).withVisibility { !all.value }.build())
    private val chargeState = register(Settings.integerBuilder("Bow Charge").withMinimum(0).withMaximum(20).withValue(3).withVisibility { all.value || bow.value }.build())
    private val expBottles = register(Settings.booleanBuilder().withName("Exp Bottles").withValue(true).withVisibility { !all.value }.build())
    private val endCrystals = register(Settings.booleanBuilder().withName("End Crystals").withValue(true).withVisibility { !all.value }.build())
    private val fireworks = register(Settings.booleanBuilder().withName("Fireworks").withValue(false).withVisibility { !all.value }.build())

    public override fun onDisable() {
        mc.rightClickDelayTimer = 4
    }

    override fun onUpdate() {
        if (mc.player == null || mc.player.isSpectator) return

        if ((all.value || bow.value) && mc.player.heldItemMainhand.getItem() is ItemBow && mc.player.isHandActive && mc.player.itemInUseMaxCount >= chargeState.value) {
            mc.player.connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.horizontalFacing))
            mc.player.connection.sendPacket(CPacketPlayerTryUseItem(mc.player.activeHand))
            mc.player.stopActiveHand()
        }

        if (delay.value > 0) {
            if (time <= 0) time = Math.round((2 * Math.round(delay.value.toFloat() / 2)).toFloat()).toLong() else {
                time--
                mc.rightClickDelayTimer = 1
                return
            }
        }

        if (passItemCheck(mc.player.heldItemMainhand.getItem()) || passItemCheck(mc.player.heldItemOffhand.getItem())) {
            mc.rightClickDelayTimer = 0
        }
    }

    private fun passItemCheck(item: Item): Boolean {
        if (all.value) return true
        if (expBottles.value && item is ItemExpBottle) return true
        if (endCrystals.value && item is ItemEndCrystal) return true
        return fireworks.value && item is ItemFirework
    }

    companion object {
        private var time: Long = 0
    }
}