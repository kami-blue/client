package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.syntax.ChunkBuilder
import me.zeroeightsix.kami.command.syntax.parsers.EnumParser
import me.zeroeightsix.kami.module.modules.render.Search
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.block.Block

/**
 * Created by 20kdc on 17/02/2020.
 * Updated by l1ving on 17/02/20
 * Modified for use with search module by wnuke on 20/04/2020
 * Updated by Xiaro on 23/07/20
 */
class SearchCommand : Command("search", ChunkBuilder().append("command", true, EnumParser(arrayOf("+block", "-block", "=block", "list", "default", "clear", "help"))).build()) {
    private val bannedBlocks = arrayOf("minecraft:air", "minecraft:netherrack", "minecraft:dirt", "minecraft:water", "minecraft:stone")
    private val warningBlocks = arrayOf("minecraft:grass", "minecraft:end_stone", "minecraft:lava", "minecraft:bedrock")

    override fun call(args: Array<String?>) {
        if (Search.isDisabled) {
            MessageSendHelper.sendWarningMessage("&6Warning: The ${Search.name} module is not enabled!")
            MessageSendHelper.sendWarningMessage("These commands will still have effect, but will not visibly do anything.")
        }
        when {
            args[0] == null || args[0].equals("help", ignoreCase = true) -> {
                val p = getCommandPrefix()
                MessageSendHelper.sendChatMessage("Search command help\n\n" +
                        "    &7+block&f <name>\n" +
                        "        &7${p}search +cobblestone\n\n" +
                        "    &7-block&f <name>\n" +
                        "        &7${p}search -cobblestone\n\n" +
                        "    &7=block&f <name>\n" +
                        "        &7${p}search =portal\n\n" +
                        "    &7list&f\n" +
                        "        &7${p}search list\n\n" +
                        "    &7default&f\n" +
                        "        &7${p}search default\n\n" +
                        "    &7clear&f\n" +
                        "        &7${p}search clear")
            }
            args[0]!!.startsWith("+", true) -> {
                val name = args[0]!!.replace("+", "").replace("?", "")
                if (Block.getBlockFromName(name) == null) {
                    MessageSendHelper.sendChatMessage("&cInvalid block name <$name>")
                } else {
                    val blockName = Block.getBlockFromName(name)!!.registryName.toString()
                    when {
                        bannedBlocks.contains(blockName) -> {
                            MessageSendHelper.sendChatMessage("You can't add <$blockName> to the ${Search.name} block list")
                        }
                        warningBlocks.contains(blockName) -> {
                            if (args[0]!!.replace("+", "").startsWith("?", true)) {
                                Search.searchList.value.add(blockName)
                                MessageSendHelper.sendChatMessage("<$blockName> has been added to the ${Search.name} block list")
                            } else {
                                MessageSendHelper.sendWarningMessage("Your world contains lots of <$blockName>, it might cause extreme lag to add it." +
                                        " If you are sure you want to add it run &7${commandPrefix.value}search +?$name")
                            }
                        }
                        else -> {
                            if (Search.searchList.value.add(blockName)) {
                                MessageSendHelper.sendChatMessage("<$blockName> has been added to the ${Search.name} block list")
                            } else {
                                MessageSendHelper.sendChatMessage("&c<$blockName> already exist")
                            }
                        }
                    }
                }
            }
            args[0]!!.startsWith("-", true) -> {
                val name = args[0]!!.replace("-", "")
                if (Block.getBlockFromName(name) == null) {
                    MessageSendHelper.sendChatMessage("&cInvalid block name/id <$name>")
                } else {
                    val blockName = Block.getBlockFromName(name)!!.registryName.toString()
                    if (Search.searchList.value.remove(blockName)) {
                        MessageSendHelper.sendChatMessage("<$blockName> has been removed from the ${Search.name} block list")
                    } else {
                        MessageSendHelper.sendChatMessage("&c<$blockName> doesn't exist")
                    }
                }
            }
            args[0]!!.startsWith("=", true) -> {
                val name = args[0]!!.replace("=", "").replace("?", "")
                if (Block.getBlockFromName(name) == null) {
                    MessageSendHelper.sendChatMessage("&cInvalid block name/id <$name>")
                } else {
                    val blockName = Block.getBlockFromName(name)!!.registryName.toString()
                    when {
                        bannedBlocks.contains(blockName) -> {
                            MessageSendHelper.sendChatMessage("You can't set ${Search.name} block list to <$blockName>")
                        }
                        warningBlocks.contains(blockName) -> {
                            if (args[0]!!.replace("+", "").startsWith("?", true)) {
                                Search.searchList.value.clear()
                                Search.searchList.value.add(blockName)
                                MessageSendHelper.sendChatMessage("${Search.name} block list has been set to <$blockName>")
                            } else {
                                MessageSendHelper.sendWarningMessage("Your world contains lots of <$blockName>, it might cause extreme lag to set to it." +
                                        " If you are sure you want to set to it run &7${commandPrefix.value}search +?$name")
                            }
                        }
                        else -> {
                            Search.searchList.value.clear()
                            Search.searchList.value.add(blockName)
                            MessageSendHelper.sendChatMessage("${Search.name} block list has been set to <$blockName>")
                        }
                    }
                }
            }
            args[0].equals("list", true) -> {
                MessageSendHelper.sendChatMessage(Search.searchList.value.joinToString())
            }
            args[0].equals("default", true) -> {
                Search.searchList.resetValue()
                MessageSendHelper.sendChatMessage("Reset the ${Search.name} block list to default")
            }
            args[0].equals("clear", true) -> {
                Search.searchList.value.clear()
                MessageSendHelper.sendChatMessage("Cleared the ${Search.name} block list")
            }
            args[0].equals("override", true) -> {
                Search.overrideWarning.value = true
                MessageSendHelper.sendWarningMessage("${Search.chatName} Override for Intel Integrated GPUs enabled!")
            }
            else -> {
                MessageSendHelper.sendChatMessage("&cInvalid subcommand ${args[0]}")
            }
        }
    }

    init {
        setDescription("Allows you to add or remove blocks from the &fSearch &7module")
    }
}