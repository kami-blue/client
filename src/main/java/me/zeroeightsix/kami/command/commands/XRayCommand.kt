package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.module.modules.render.XRay
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.text.formatValue

// TODO: Remove once GUI has List
object XRayCommand : ClientCommand(
    name = "xray",
    description = "Manage xray blocks"
) {
    init {
        literal("add", "+") {
            block("block") { blockArg ->
                execute("Add a block to xray list") {
                    val blockName = blockArg.value.registryName.toString()

                    XRay.INSTANCE.extAdd(blockName)
                    MessageSendHelper.sendChatMessage("${formatValue(blockName)} has been added to the xray block list")
                }
            }
        }

        literal("remove", "-") {
            block("block") { blockArg ->
                execute("Remove a block from xray list") {
                    val blockName = blockArg.value.registryName.toString()

                    XRay.INSTANCE.extRemove(blockName)
                    MessageSendHelper.sendChatMessage("Removed ${formatValue(blockName)} from xray block list")
                }
            }
        }

        literal("set", "=") {
            block("block") { blockArg ->
                execute("Set the xray list to one block") {
                    val blockName = blockArg.value.registryName.toString()

                    XRay.INSTANCE.extSet(blockName)
                    MessageSendHelper.sendChatMessage("Set the xray block list to ${formatValue(blockName)}")
                }
            }
        }

        literal("list") {
            execute("Print xray block list") {
                MessageSendHelper.sendChatMessage("The following blocks are set in xray:")
                MessageSendHelper.sendRawChatMessage(XRay.INSTANCE.extGet())
            }
        }

        literal("reset", "default") {
            execute("Reset the xray list to defaults") {
                XRay.INSTANCE.extDefaults()
                MessageSendHelper.sendChatMessage("Reset the xray block list to defaults")
            }
        }

        literal("clear") {
            execute("Set the xray list to nothing") {
                XRay.INSTANCE.extClear()
                MessageSendHelper.sendChatMessage("Cleared the xray block list")
            }
        }

        literal("invert") {
            execute("Invert xray block list") {
                XRay.INSTANCE.invert.value = !XRay.INSTANCE.invert.value
                MessageSendHelper.sendChatMessage("Inverted the xray block list")
            }
        }
    }
}