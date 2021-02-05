package org.kamiblue.client.command.commands

import org.kamiblue.client.command.ClientCommand
import org.kamiblue.client.module.modules.player.InventoryManager
import org.kamiblue.client.util.text.MessageSendHelper

// TODO: Remove once GUI has List
object EjectCommand : ClientCommand(
    name = "eject",
    description = "Modify AutoEject item list"
) {
    init {
        literal("add", "+") {
            item("item") { itemArg ->
                execute("Add an item to the eject list") {
                    val itemName = itemArg.value.registryName!!.toString()

                    if (InventoryManager.ejectList.contains(itemName)) {
                        MessageSendHelper.sendErrorMessage("&c$itemName is already added to eject list")
                    } else {
                        InventoryManager.ejectList.add(itemName)
                        MessageSendHelper.sendChatMessage("$itemName has been added to the eject list")
                    }
                }
            }
        }

        literal("del", "remove", "-") {
            item("item") { itemArg ->
                execute("Remove an item from the eject list") {
                    val itemName = itemArg.value.registryName!!.toString()

                    if (!InventoryManager.ejectList.contains(itemName)) {
                        MessageSendHelper.sendErrorMessage("&c$itemName is not in the eject list")
                    } else {
                        InventoryManager.ejectList.remove(itemName)
                        MessageSendHelper.sendChatMessage("$itemName has been removed from the eject list")
                    }
                }
            }
        }

        literal("list") {
            execute("List items in the eject list") {
                var list = InventoryManager.ejectList.joinToString()
                if (list.isEmpty()) list = "&cNo items!"
                MessageSendHelper.sendChatMessage("AutoEject item list:\n$list")
            }
        }

        literal("reset", "default") {
            execute("Reset the eject list to defaults") {
                InventoryManager.ejectList.resetValue()
                MessageSendHelper.sendChatMessage("Reset eject list to defaults")
            }
        }

        literal("clear") {
            execute("Set the eject list to nothing") {
                InventoryManager.ejectList.clear()
                MessageSendHelper.sendChatMessage("Reset eject list was cleared")
            }
        }
    }
}