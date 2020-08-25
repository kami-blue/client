package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.item.*
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.util.math.BlockPos

/**
 * @author dominikaaaa
 *
 * Created by dominikaaaa on 23/10/2019
 * Updated by dominikaaaa on 03/12/19
 * Updated by d1gress/Qther on 4/12/19
 * Updated by Xiaro on 24/08/20
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
    private val blocks = register(Settings.b("Blocks", false))
    private val allItems = register(Settings.b("AllItems", false))
    private val expBottles = register(Settings.booleanBuilder().withName("ExpBottles").withValue(true).withVisibility { !allItems.value }.build())
    private val endCrystals = register(Settings.booleanBuilder().withName("EndCrystals").withValue(true).withVisibility { !allItems.value }.build())
    private val fireworks = register(Settings.booleanBuilder().withName("Fireworks").withValue(false).withVisibility { !allItems.value }.build())
    private val bow = register(Settings.booleanBuilder().withName("Bow").withValue(true).withVisibility { !allItems.value }.build())
    private val bowCharge = register(Settings.integerBuilder("BowCharge").withMinimum(0).withMaximum(20).withValue(3).withVisibility { allItems.value || bow.value }.build())
    private val chargeVariation = register(Settings.integerBuilder("ChargeVariation").withValue(5).withRange(0, 20).withVisibility { allItems.value || bow.value }.build())

    private var randomVariation = 0
    private var time = 0

    override fun onUpdate() {
        if (mc.player.isSpectator) return

        if ((allItems.value || bow.value) && mc.player.heldItemMainhand.getItem() is ItemBow && mc.player.isHandActive && mc.player.itemInUseMaxCount >= getBowCharge()) {
            randomVariation = 0
            mc.player.connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.horizontalFacing))
            mc.player.connection.sendPacket(CPacketPlayerTryUseItem(mc.player.activeHand))
            mc.player.stopActiveHand()
        }

        if (delay.value > 0) {
            if (time <= 0) {
                time = delay.value
            } else {
                time--
                return
            }
        }

        if (passItemCheck(mc.player.heldItemMainhand.getItem()) || passItemCheck(mc.player.heldItemOffhand.getItem())) {
            mc.rightClickDelayTimer = 0
        }
    }

    public override fun onDisable() {
        mc.rightClickDelayTimer = 4
    }

    private fun getBowCharge(): Int {
        if (randomVariation == 0) {
            randomVariation = if (chargeVariation.value == 0) 0 else (0..chargeVariation.value).random()
        }
        return bowCharge.value + randomVariation
    }

    private fun passItemCheck(item: Item): Boolean {
        return item !is ItemAir && ((allItems.value && item !is ItemBlock)
                || (blocks.value && item is ItemBlock)
                || (expBottles.value && item is ItemExpBottle)
                || (endCrystals.value && item is ItemEndCrystal)
                || (fireworks.value && item is ItemFirework))
    }
}