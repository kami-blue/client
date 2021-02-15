package org.kamiblue.client.command.commands

import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.event.ClickEvent
import net.minecraft.util.text.event.HoverEvent
import org.kamiblue.client.command.ClientCommand
import org.kamiblue.client.command.CommandManager
import org.kamiblue.client.module.modules.client.CommandConfig
import org.kamiblue.client.util.text.MessageSendHelper.sendChatMessage

object ModifiedCommand: ClientCommand(
    name = "modified",
    description = "View modified settings in a module"
) {
    init {
        module("module") { module ->
            execute("List modified settings") {
                var anyChanged = false

                for (setting in module.value.settingList) {
                    if (!setting.isModified) continue
                    anyChanged = true

                    val component = TextComponentString("${setting.name} has been changed to ${setting.value}")
                    // horrible, however this is mojang code that we are working on.
                    component.style.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "${CommandManager.prefix}set ${module.value.name} ${setting.name.replace(" ", "")} ${setting.defaultValue}")
                    component.style.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponentString("Click to reset to default settings"))

                    sendChatMessage(component)
                }

                if (!anyChanged) {
                    sendChatMessage("${module.value.name}'s settings are not modified from default")
                }
            }
        }
    }
}