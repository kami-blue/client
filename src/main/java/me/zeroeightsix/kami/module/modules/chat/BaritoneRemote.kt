package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.PrintChatMessageEvent
import me.zeroeightsix.kami.manager.managers.FriendManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.BaritoneUtils
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.text.MessageDetectionHelper
import me.zeroeightsix.kami.util.text.MessageDetectionHelper.detect
import me.zeroeightsix.kami.util.text.MessageDetectionHelper.detectAndRemove
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.text.Regexes
import net.minecraft.network.play.server.SPacketChat

@Module.Info(
        name = "BaritoneRemote",
        description = "Remotely control Baritone with /msg",
        category = Module.Category.CHAT
)
object BaritoneRemote : Module() {
    private val feedback = setting("SendFeedback", true)
    private val allow: Setting<Allow> = setting("Allow", Allow.FRIENDS)
    private val custom = setting("Custom", "unchanged")

    private var sendNextMsg = false
    private var lastController: String? = null

    init {
        /* instructions for changing custom setting */
        allow.listeners.add {
            mc.player?.let {
                if ((allow.value == Allow.CUSTOM || allow.value == Allow.FRIENDS_AND_CUSTOM) && custom.value == "unchanged") {
                    MessageSendHelper.sendChatMessage("$chatName Use the &7" + Command.getCommandPrefix()
                            + "set $name Custom names&f command to change the custom users list. Use , to separate players, for example &7"
                            + Command.getCommandPrefix()
                            + "set $name Custom dominika,Dewy,086&f")
                }
            }
        }

        /* convert incoming dms into valid baritone commands */
        listener<PacketEvent.Receive> {
            if (it.packet !is SPacketChat) return@listener
            val message = it.packet.getChatComponent().unformattedText

            if (MessageDetectionHelper.isDirect(true, message)) {
                val username = MessageDetectionHelper.getDirectUsername(message) ?: return@listener
                val command = message.detectAndRemove(Regexes.DIRECT) ?: message.detectAndRemove(Regexes.DIRECT_ALT)
                ?: return@listener

                if ((!command.startsWith("#") && !command.startsWith(";b ")) || !isValidUser(username)) return@listener

                val baritoneCommand =
                        if (command.startsWith(bPrefix)) command.substring(bPrefix.length).split(" ")
                        else command.substring(kbPrefix.length).split(" ")

                MessageSendHelper.sendBaritoneCommand(*baritoneCommand.toTypedArray())
                sendNextMsg = true
                lastController = username
            }
        }

        /* forward baritone feedback to controller */
        listener<PrintChatMessageEvent> {
            lastController?.let { controller ->
                if (feedback.value && it.chatComponent.unformattedText.detect(Regexes.BARITONE)) {
                    MessageSendHelper.sendServerMessage("/msg $controller " + it.chatComponent.unformattedText)
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
