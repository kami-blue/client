package org.kamiblue.client.command.commands

import net.minecraft.block.BlockAir
import org.kamiblue.client.command.ClientCommand
import org.kamiblue.client.module.modules.player.InventoryManager
import org.kamiblue.client.util.items.block
import org.kamiblue.client.util.items.id
import org.kamiblue.client.util.text.MessageSendHelper

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
                    InventoryManager.buildingBlockID = 0
                    MessageSendHelper.sendChatMessage("Building block has been reset")
                }
                heldItem.item.block !is BlockAir -> {
                    InventoryManager.buildingBlockID = heldItem.item.id
                    MessageSendHelper.sendChatMessage("Building block has been set to ${heldItem.displayName}")
                }
                else -> {
                    MessageSendHelper.sendChatMessage("You are not holding a valid block")
                }
            }
        }
    }
}