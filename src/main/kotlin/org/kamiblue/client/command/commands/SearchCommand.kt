package org.kamiblue.client.command.commands

import org.kamiblue.client.command.ClientCommand
import org.kamiblue.client.module.modules.render.Search
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.text.formatValue

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
                        MessageSendHelper.sendWarningMessage("Your world contains lots of ${formatValue(blockName)}, " +
                            "it might cause extreme lag to add it. " +
                            "If you are sure you want to add it run ${formatValue("$prefixName add force $blockName")}"
                        )
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

                    if (!Search.searchList.remove(blockName)) {
                        MessageSendHelper.sendErrorMessage("You do not have ${formatValue(blockName)} added to search block list")
                    } else {
                        MessageSendHelper.sendChatMessage("Removed ${formatValue(blockName)} from search block list")
                    }
                }
            }
        }

        literal("set", "=") {
            block("block") { blockArg ->
                execute("Set the search list to one block") {
                    val blockName = blockArg.value.registryName.toString()

                    Search.searchList.clear()
                    Search.searchList.add(blockName)
                    MessageSendHelper.sendChatMessage("Set the search block list to ${formatValue(blockName)}")
                }
            }
        }

        literal("reset", "default") {
            execute("Reset the search list to defaults") {
                Search.searchList.resetValue()
                MessageSendHelper.sendChatMessage("Reset the search block list to defaults")
            }
        }

        literal("list") {
            execute("Print search list") {
                MessageSendHelper.sendChatMessage(Search.searchList.joinToString())
            }
        }

        literal("clear") {
            execute("Set the search list to nothing") {
                Search.searchList.clear()
                MessageSendHelper.sendChatMessage("Cleared the search block list")
            }
        }

        literal("override") {
            execute("Override the Intel Integrated GPU check") {
                Search.overrideWarning = true
                MessageSendHelper.sendWarningMessage("Override for Intel Integrated GPUs enabled!")
            }
        }
    }

    private fun addBlock(blockName: String) {
        if (blockName == "minecraft:air") {
            MessageSendHelper.sendChatMessage("You can't add ${formatValue(blockName)} to the search block list")
            return
        }

        if (!Search.searchList.add(blockName)) {
            MessageSendHelper.sendErrorMessage("${formatValue(blockName)} is already added to the search block list")
        } else {
            MessageSendHelper.sendChatMessage("${formatValue(blockName)} has been added to the search block list")
        }
    }
}