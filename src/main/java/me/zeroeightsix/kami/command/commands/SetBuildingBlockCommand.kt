package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.module.modules.player.InventoryManager
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage
import net.minecraft.block.Block.getBlockFromItem
import net.minecraft.block.BlockAir
import net.minecraft.item.Item.getIdFromItem

/**
 * @author Xiaro
 */
class SetBuildingBlockCommand : Command("setbuildingblock", null) {

    override fun call(args: Array<out String>?) {
        if (mc.player == null || mc.player.isSpectator) return
        val heldItem = mc.player.inventory.getCurrentItem()
        when {
            heldItem.isEmpty -> {
                InventoryManager.buildingBlockID.value = 0
                sendChatMessage("Building block has been reset")
            }
            getBlockFromItem(heldItem.item) !is BlockAir -> {
                InventoryManager.buildingBlockID.value = getIdFromItem(heldItem.item)
                val blockName = heldItem.displayName
                sendChatMessage("Building block has been set to $blockName")
            }
            else -> {
                sendChatMessage("You are not holding a valid block")
            }
        }
    }
}