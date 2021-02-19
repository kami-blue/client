package org.kamiblue.client.module.modules.player

import net.minecraft.init.Items
import net.minecraft.item.*
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.mixin.extension.rightClickDelayTimer
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.event.listener.listener

/**
 * Bowspam code from https://github.com/seppukudevelopment/seppuku/blob/5586365/src/main/java/me/rigamortis/seppuku/impl/module/combat/FastBowModule.java
 */
internal object FastUse : Module(
    name = "FastUse",
    category = Category.PLAYER,
    description = "Use items faster"
) {
    private val delay = setting("Delay", 0, 0..10, 1)
    private val blocks = setting("Blocks", false)
    private val allItems = setting("All Items", false)
    private val expBottles = setting("Exp Bottles", true, { !allItems.value })
    private val endCrystals = setting("End Crystals", true, { !allItems.value })
    private val fireworks = setting("Fireworks", false, { !allItems.value })
    private val bow = setting("Bow", true, { !allItems.value })
    private val chargeSetting = setting("Bow Charge", 3, 0..20, 1, { allItems.value || bow.value })
    private val chargeVariation = setting("Charge Variation", 5, 0..20, 1, { allItems.value || bow.value })

    private var lastUsedHand = EnumHand.MAIN_HAND
    private var randomVariation = 0
    private var tickCount = 0

    val bowCharge get() = if (isEnabled && (allItems.value || bow.value)) 72000.0 - (chargeSetting.value.toDouble() + chargeVariation.value / 2.0) else null

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.END || player.isSpectator) return@safeListener

            if ((allItems.value || bow.value) && player.isHandActive && (player.activeItemStack.item == Items.BOW) && player.itemInUseMaxCount >= getBowCharge()) {
                randomVariation = 0
                connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, player.horizontalFacing))
                connection.sendPacket(CPacketPlayerTryUseItem(player.activeHand))
                player.stopActiveHand()
            }

            if (delay.value > 0) {
                if (tickCount <= 0) {
                    tickCount = delay.value
                } else {
                    tickCount--
                    return@safeListener
                }
            }

            if (passItemCheck(player.getHeldItem(lastUsedHand).item)) {
                mc.rightClickDelayTimer = 0
            }
        }

        listener<PacketEvent.PostSend> {
            if (it.packet is CPacketPlayerTryUseItem) lastUsedHand = it.packet.hand
            if (it.packet is CPacketPlayerTryUseItemOnBlock) lastUsedHand = it.packet.hand
        }
    }

    private fun getBowCharge(): Int {
        if (randomVariation == 0) {
            randomVariation = if (chargeVariation.value == 0) 0 else (0..chargeVariation.value).random()
        }
        return chargeSetting.value + randomVariation
    }

    private fun passItemCheck(item: Item): Boolean {
        return item !is ItemAir
            && (allItems.value && item !is ItemBlock
            || blocks.value && item is ItemBlock
            || expBottles.value && item is ItemExpBottle
            || endCrystals.value && item is ItemEndCrystal
            || fireworks.value && item is ItemFirework)
    }
}