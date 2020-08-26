package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.KamiMod
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
        val inventoryManger = KamiMod.MODULE_MANAGER.getModuleT(InventoryManager::class.java) ?: return
        val heldItem = mc.player.inventory.getCurrentItem()
        when {
            heldItem.isEmpty -> {
                inventoryManger.buildingBlockID.value = 0
                sendChatMessage("Building block has been reset")
            }
            getBlockFromItem(heldItem.item) !is BlockAir -> {
                inventoryManger.buildingBlockID.value = getIdFromItem(heldItem.item)
                val blockName = heldItem.displayName
                sendChatMessage("Building block has been set to $blockName")
            }
            else -> {
                sendChatMessage("You are not holding a valid block")
            }
        }
    }
}