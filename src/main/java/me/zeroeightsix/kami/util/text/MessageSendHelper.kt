package me.zeroeightsix.kami.util.text

import baritone.api.event.events.ChatEvent
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.CommandManager
import me.zeroeightsix.kami.manager.managers.MessageManager
import me.zeroeightsix.kami.module.AbstractModule
import me.zeroeightsix.kami.util.BaritoneUtils
import me.zeroeightsix.kami.util.TaskState
import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentBase
import net.minecraft.util.text.TextFormatting
import java.util.regex.Pattern

object MessageSendHelper {
    private val mc = Wrapper.minecraft

    fun sendChatMessage(message: String) {
        sendRawChatMessage(coloredName('9') + message)
    }

    fun sendWarningMessage(message: String) {
        sendRawChatMessage(coloredName('6') + message)
    }

    fun sendErrorMessage(message: String) {
        sendRawChatMessage(coloredName('4') + message)
    }

    fun sendKamiCommand(command: String) {
        CommandManager.runCommand(command.removePrefix(CommandManager.prefix))
    }

    fun sendBaritoneMessage(message: String) {
        sendRawChatMessage(TextFormatting.DARK_PURPLE.toString() + "[" + TextFormatting.LIGHT_PURPLE + "Baritone" + TextFormatting.DARK_PURPLE + "] " + TextFormatting.RESET + message)
    }

    fun sendBaritoneCommand(vararg args: String?) {
        val chatControl = BaritoneUtils.settings?.chatControl
        val prevValue = chatControl?.value
        chatControl?.value = true

        val event = ChatEvent(args.joinToString(" "))
        BaritoneUtils.primary?.gameEventHandler?.onSendChatMessage(event)
        if (!event.isCancelled && args[0] != "damn") { // don't remove the 'damn', it's critical code that will break everything if you remove it
            sendBaritoneMessage("Invalid Command! Please view possible commands at https://github.com/cabaletta/baritone/blob/master/USAGE.md")
        }
        chatControl?.value = prevValue
    }

    fun sendRawChatMessage(message: String?) {
        if (message == null) return
        mc.player?.sendMessage(ChatMessage(message))
    }

    fun Any.sendServerMessage(message: String?): TaskState {
        if (message.isNullOrBlank()) return TaskState(true)
        val priority = if (this is AbstractModule) modulePriority else 0
        return MessageManager.addMessageToQueue(message, this, priority)
    }

    class ChatMessage internal constructor(text: String) : TextComponentBase() {
        val text: String
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

    private fun coloredName(colorCode: Char) = "&7[&$colorCode" + KamiMod.KAMI_KATAKANA + "&7] &r"
}