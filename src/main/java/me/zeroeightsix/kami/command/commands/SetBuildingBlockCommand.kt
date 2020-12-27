package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.module.modules.player.InventoryManager
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.block.Block.getBlockFromItem
import net.minecraft.block.BlockAir
import net.minecraft.item.Item.getIdFromItem

// TODO: Remove once GUI has Block settings
object SetBuildingBlockCommand : ClientCommand(
    name = "setbuildingblock",
    description = "Set the default building block"
) {
    init {
        executeSafe {
            val heldItem = player.inventory.getCurrentItem()
            when {
                heldItem.isEmpty -> {
                    InventoryManager.buildingBlockID.value = 0
                    MessageSendHelper.sendChatMessage("Building block has been reset")
                }
                getBlockFromItem(heldItem.item) !is BlockAir -> {
                    InventoryManager.buildingBlockID.value = getIdFromItem(heldItem.item)
                    MessageSendHelper.sendChatMessage("Building block has been set to ${heldItem.displayName}")
                }
                else -> {
                    MessageSendHelper.sendChatMessage("You are not holding a valid block")
                }
            }
        }
    }
}