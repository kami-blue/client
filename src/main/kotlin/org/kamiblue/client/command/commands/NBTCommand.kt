package org.kamiblue.client.command.commands

import net.minecraft.item.ItemStack
import net.minecraft.nbt.JsonToNBT
import net.minecraft.nbt.NBTTagCompound
import org.kamiblue.client.command.ClientCommand
import org.kamiblue.client.event.SafeExecuteEvent
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.text.formatValue
import org.kamiblue.commons.utils.SystemUtils

object NBTCommand : ClientCommand(
    name = "nbt",
    description = "Get, copy, paste, clear NBT for item held in main hand"
) {

    init {
        literal("get") {
            executeSafe("Print NBT data to chat") {
                val itemStack = getHelpItemStack() ?: return@executeSafe
                val nbtTag = getNbtTag(itemStack) ?: return@executeSafe

                MessageSendHelper.sendChatMessage("NBT tags on item ${formatValue(itemStack.displayName)}")
                MessageSendHelper.sendRawChatMessage(nbtTag.toString())
            }
        }

        literal("copy") {
            executeSafe("Copy NBT data to clipboard") {
                val itemStack = getHelpItemStack() ?: return@executeSafe
                val nbtTag = getNbtTag(itemStack) ?: return@executeSafe

                SystemUtils.setClipboard(nbtTag.toString())

                MessageSendHelper.sendChatMessage("Copied NBT tags from item ${formatValue(itemStack.displayName)}")
            }
        }

        literal("paste") {
            executeSafe("Paste NBT data from clipboard to held item") {
                val itemStack = getHelpItemStack() ?: return@executeSafe

                val nbtTag: NBTTagCompound

                try {
                    val clipboard = SystemUtils.getClipboard() ?: throw IllegalStateException()
                    nbtTag = JsonToNBT.getTagFromJson(clipboard)
                } catch (e: Exception) {
                    MessageSendHelper.sendErrorMessage("Invalid NBT data in clipboard")
                    return@executeSafe
                }

                itemStack.tagCompound = nbtTag
                MessageSendHelper.sendChatMessage("Pasted NBT tags to item ${formatValue(itemStack.displayName)}")
            }
        }

        literal("clear", "wipe") {
            executeSafe("Wipe NBT data from held item") {
                val itemStack = getHelpItemStack() ?: return@executeSafe
                getNbtTag(itemStack) ?: return@executeSafe // Make sure it has a NBT tag before

                itemStack.tagCompound = NBTTagCompound()
            }
        }
    }

    private fun getNbtTag(itemStack: ItemStack): NBTTagCompound? {
        val nbtTag = itemStack.tagCompound

        if (nbtTag == null) {
            MessageSendHelper.sendChatMessage("Item ${formatValue(itemStack.displayName)} doesn't have NBT tag!")
        }

        return nbtTag
    }

    private fun SafeExecuteEvent.getHelpItemStack(): ItemStack? {
        val itemStack = player.inventory?.getCurrentItem()

        if (itemStack == null || itemStack.isEmpty) {
            MessageSendHelper.sendChatMessage("Not holding an item!")
            return null
        }

        return itemStack
    }

}