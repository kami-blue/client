package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.command.CommandManager.colorFormatValue
import me.zeroeightsix.kami.module.modules.render.Search
import me.zeroeightsix.kami.util.text.MessageSendHelper

// TODO: Remove once GUI has List
object SearchCommand : ClientCommand(
    name = "search",
    description = "Manage search blocks"
) {
    private val warningBlocks = arrayOf("minecraft:grass", "minecraft:end_stone", "minecraft:lava", "minecraft:bedrock", "minecraft:netherrack", "minecraft:dirt", "minecraft:water", "minecraft:stone")

    init {
        literal("add", "+") {
            block("block") { blockArg ->
                literal("force") {
                    execute("Force add a block to search list") {
                        val blockName = blockArg.value.registryName.toString()
                        addBlock(blockName)
                    }

                }

                execute("Add a block to search list") {
                    val blockName = blockArg.value.registryName.toString()

                    if (warningBlocks.contains(blockName)) {
                        MessageSendHelper.sendWarningMessage("Your world contains lots of ${blockName.colorFormatValue}" +
                            ", it might cause extreme lag to add it." +
                            " If you are sure you want to add it run " + "$prefixName add force $blockName".colorFormatValue)
                    } else {
                        addBlock(blockName)
                    }
                }
            }
        }

        literal("remove", "-") {
            block("block") { blockArg ->
                execute("Remove a block from search list") {
                    val blockName = blockArg.value.registryName.toString()

                    if (!Search.searchArrayList.contains(blockName)) {
                        MessageSendHelper.sendErrorMessage("You do not have ${blockName.colorFormatValue} added to search block list")
                    } else {
                        Search.searchRemove(blockName)
                        MessageSendHelper.sendChatMessage("Removed ${blockName.colorFormatValue} from search block list")
                    }
                }
            }
        }

        literal("set", "=") {
            block("block") { blockArg ->
                execute("Set the search list to one block") {
                    val blockName = blockArg.value.registryName.toString()

                    Search.searchSet(blockName)
                    MessageSendHelper.sendChatMessage("Set the search block list to ${blockName.colorFormatValue}")
                }
            }
        }

        literal("reset", "default") {
            execute("Reset the search list to defaults") {
                Search.searchDefault()
                MessageSendHelper.sendChatMessage("Reset the search block list to defaults")
            }
        }

        literal("clear") {
            execute("Set the search list to nothing") {
                Search.searchClear()
                MessageSendHelper.sendChatMessage("Cleared the ${Search.name.value} block list")
            }
        }

        literal("override") {
            execute("Override the Intel Integrated GPU check") {
                Search.overrideWarning.value = true
                MessageSendHelper.sendWarningMessage("Override for Intel Integrated GPUs enabled!")
            }
        }
    }

    private fun addBlock(blockName: String) {
        return when {
            blockName == "minecraft:air" -> {
                MessageSendHelper.sendChatMessage("You can't add ${blockName.colorFormatValue} to the search block list")
            }

            Search.searchArrayList.contains(blockName) -> {
                MessageSendHelper.sendErrorMessage("${blockName.colorFormatValue} is already added to the search block list")
            }

            else -> {
                Search.searchAdd(blockName)
                MessageSendHelper.sendChatMessage("${blockName.colorFormatValue} has been added to the search block list")
            }
        }
    }
}