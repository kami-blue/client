package me.zeroeightsix.kami.util.text

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.module.modules.chat.ChatEncryption
import java.util.regex.Pattern

/**
 * A helper to detect certain messages and return a boolean or message
 *
 * @author l1ving
 * @see me.zeroeightsix.kami.module.modules.chat.DiscordNotifs
 */
object MessageDetectionHelper {
    @JvmStatic
    fun getMessageType(direct: Boolean, directSent: Boolean, message: String, server: String): String {
        if (isDirect(direct, message)) return "You got a direct message!\n"
        if (isDirectOther(directSent, message)) return "You sent a direct message!\n"
        if (message == "KamiBlueMessageType1") return "Connected to $server"
        return if (message == "KamiBlueMessageType2") "Disconnected from $server" else ""
    }

    @JvmStatic
    fun isDirect(direct: Boolean, message: String?): Boolean {
        if (message == null) return false
        return direct && (Pattern.compile("^([0-9A-z_])+ whispers:.*").matcher(message).find() || Pattern.compile("^\\[.*->.*]").matcher(message).find())
    }

    @JvmStatic
    fun isDirectOther(directSent: Boolean, message: String?): Boolean {
        if (message == null) return false
        return directSent && Pattern.compile("^to ([0-9A-z_])+:.*").matcher(message).find()
    }

    fun isTPA(tpa: Boolean, message: String?): Boolean {
        if (message == null) return false
        return tpa && Pattern.compile("^([0-9A-z_])+ (has requested|wants) to teleport to you\\..*").matcher(message).find()
    }

    fun isQueue(queue: Boolean, message: String): Boolean {
        return if (queue && message.contains("Position in queue:")) true else queue && message.contains("2b2t is full")
    }

    @JvmStatic
    fun isImportantQueue(importantPings: Boolean, message: String): Boolean {
        return importantPings && (message == "Position in queue: 1" || message == "Position in queue: 2" || message == "Position in queue: 3")
    }

    @JvmStatic
    fun isRestart(restart: Boolean, message: String): Boolean {
        return restart && message.contains("[SERVER] Server restarting in")
    }

    @JvmStatic
    fun shouldSend(all: Boolean, restart: Boolean, direct: Boolean, directSent: Boolean, queue: Boolean, importantPings: Boolean, message: String): Boolean {
        return if (all) true else isRestart(restart, message) || isDirect(direct, message) || isDirectOther(directSent, message) || isQueue(queue, message) || isImportantQueue(importantPings, message)
    }

    fun isCommand(string: String) = commandPrefixes.firstOrNull { string.startsWith(it) } != null

    fun isKamiCommand(string: String) = string.startsWith(Command.getCommandPrefix())

    private val commandPrefixes: Array<String>
        get() = arrayOf("/", ",", ".", "-", ";", "?", "*", "^", "&", "%", "#", "$",
                Command.getCommandPrefix(),
                ChatEncryption.delimiterValue.value)
}