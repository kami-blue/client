package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.syntax.ChunkBuilder
import me.zeroeightsix.kami.command.syntax.parsers.EnumParser
import me.zeroeightsix.kami.module.modules.player.InventoryManager
import me.zeroeightsix.kami.util.MessageSendHelper.*
import net.minecraft.item.Item

/**
 * Created by 20kdc on 17/02/2020.
 * Updated by dominikaaaa on 17/02/20
 * Modified for use with AutoEject by Xiaro on 19/07/2020
 */
class EjectCommand : Command("eject", ChunkBuilder().append("command", true, EnumParser(arrayOf("help", "+item", "-item", "=item", "list", "default", "clear"))).build()) {
    override fun call(args: Array<String?>) {
        val im = KamiMod.MODULE_MANAGER.getModuleT(InventoryManager::class.java)
        if (im == null) {
            sendErrorMessage("&cThe module is not available for some reason. Make sure the name you're calling is correct and that you have the module installed!!")
            return
        }
        if (!im.autoEject.value) {
            sendWarningMessage("&6Warning: AutoEject in ${im.name} module is not enabled!")
            sendWarningMessage("These commands will still have effect, but will not visibly do anything.")
        } else if (im.isDisabled) {
            sendWarningMessage("&6Warning: The ${im.name} module is not enabled!")
            sendWarningMessage("These commands will still have effect, but will not visibly do anything.")
        }
        when {
            args[0] == null || args[0].equals("help", ignoreCase = true) -> {
                sendChatMessage("""Available options: 
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
                    sendChatMessage("&cInvalid item name/id $name")
                } else {
                    val itemName = Item.getByNameOrId(name)!!.registryName.toString()
                    if (im.ejectArrayList.contains(itemName)) {
                        sendChatMessage("&c$itemName already exist")
                    } else {
                        im.ejectAdd(itemName)
                        sendChatMessage("$itemName has been added to the AutoEject item list")
                    }
                }
            }

            args[0]!!.startsWith("-", true) -> {
                val name = args[0]!!.replace("-", "")
                if (Item.getByNameOrId(name) == null) {
                    sendChatMessage("&cInvalid item name/id $name")
                } else {
                    val itemName = Item.getByNameOrId(name)!!.registryName.toString()
                    if (!im.ejectArrayList.contains(itemName)) {
                        sendChatMessage("&c$itemName doesn't exist")
                    } else {
                        im.ejectRemove(itemName)
                        sendChatMessage("$itemName has been removed from the AutoEject item list")
                    }
                }
            }

            args[0]!!.startsWith("=", true) -> {
                val name = args[0]!!.replace("=", "")
                if (Item.getByNameOrId(name) == null) {
                    sendChatMessage("&cInvalid item name/id $name")
                } else {
                    val itemName = Item.getByNameOrId(name)!!.registryName.toString()
                    im.ejectSet(itemName)
                    sendChatMessage("AutoEject item list has been to $itemName")
                }
            }

            args[0].equals("list", true) -> {
                sendChatMessage(im.ejectGetString())
            }

            args[0].equals("default", true) -> {
                im.ejectDefault()
                sendChatMessage("Reset the AutoEject item list to default")
            }

            args[0].equals("clear", true) -> {
                im.ejectClear()
                sendChatMessage("Cleared the AutoEject item list")
            }

            else -> {
                sendChatMessage("&cInvalid subcommand ${args[0]}")
            }
        }
    }

    init {
        setDescription("Allows you to add or remove item from the &fInventoryManager &7module AutoEject")
    }
}