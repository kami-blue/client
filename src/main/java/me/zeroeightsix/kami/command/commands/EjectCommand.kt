package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.syntax.ChunkBuilder
import me.zeroeightsix.kami.command.syntax.parsers.EnumParser
import me.zeroeightsix.kami.module.modules.player.InventoryManager
import me.zeroeightsix.kami.util.MessageSendHelper
import net.minecraft.block.Block

/**
 * Created by 20kdc on 17/02/2020.
 * Updated by dominikaaaa on 17/02/20
 * Modified for use with search module by wnuke on 20/04/2020
 */
class EjectCommand : Command("eject", ChunkBuilder().append("command", true, EnumParser(arrayOf("help", "+item", "-item", "=item", "list", "defaults", "clear"))).build()) {
    override fun call(args: Array<String>) {
        val im = KamiMod.MODULE_MANAGER.getModuleT(InventoryManager::class.java)
        if (im == null) {
            MessageSendHelper.sendErrorMessage("&cThe module is not available for some reason. Make sure the name you're calling is correct and that you have the module installed!!")
            return
        }
        if (!im.isEnabled) {
            MessageSendHelper.sendWarningMessage("&6Warning: The " + im.name + " module is not enabled!")
            MessageSendHelper.sendWarningMessage("These commands will still have effect, but will not visibly do anything.")
        }
        for (s in args) {
            if (s == null) continue
            if (s.equals("help", ignoreCase = true)) {
                MessageSendHelper.sendChatMessage("""Available options: 
  +item: Adds item to the list
  -item: Removes item from the list
  =item: Changes the list to only contain item
  list: Prints the list of selected items
  defaults: Resets the list to the default list
  clear: Removes all items from the ${im.name} item list""")
            } else if (s.equals("clear", ignoreCase = true)) {
                im.extClear()
                MessageSendHelper.sendWarningMessage("Cleared the " + im.name + " item list")
            } else if (s.equals("defaults", ignoreCase = true)) {
                im.extDefaults()
                MessageSendHelper.sendChatMessage("Reset the " + im.name + " item list to default")
            } else if (s.equals("list", ignoreCase = true)) {
                MessageSendHelper.sendChatMessage("""
    
    ${im.extGet()}
    """.trimIndent())
            } else if (s.startsWith("=")) {
                val sT = s.replace("=", "")
                im.extSet(sT)
                MessageSendHelper.sendChatMessage("Set the " + im.name + " block list to " + sT)
            } else if (s.startsWith("+") || s.startsWith("-")) {
                var name = s.substring(1)
                if (s.substring(1).startsWith("?")) name = s.substring(2)
                val b = Block.getBlockFromName(name)
                if (b == null) {
                    MessageSendHelper.sendChatMessage("&cInvalid block name <$name>")
                } else {
                    if (s.startsWith("+")) {
                        if (s.substring(1).startsWith("?")) {
                            MessageSendHelper.sendWarningMessage("Added <" + name + "> to the " + im.name + " block list")
                            im.extAdd(name)
                        } else {
                            MessageSendHelper.sendChatMessage("Added <" + name + "> to the " + im.name + " block list")
                            im.extAdd(name)
                        }
                    } else {
                        MessageSendHelper.sendChatMessage("Removed <" + name + "> from the " + im.name + " block list")
                        im.extRemove(name)
                    }
                }
            } else {
                MessageSendHelper.sendChatMessage("&cInvalid subcommand <$s>")
            }
        }
    }

    init {
        setDescription("Allows you to add or remove item from the &fInventoryManager &7module AutoEject")
    }
}