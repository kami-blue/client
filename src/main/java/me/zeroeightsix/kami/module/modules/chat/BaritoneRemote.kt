package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.command.CommandManager
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.PrintChatMessageEvent
import me.zeroeightsix.kami.manager.managers.FriendManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.text.*
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendServerMessage
import net.minecraft.network.play.server.SPacketChat
import org.kamiblue.event.listener.listener

object BaritoneRemote : Module(
    name = "BaritoneRemote",
    description = "Remotely control Baritone with /msg",
    category = Category.CHAT
) {
    private val feedback = setting("SendFeedback", true)
    private val allow = setting("Allow", Allow.FRIENDS)
    private val custom = setting("Custom", "unchanged")

    private var sendNextMsg = false
    private var lastController: String? = null

    init {
        /* instructions for changing custom setting */
        allow.listeners.add {
            mc.player?.let {
                if ((allow.value == Allow.CUSTOM || allow.value == Allow.FRIENDS_AND_CUSTOM) && custom.value == "unchanged") {
                    MessageSendHelper.sendChatMessage("$chatName Use the ${formatValue("${CommandManager.prefix}set Custom")}"
                        + " command to change the custom users list. For example, "
                        + formatValue("${CommandManager.prefix}set Custom dominika,Dewy,086"))
                }
            }
        }

        /* convert incoming dms into valid baritone commands */
        listener<PacketEvent.Receive> {
            if (it.packet !is SPacketChat) return@listener
            val message = it.packet.chatComponent.unformattedText

            if (MessageDetection.Direct.RECEIVE detectNot message) return@listener

            val command = MessageDetection.Direct.RECEIVE.removedOrNull(message) ?: return@listener
            val username = MessageDetection.Direct.RECEIVE.playerName(message) ?: return@listener

            if (!isValidUser(username)) return@listener

            val baritoneCommand = MessageDetection.Command.BARITONE.removedOrNull(command) ?: return@listener

            MessageSendHelper.sendBaritoneCommand(*baritoneCommand.split(' ').toTypedArray())
            sendNextMsg = true
            lastController = username
        }

        /* forward baritone feedback to controller */
        listener<PrintChatMessageEvent> {
            lastController?.let { controller ->
                if (feedback.value && MessageDetection.Other.BARITONE detect it.chatComponent.unformattedText) {
                    sendServerMessage("/msg $controller " + it.chatComponent.unformattedText)
                }
            }
        }
    }

    private fun isValidUser(username: String): Boolean {
        return when (allow.value) {
            Allow.ANYBODY -> true
            Allow.FRIENDS -> FriendManager.isFriend(username)
            Allow.CUSTOM -> isCustomUser(username)
            Allow.FRIENDS_AND_CUSTOM -> FriendManager.isFriend(username) || isCustomUser(username)
        }
    }

    private fun isCustomUser(username: String): Boolean {
        val customs = custom.value.split(",")
        for (_custom in customs) {
            if (_custom == username) return true
        }
        return false
    }

    private enum class Allow {
        ANYBODY, FRIENDS, CUSTOM, FRIENDS_AND_CUSTOM
    }
}
