package me.zeroeightsix.kami.util.text

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.module.modules.chat.ChatEncryption
import java.util.regex.Pattern

object MessageDetectionHelper {
    fun getMessageType(direct: Boolean, directSent: Boolean, message: String, server: String): String {
        if (isDirect(direct, message)) return "You got a direct message!\n"
        if (isDirectOther(directSent, message)) return "You sent a direct message!\n"
        if (message == "KamiBlueMessageType1") return "Connected to $server"
        return if (message == "KamiBlueMessageType2") "Disconnected from $server" else ""
    }

    fun isDirect(direct: Boolean, message: String?): Boolean {
        if (message == null) return false
        return direct && (message.find("^([0-9A-z_])+ whispers:.*") || message.find("^\\[.*->.*]") || message.find("^([0-9A-z_])+ whispers to you:.*"))
    }

    fun isDirectOther(directSent: Boolean, message: String?): Boolean {
        if (message == null) return false
        return directSent && message.find("^to ([0-9A-z_])+:.*")
    }

    fun isTPA(tpa: Boolean, message: String?): Boolean {
        if (message == null) return false
        return tpa && message.find("^([0-9A-z_])+ (has requested|wants) to teleport to you\\..*")
    }

    fun isQueue(queue: Boolean, message: String): Boolean {
        return if (queue && message.contains("Position in queue:")) true else queue && message.contains("2b2t is full")
    }

    fun isImportantQueue(importantPings: Boolean, message: String): Boolean {
        return importantPings && (message == "Position in queue: 1" || message == "Position in queue: 2" || message == "Position in queue: 3")
    }

    fun isRestart(restart: Boolean, message: String): Boolean {
        return restart && message.contains("[SERVER] Server restarting in")
    }

    fun shouldSend(all: Boolean, restart: Boolean, direct: Boolean, directSent: Boolean, queue: Boolean, importantPings: Boolean, message: String): Boolean {
        return if (all) true else isRestart(restart, message) || isDirect(direct, message) || isDirectOther(directSent, message) || isQueue(queue, message) || isImportantQueue(importantPings, message)
    }

    fun isCommand(string: String) = commandPrefixes.firstOrNull { string.startsWith(it) } != null

    fun isKamiCommand(string: String) = string.startsWith(Command.getCommandPrefix())

    fun String.find(regex: String): Boolean = Pattern.compile(regex).matcher(this).find()

    private val commandPrefixes: Array<String>
        get() = arrayOf("/", ",", ".", "-", ";", "?", "*", "^", "&", "%", "#", "$",
                Command.getCommandPrefix(),
                ChatEncryption.delimiterValue.value)
}