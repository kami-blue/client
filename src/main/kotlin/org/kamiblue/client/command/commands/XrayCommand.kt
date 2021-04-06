package org.kamiblue.client.command.commands

import org.kamiblue.client.command.ClientCommand
import org.kamiblue.client.module.modules.render.Xray
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.text.formatValue

// TODO: Remove once GUI has List
object XrayCommand : ClientCommand(
    name = "Xray",
    description = "Manage visible xray blocks"
) {

    init {
        literal("add", "+") {
            block("block") { blockArg ->
                execute("Add a block to visible xray list") {
                    val blockName = blockArg.value.registryName.toString()

                    addBlock(blockName)
                }
            }
        }

        literal("remove", "-") {
            block("block") { blockArg ->
                execute("Remove a block from visible xray list") {
                    val blockName = blockArg.value.registryName.toString()

                    if (!Xray.visibleList.remove(blockName)) {
                        MessageSendHelper.sendErrorMessage("You do not have ${formatValue(blockName)} added to xray visible block list")
                    } else {
                        MessageSendHelper.sendChatMessage("Removed ${formatValue(blockName)} from xray visible block list")
                    }
                }
            }
        }

        literal("set", "=") {
            block("block") { blockArg ->
                execute("Set the xray list to one block") {
                    val blockName = blockArg.value.registryName.toString()

                    Xray.visibleList.clear()
                    Xray.visibleList.add(blockName)
                    MessageSendHelper.sendChatMessage("Set the xray block list to ${formatValue(blockName)}")
                }
            }
        }

        literal("reset", "default") {
            execute("Reset the visible block list to defaults") {
                Xray.visibleList.resetValue()
                MessageSendHelper.sendChatMessage("Reset the visible block list to defaults")
            }
        }

        literal("list") {
            execute("Print visible list") {
                MessageSendHelper.sendChatMessage(Xray.visibleList.joinToString())
            }
        }

        literal("clear") {
            execute("Set the visible list to nothing") {
                Xray.visibleList.clear()
                MessageSendHelper.sendChatMessage("Cleared the visible block list")
            }
        }
    }

    private fun addBlock(blockName: String) {
        if (!Xray.visibleList.add(blockName)) {
            MessageSendHelper.sendErrorMessage("${formatValue(blockName)} is already added to the visible block list")
        } else {
            MessageSendHelper.sendChatMessage("${formatValue(blockName)} has been added to the visible block list")
        }
    }
}