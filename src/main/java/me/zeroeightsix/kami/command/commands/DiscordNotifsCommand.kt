package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.module.modules.chat.DiscordNotifs
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.util.text.TextFormatting

// TODO: Remove once GUI has proper String setting editing and is in master branch
object DiscordNotifsCommand : ClientCommand(
    name = "discordnotifs",
    alias = arrayOf("webhook")
) {
    private val urlRegex = Regex("^https://.*discord\\.com/api/webhooks/([0-9])+/.{68}$2")

    init {
        literal("id") {
            long("discord user id") { idArg ->
                execute("Set the ID of the user to be pinged") {
                    DiscordNotifs.pingID.value = idArg.value.toString()
                    MessageSendHelper.sendChatMessage("Set Discord User ID to [&7${idArg.value}&f]!")
                }
            }

        }

        literal("avatar") {
            greedy("url") { urlArg ->
                execute {
                    DiscordNotifs.avatar.value = urlArg.value
                    MessageSendHelper.sendChatMessage("Set Webhook Avatar to [&7${urlArg.value}&f]!")
                }
            }
        }

        greedy("url") { urlArg ->
            execute("Set the webhook url") {
                if (!urlRegex.matches(urlArg.value)) {
                    MessageSendHelper.sendErrorMessage("Error, the URL [" +
                        TextFormatting.GRAY + urlArg.value + TextFormatting.RESET +
                        "] does not match the valid webhook format!"
                    )
                    return@execute
                }

                DiscordNotifs.url.value = urlArg.value
                MessageSendHelper.sendChatMessage("Set Webhook URL to [&7${urlArg.value}&f]!")
            }
        }
    }
}