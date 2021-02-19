package org.kamiblue.client.util.items

import io.netty.buffer.Unpooled
import net.minecraft.block.Block
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.init.Enchantments
import net.minecraft.item.*
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.CPacketCustomPayload
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.mixin.extension.attackDamage

val ItemStack.originalName: String get() = item.getItemStackDisplayName(this)

val Item.id get() = Item.getIdFromItem(this)

val Item.block: Block get() = Block.getBlockFromItem(this)

val Item.isWeapon get() = this is ItemSword || this is ItemAxe

val ItemFood.foodValue get() = this.getHealAmount(ItemStack.EMPTY)

val ItemFood.saturation get() = foodValue * this.getSaturationModifier(ItemStack.EMPTY) * 2f

val ItemStack.attackDamage
    get() = this.item.baseAttackDamage + EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS, this).let {
        if (it > 0) it * 0.5f + 0.5f
        else 0.0f
    }

val Item.baseAttackDamage
    get() = when (this) {
        is ItemSword -> this.attackDamage + 4.0f
        is ItemTool -> this.attackDamage + 1.0f
        else -> 1.0f
    }

fun SafeClientEvent.itemPayload(item: ItemStack, channelIn: String) {
    val buffer = PacketBuffer(Unpooled.buffer())
    buffer.writeItemStack(item)
    player.connection.sendPacket(CPacketCustomPayload(channelIn, buffer))
}
