package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.init.Items
import net.minecraft.item.*
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.util.math.BlockPos

/**
 * Bowspam code from https://github.com/seppukudevelopment/seppuku/blob/5586365/src/main/java/me/rigamortis/seppuku/impl/module/combat/FastBowModule.java
 */
@Module.Info(
        name = "FastUse",
        category = Module.Category.PLAYER,
        description = "Use items faster"
)
object FastUse : Module() {
    private val delay = register(Settings.integerBuilder("Delay").withMinimum(0).withMaximum(20).withValue(0).build())
    private val blocks = register(Settings.b("Blocks", false))
    private val allItems = register(Settings.b("AllItems", false))
    private val expBottles = register(Settings.booleanBuilder().withName("ExpBottles").withValue(true).withVisibility { !allItems.value }.build())
    private val endCrystals = register(Settings.booleanBuilder().withName("EndCrystals").withValue(true).withVisibility { !allItems.value }.build())
    private val fireworks = register(Settings.booleanBuilder().withName("Fireworks").withValue(false).withVisibility { !allItems.value }.build())
    private val bow = register(Settings.booleanBuilder().withName("Bow").withValue(true).withVisibility { !allItems.value }.build())
    private val chargeSetting = register(Settings.integerBuilder("BowCharge").withValue(3).withRange(0, 20).withVisibility { allItems.value || bow.value }.build())
    private val chargeVariation = register(Settings.integerBuilder("ChargeVariation").withValue(5).withRange(0, 20).withVisibility { allItems.value || bow.value }.build())

    private var randomVariation = 0
    private var time = 0

    val bowCharge get() = if (isEnabled && (allItems.value || bow.value)) 72000.0 - (chargeSetting.value.toDouble() + chargeVariation.value / 2.0) else null

    override fun onUpdate(event: SafeTickEvent) {
        if (mc.player.isSpectator) return

        @Suppress("SENSELESS_COMPARISON") // IDE meme
        if ((allItems.value || bow.value) && mc.player.activeHand != null && (mc.player.getHeldItem(mc.player.activeHand).getItem() == Items.BOW) && mc.player.isHandActive && mc.player.itemInUseMaxCount >= getBowCharge()) {
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
        return chargeSetting.value + randomVariation
    }

    private fun passItemCheck(item: Item): Boolean {
        return item !is ItemAir && ((allItems.value && item !is ItemBlock)
                || (blocks.value && item is ItemBlock)
                || (expBottles.value && item is ItemExpBottle)
                || (endCrystals.value && item is ItemEndCrystal)
                || (fireworks.value && item is ItemFirework))
    }
}