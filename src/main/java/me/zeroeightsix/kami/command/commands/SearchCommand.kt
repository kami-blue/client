package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.syntax.ChunkBuilder
import me.zeroeightsix.kami.command.syntax.parsers.EnumParser
import me.zeroeightsix.kami.module.modules.render.Search
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendErrorMessage
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendWarningMessage
import net.minecraft.block.Block

/**
 * Created by 20kdc on 17/02/2020.
 * Updated by dominikaaaa on 17/02/20
 * Modified for use with search module by wnuke on 20/04/2020
 * Updated by Xiaro on 23/07/20
 */
class SearchCommand : Command("search", ChunkBuilder().append("command", true, EnumParser(arrayOf("+block", "-block", "=block", "list", "default", "clear", "help"))).build()) {
    private val ESP_BANNED_BLOCKS = arrayOf("minecraft:air", "minecraft:netherrack", "minecraft:dirt", "minecraft:water", "minecraft:stone")
    private val WARNING_BLOCKS = arrayOf("minecraft:grass", "minecraft:end_stone", "minecraft:lava", "minecraft:bedrock")

    override fun call(args: Array<String?>) {
        val search = KamiMod.MODULE_MANAGER.getModuleT(Search::class.java)
        if (search == null) {
            sendErrorMessage("&cThe module is not available for some reason. Make sure the name you're calling is correct and that you have the module installed!!")
            return
        }
        if (search.isDisabled) {
            sendWarningMessage("&6Warning: The ${search.name} module is not enabled!")
            sendWarningMessage("These commands will still have effect, but will not visibly do anything.")
        }
        when {
            args[0] == null || args[0].equals("help", ignoreCase = true) -> {
                val p = getCommandPrefix()
                sendChatMessage("Search command help\n\n" +
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
                    sendChatMessage("&cInvalid block name <$name>")
                } else {
                    val blockName = Block.getBlockFromName(name)!!.registryName.toString()
                    when {
                        ESP_BANNED_BLOCKS.contains(blockName) -> {
                            sendChatMessage("You can't add <$blockName> to the ${search.name} block list")
                        }
                        WARNING_BLOCKS.contains(blockName) -> {
                            if (args[0]!!.replace("+", "").startsWith("?", true)) {
                                search.searchAdd(blockName)
                                sendChatMessage("<$blockName> has been added to the ${search.name} block list")
                            } else {
                                sendWarningMessage("Your world contains lots of <$blockName>, it might cause extreme lag to add it." +
                                        " If you are sure you want to add it run &7${commandPrefix.value}search +?$name")
                            }
                        }
                        search.searchArrayList.contains(blockName) -> {
                            sendChatMessage("&c<$blockName> already exist")
                        }
                        else -> {
                            search.searchAdd(blockName)
                            sendChatMessage("<$blockName> has been added to the ${search.name} block list")
                        }
                    }
                }
            }
            args[0]!!.startsWith("-", true) -> {
                val name = args[0]!!.replace("-", "")
                if (Block.getBlockFromName(name) == null) {
                    sendChatMessage("&cInvalid block name/id <$name>")
                } else {
                    val blockName = Block.getBlockFromName(name)!!.registryName.toString()
                    if (!search.searchArrayList.contains(blockName)) {
                        sendChatMessage("&c<$blockName> doesn't exist")
                    } else {
                        search.searchRemove(blockName)
                        sendChatMessage("<$blockName> has been removed from the ${search.name} block list")
                    }
                }
            }
            args[0]!!.startsWith("=", true) -> {
                val name = args[0]!!.replace("=", "").replace("?", "")
                if (Block.getBlockFromName(name) == null) {
                    sendChatMessage("&cInvalid block name/id <$name>")
                } else {
                    val blockName = Block.getBlockFromName(name)!!.registryName.toString()
                    when {
                        ESP_BANNED_BLOCKS.contains(blockName) -> {
                            sendChatMessage("You can't set ${search.name} block list to <$blockName>")
                        }
                        WARNING_BLOCKS.contains(blockName) -> {
                            if (args[0]!!.replace("+", "").startsWith("?", true)) {
                                search.searchSet(blockName)
                                sendChatMessage("${search.name} block list has been set to <$blockName>")
                            } else {
                                sendWarningMessage("Your world contains lots of <$blockName>, it might cause extreme lag to set to it." +
                                        " If you are sure you want to set to it run &7${commandPrefix.value}search +?$name")
                            }
                        }
                        else -> {
                            search.searchSet(blockName)
                            sendChatMessage("${search.name} block list has been set to <$blockName>")
                        }
                    }
                }
            }
            args[0].equals("list", true) -> {
                sendChatMessage(search.searchGetString())
            }
            args[0].equals("default", true) -> {
                search.searchDefault()
                sendChatMessage("Reset the ${search.name} block list to default")
            }
            args[0].equals("clear", true) -> {
                search.searchClear()
                sendChatMessage("Cleared the ${search.name} block list")
            }
            args[0].equals("override", true) -> {
                search.overrideWarning.value = true
                sendWarningMessage("${search.chatName} Override for Intel Integrated GPUs enabled!")
            }
            else -> {
                sendChatMessage("&cInvalid subcommand ${args[0]}")
            }
        }
    }

    init {
        setDescription("Allows you to add or remove blocks from the &fSearch &7module")
    }
}