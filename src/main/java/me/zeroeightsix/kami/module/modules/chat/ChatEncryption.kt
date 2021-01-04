package me.zeroeightsix.kami.module.modules.chat

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.manager.managers.MessageManager
import me.zeroeightsix.kami.manager.managers.MessageManager.newMessageModifier
import me.zeroeightsix.kami.mixin.extension.textComponent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.text.MessageDetection
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.text.format
import me.zeroeightsix.kami.util.text.formatValue
import me.zeroeightsix.kami.util.threads.defaultScope
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.network.play.server.SPacketChat
import net.minecraft.util.text.TextFormatting
import org.kamiblue.commons.utils.SystemUtils
import java.util.*
import kotlin.collections.HashMap

// TODO: Add proper RSA encryption
@Module.Info(
    name = "ChatEncryption",
    description = "Encrypts and decrypts chat messages",
    category = Module.Category.CHAT,
    modulePriority = -69420
)
object ChatEncryption : Module() {
    private val commands = setting("Commands", false)
    private val self = setting("DecryptOwn", true)
    private val keySetting = setting("KeySetting", "DefaultKey")
    val delimiter = setting("Delimiter", "%", consumer = { prev: String, value: String ->
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
            (commands.value || MessageDetection.Command.ANY_EXCEPT_DELIMITER detectNot it.packet.message)
        },
        modifier = {
            it.encrypt() ?: it.packet.message
        }
    )

    private var previousMessage = ""

    override fun onEnable() {
        getOrGenKey()
        modifier.enable()
    }

    override fun onDisable() {
        modifier.disable()
    }

    private fun getOrGenKey(): String {
        var key = keySetting.value

        if (key == "DefaultKey") {
            key = randomChars()
            keySetting.value = key

            MessageSendHelper.sendChatMessage("$chatName Your encryption key was set to ${formatValue(key)}, and copied to your clipboard.")

            defaultScope.launch(Dispatchers.IO) {
                SystemUtils.copyToClipboard(key)
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
            if (!self.value && playerName == player.name) return@safeListener

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

        val splitString = message.split(delimiter.value)

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