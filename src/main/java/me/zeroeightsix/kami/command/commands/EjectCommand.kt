package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.syntax.ChunkBuilder
import me.zeroeightsix.kami.command.syntax.parsers.EnumParser
import me.zeroeightsix.kami.module.modules.player.InventoryManager
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.item.Item

/**
 * Created by 20kdc on 17/02/2020.
 * Updated by l1ving on 17/02/20
 * Modified for use with AutoEject by Xiaro on 19/07/2020
 */
class EjectCommand : Command("eject", ChunkBuilder().append("command", true, EnumParser(arrayOf("help", "+item", "-item", "=item", "list", "default", "clear"))).build()) {
    override fun call(args: Array<String?>) {
        if (!InventoryManager.autoEject.value) {
            MessageSendHelper.sendWarningMessage("&6Warning: AutoEject in ${InventoryManager.name} module is not enabled!")
            MessageSendHelper.sendWarningMessage("These commands will still have effect, but will not visibly do anything.")
        } else if (InventoryManager.isDisabled) {
            MessageSendHelper.sendWarningMessage("&6Warning: The ${InventoryManager.name} module is not enabled!")
            MessageSendHelper.sendWarningMessage("These commands will still have effect, but will not visibly do anything.")
        }
        when {
            args[0] == null || args[0].equals("help", ignoreCase = true) -> {
                MessageSendHelper.sendWarningMessage("""Available options: 
  +item: Adds item to the list
  -item: Removes item from the list
  =item: Changes the list to only contain item
  list: Prints the list of selected items
  defaults: Resets the list to the default list
  clear: Removes all items from the AutoEject item list""")
            }

            args[0]!!.startsWith("+", true) -> {
                val name = args[0]!!.replace("+", "")
                if (Item.getByNameOrId(name) == null) {
                    MessageSendHelper.sendWarningMessage("&cInvalid item name/id $name")
                } else {
                    val itemName = Item.getByNameOrId(name)!!.registryName.toString()
                    if (InventoryManager.ejectList.value.add(itemName)) {
                        MessageSendHelper.sendWarningMessage("$itemName has been added to the AutoEject item list")
                    } else {
                        MessageSendHelper.sendWarningMessage("&c$itemName already exist")
                    }
                }
            }

            args[0]!!.startsWith("-", true) -> {
                val name = args[0]!!.replace("-", "")
                if (Item.getByNameOrId(name) == null) {
                    MessageSendHelper.sendWarningMessage("&cInvalid item name/id $name")
                } else {
                    val itemName = Item.getByNameOrId(name)!!.registryName.toString()
                    if (!InventoryManager.ejectList.value.remove(itemName)) {
                        MessageSendHelper.sendWarningMessage("$itemName has been removed from the AutoEject item list")
                    } else {
                        MessageSendHelper.sendWarningMessage("&c$itemName doesn't exist")
                    }
                }
            }

            args[0]!!.startsWith("=", true) -> {
                val name = args[0]!!.replace("=", "")
                if (Item.getByNameOrId(name) == null) {
                    MessageSendHelper.sendWarningMessage("&cInvalid item name/id $name")
                } else {
                    val itemName = Item.getByNameOrId(name)!!.registryName.toString()
                    InventoryManager.ejectList.value.apply { 
                        clear()
                        add(itemName)
                    }
                    MessageSendHelper.sendWarningMessage("AutoEject item list has been to $itemName")
                }
            }

            args[0].equals("list", true) -> {
                MessageSendHelper.sendWarningMessage(InventoryManager.ejectList.value.joinToString())
            }

            args[0].equals("default", true) -> {
                InventoryManager.ejectList.resetValue()
                MessageSendHelper.sendWarningMessage("Reset the AutoEject item list to default")
            }

            args[0].equals("clear", true) -> {
                InventoryManager.ejectList.value.clear()
                MessageSendHelper.sendWarningMessage("Cleared the AutoEject item list")
            }

            else -> {
                MessageSendHelper.sendWarningMessage("&cInvalid subcommand ${args[0]}")
            }
        }
    }

    init {
        setDescription("Allows you to add or remove item from the &fInventoryManager &7module AutoEject")
    }
}