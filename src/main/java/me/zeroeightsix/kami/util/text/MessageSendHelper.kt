package me.zeroeightsix.kami.util.text

import baritone.api.BaritoneAPI
import baritone.api.event.events.ChatEvent
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.util.Wrapper.minecraft
import me.zeroeightsix.kami.util.Wrapper.player
import net.minecraft.client.Minecraft
import net.minecraft.launchwrapper.LogWrapper
import net.minecraft.network.play.client.CPacketChatMessage
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentBase
import net.minecraft.util.text.TextFormatting
import java.util.regex.Pattern

object MessageSendHelper {
    @JvmStatic
    fun sendChatMessage(message: String) {
        sendRawChatMessage("&7[&9" + KamiMod.KAMI_KANJI + "&7] &r" + message)
    }

    @JvmStatic
    fun sendWarningMessage(message: String) {
        sendRawChatMessage("&7[&6" + KamiMod.KAMI_KANJI + "&7] &r" + message)
    }

    @JvmStatic
    fun sendErrorMessage(message: String) {
        sendRawChatMessage("&7[&4" + KamiMod.KAMI_KANJI + "&7] &r" + message)
    }

    @JvmStatic
    fun sendKamiCommand(command: String, addToHistory: Boolean) {
        try {
            if (addToHistory) {
                minecraft.ingameGUI.chatGUI.addToSentMessages(command)
            }
            if (command.length > 1) KamiMod.getInstance().commandManager.callCommand(command.substring(Command.getCommandPrefix().length - 1)) else sendChatMessage("Please enter a command!")
        } catch (e: Exception) {
            e.printStackTrace()
            sendChatMessage("Error occurred while running command! (" + e.message + "), check the log for info!")
        }
    }

    @JvmStatic
    fun sendBaritoneMessage(message: String) {
        sendRawChatMessage(TextFormatting.DARK_PURPLE.toString() + "[" + TextFormatting.LIGHT_PURPLE + "Baritone" + TextFormatting.DARK_PURPLE + "] " + TextFormatting.RESET + message)
    }

    @JvmStatic
    fun sendBaritoneCommand(vararg args: String?) {
        val chatControl = BaritoneAPI.getSettings().chatControl
        val prevValue = chatControl.value
        chatControl.value = true

        // ty leijuwuv <--- quit it :monkey:
        val event = ChatEvent(java.lang.String.join(" ", *args))
        BaritoneAPI.getProvider().primaryBaritone.gameEventHandler.onSendChatMessage(event)
        if (!event.isCancelled && args[0] != "damn") { // don't remove the 'damn', it's critical code that will break everything if you remove it
            sendBaritoneMessage("Invalid Command! Please view possible commands at https://github.com/cabaletta/baritone/blob/master/USAGE.md")
        }
        chatControl.value = prevValue
    }

    @JvmStatic
    fun sendStringChatMessage(messages: Array<String?>) {
        sendChatMessage("")
        for (s in messages) sendRawChatMessage(s)
    }

    @JvmStatic
    fun sendRawChatMessage(message: String?) {
        if (message == null) return
        if (Minecraft.getMinecraft().player != null) {
            player!!.sendMessage(ChatMessage(message))
        } else {
            LogWrapper.info(message)
        }
    }

    @JvmStatic
    fun sendServerMessage(message: String?) {
        if (message.isNullOrBlank()) return
        if (Minecraft.getMinecraft().player != null) {
            player!!.connection.sendPacket(CPacketChatMessage(message))
        } else {
            LogWrapper.warning("Could not send server message: \"$message\"")
        }
    }

    class ChatMessage internal constructor(text: String) : TextComponentBase() {
        var text: String
        override fun getUnformattedComponentText(): String {
            return text
        }

        override fun createCopy(): ITextComponent {
            return ChatMessage(text)
        }

        init {
            val p = Pattern.compile("&[0123456789abcdefrlonmk]")
            val m = p.matcher(text)
            val sb = StringBuffer()
            while (m.find()) {
                val replacement = "\u00A7" + m.group().substring(1)
                m.appendReplacement(sb, replacement)
            }
            m.appendTail(sb)
            this.text = sb.toString()
        }
    }
}