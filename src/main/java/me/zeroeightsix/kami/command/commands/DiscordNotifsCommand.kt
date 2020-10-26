package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.syntax.ChunkBuilder
import me.zeroeightsix.kami.module.modules.chat.DiscordNotifs
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendErrorMessage

/**
 * @author l1ving
 * Created by l1ving on 26/03/20
 */
class DiscordNotifsCommand : Command("discordnotifs", ChunkBuilder().append("webhook url").append("discord id").append("avatar url").build(), "webhook") {
    override fun call(args: Array<String?>) {
        if (args[0] != null && args[0] != "") {
            DiscordNotifs.url.setValue(args[0]!!)
            sendChatMessage(DiscordNotifs.chatName + " Set URL to \"" + args[0] + "\"!")
        } else if (args[0] == null) {
            sendErrorMessage(DiscordNotifs.chatName + " Error: you must specify a URL or \"\" for the first parameter when running the command")
        }
        if (args[1] == null) return
        if (args[1] != "") {
            DiscordNotifs.pingID.setValue(args[1]!!)
            sendChatMessage(DiscordNotifs.chatName + " Set Discord ID to \"" + DiscordNotifs.pingID.value + "\"!")
        }
        if (args[2] == null) return
        if (args[2] != "") {
            DiscordNotifs.avatar.setValue(args[2]!!)
            sendChatMessage(DiscordNotifs.chatName + " Set Avatar to \"" + args[2] + "\"!")
        } else {
            DiscordNotifs.avatar.setValue(KamiMod.GITHUB_LINK + "raw/assets/assets/icons/kami.png")
            sendChatMessage(DiscordNotifs.chatName + " Reset Avatar!")
        }
    }
}