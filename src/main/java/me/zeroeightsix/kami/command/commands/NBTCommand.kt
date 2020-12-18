package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.command.CommandManager.colorFormatValue
import me.zeroeightsix.kami.command.SafeExecuteEvent
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

object NBTCommand : ClientCommand(
    name = "nbt",
    description = "Get, copy, paste, clear NBT for item held in main hand"
) {

    private var copiedNbtTag: NBTTagCompound? = null

    init {
        literal("get") {
            executeSafe {
                val itemStack = getHelpItemStack() ?: return@executeSafe
                val nbtTag = getNbtTag(itemStack) ?: return@executeSafe

                MessageSendHelper.sendChatMessage("NBT tags on item ${itemStack.displayName.colorFormatValue}")
                MessageSendHelper.sendRawChatMessage(nbtTag.toString())
            }
        }

        literal("copy") {
            executeSafe {
                val itemStack = getHelpItemStack() ?: return@executeSafe
                val nbtTag = getNbtTag(itemStack) ?: return@executeSafe

                copiedNbtTag = nbtTag
                MessageSendHelper.sendChatMessage("Copied NBT tags from item ${itemStack.displayName.colorFormatValue}")
            }
        }

        literal("paste") {
            executeSafe {
                val itemStack = getHelpItemStack() ?: return@executeSafe
                val nbtTag = copiedNbtTag?: run {
                    MessageSendHelper.sendChatMessage("No copied NBT tags!")
                    return@executeSafe
                }

                itemStack.tagCompound = nbtTag
                MessageSendHelper.sendChatMessage("Pasted NBT tags to item ${itemStack.displayName.colorFormatValue}")
            }
        }

        literal("clear", "wipe") {
            executeSafe {
                val itemStack = getHelpItemStack() ?: return@executeSafe
                getNbtTag(itemStack) ?: return@executeSafe // Make sure it has a NBT tag before

                itemStack.tagCompound = NBTTagCompound()
            }
        }
    }

    private fun getNbtTag(itemStack: ItemStack): NBTTagCompound? {
        val nbtTag = itemStack.tagCompound

        if (nbtTag == null) {
            MessageSendHelper.sendChatMessage("Item ${itemStack.displayName.colorFormatValue} doesn't have NBT tag!")
            return null
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