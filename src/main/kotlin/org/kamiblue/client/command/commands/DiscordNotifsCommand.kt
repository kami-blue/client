package org.kamiblue.client.command.commands

import org.kamiblue.client.command.ClientCommand
import org.kamiblue.client.module.modules.chat.DiscordNotifs
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.text.formatValue

// TODO: Remove once GUI has proper String setting editing and is in master branch
object DiscordNotifsCommand : ClientCommand(
    name = "discordnotifs",
    alias = arrayOf("webhook")
) {
    private val urlRegex = Regex("^https://.*discord(app|)\\.com/api/webhooks/\\d+/.{68}$")

    init {
        literal("id") {
            long("discord user id") { idArg ->
                execute("Set the ID of the user to be pinged") {
                    DiscordNotifs.pingID.value = idArg.value.toString()
                    MessageSendHelper.sendChatMessage("Set Discord User ID to ${formatValue(idArg.value.toString())}!")
                }
            }
        }

        literal("avatar") {
            greedy("url") { urlArg ->
                execute("Set the webhook icon") {
                    DiscordNotifs.avatar.value = urlArg.value
                    MessageSendHelper.sendChatMessage("Set Webhook Avatar to ${formatValue(urlArg.value)}!")
                }
            }
        }

        greedy("url") { urlArg ->
            execute("Set the webhook url") {
                if (!urlRegex.matches(urlArg.value)) {
                    MessageSendHelper.sendErrorMessage("Error, the URL " +
                        formatValue(urlArg.value) +
                        " does not match the valid webhook format!"
                    )

                    return@execute
                }

                DiscordNotifs.url.value = urlArg.value
                MessageSendHelper.sendChatMessage("Set Webhook URL to ${formatValue(urlArg.value)}!")
            }
        }
    }
}
