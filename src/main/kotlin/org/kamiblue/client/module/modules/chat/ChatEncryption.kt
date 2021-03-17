package org.kamiblue.client.module.modules.chat

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.network.play.server.SPacketChat
import net.minecraft.util.text.TextFormatting
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.manager.managers.MessageManager
import org.kamiblue.client.manager.managers.MessageManager.newMessageModifier
import org.kamiblue.client.mixin.extension.textComponent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.text.MessageDetection
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.text.format
import org.kamiblue.client.util.text.formatValue
import org.kamiblue.client.util.threads.defaultScope
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.commons.utils.SystemUtils
import java.util.*
import kotlin.collections.HashMap

// TODO: Add proper RSA encryption
internal object ChatEncryption : Module(
    name = "ChatEncryption",
    description = "Encrypts and decrypts chat messages",
    category = Category.CHAT,
    modulePriority = -69420
) {
    private val commands by setting("Commands", false)
    private val self by setting("Decrypt Own", true)
    private var keySetting by setting("Key Setting", "DefaultKey")
    val delimiter by setting("Delimiter", "%", consumer = { prev: String, value: String ->
        if (value.length == 1 && !chars.contains(value.first())) value else prev
    })

    private const val personFrowning = "🙍"

    private val chars = charArrayOf(
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        ',', '.', '!', '?', ' '
    )

    private val charsMap: Map<Char, Int> = HashMap<Char, Int>(67).apply {
        for ((index, char) in chars.withIndex()) {
            put(char, index)
        }
    }

    private val vigenereSquare = Array(chars.size) { index1 ->
        CharArray(chars.size) { index2 ->
            chars[(index1 + index2) % chars.size]
        }
    }

    private val reversedSquare = Array(chars.size) { index1 ->
        CharArray(chars.size) { index2 ->
            chars[Math.floorMod((index1 - index2), chars.size)]
        }
    }

    private val modifier = newMessageModifier(
        filter = {
            (commands || MessageDetection.Command.ANY_EXCEPT_DELIMITER detectNot it.packet.message)
        },
        modifier = {
            it.encrypt() ?: it.packet.message
        }
    )

    private var previousMessage = ""

    init {
        onEnable {
            getOrGenKey()
            modifier.enable()
        }

        onDisable {
            modifier.disable()
        }
    }

    private fun getOrGenKey(): String {
        var key = keySetting

        if (key == "DefaultKey") {
            key = randomChars()
            keySetting = key

            MessageSendHelper.sendChatMessage("$chatName Your encryption key was set to ${formatValue(key)}, and copied to your clipboard.")

            defaultScope.launch(Dispatchers.IO) {
                SystemUtils.setClipboard(key)
            }
        }

        return key
    }

    private fun randomChars() = StringBuilder().run {
        for (i in 1..32) {
            append(chars.random())
        }
        toString()
    }

    init {
        safeListener<PacketEvent.Receive> {
            if (it.packet !is SPacketChat) return@safeListener

            val fullMessage: CharSequence = it.packet.textComponent.unformattedText
            if (!fullMessage.contains(personFrowning)) return@safeListener

            val playerName = MessageDetection.Message.ANY.playerName(fullMessage) ?: "Unknown User"
            if (!self && playerName == player.name) return@safeListener

            val message = MessageDetection.Message.ANY.removedOrNull(fullMessage) ?: return@safeListener
            val splitString = message.split(personFrowning)

            val final = StringBuilder().run {
                for ((index, string) in splitString.withIndex()) {
                    if (index % 2 == 0) {
                        append(string)
                    } else {
                        decodeVigenere(string)
                    }
                }
                toString()
            }

            if (final == message) return@safeListener // prevent further possible issues

            MessageSendHelper.sendRawChatMessage("<$playerName> ${TextFormatting.BOLD format "DECRYPTED"}: $final")
        }
    }

    private fun MessageManager.QueuedMessage.encrypt(): String? {
        val message = packet.message

        // fix existing issue - for some reason this is run twice sometimes (single player only)
        if (message == previousMessage) {
            previousMessage = ""
            return null
        }

        val splitString = message.split(delimiter)

        val encrypted = StringBuilder().run {
            for ((index, string) in splitString.withIndex()) {
                if (index % 2 == 0) {
                    append(string)
                } else {
                    append(personFrowning)
                    encodeVigenere(string)
                    append(personFrowning)
                }
            }
            toString()
        }

        if (encrypted.isBlank()) return null

        if (encrypted.length > 256) { // this shouldn't happen unless your message was already around 254 chars?
            MessageSendHelper.sendChatMessage("Encrypted message length was too long, couldn't send!")
            return null
        }

        previousMessage = encrypted
        return encrypted
    }

    private fun StringBuilder.encodeVigenere(string: String) {
        val key = getOrGenKey()

        for ((index, char) in string.withIndex()) {
            val indexOfKey = charsMap[key[index % key.length]] ?: continue
            val indexOfChar = charsMap[char] ?: continue
            val encodedChar = vigenereSquare[indexOfKey][indexOfChar]

            append(encodedChar)
        }
    }

    private fun StringBuilder.decodeVigenere(string: String) {
        val key = getOrGenKey()

        for ((index, char) in string.withIndex()) {
            val indexOfKey = charsMap[key[index % key.length]] ?: continue
            val indexOfChar = charsMap[char] ?: continue
            val decodedChar = reversedSquare[indexOfChar][indexOfKey]

            append(decodedChar)
        }
    }
}