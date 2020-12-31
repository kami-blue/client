package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.CommandManager
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.mixin.extension.packetMessage
import me.zeroeightsix.kami.mixin.extension.textComponent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.text.formatValue
import net.minecraft.network.play.client.CPacketChatMessage
import net.minecraft.network.play.server.SPacketChat
import net.minecraft.util.ChatAllowedCharacters
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextFormatting
import org.kamiblue.event.listener.listener
import java.nio.CharBuffer
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors
import kotlin.math.sqrt

// TODO: for GUI branch: Rewrite to use new chat modification system, remove old modes
// Leaving for later to get merged into 2.01.01 asap, and the old gui doesn't support string settings
@Module.Info(
    name = "ChatEncryption",
    description = "Encrypts and decrypts chat messages",
    category = Module.Category.CHAT
)
object ChatEncryption : Module() {
    private val self = register(Settings.b("DecryptOwn", true))
    private val mode = register(Settings.e<EncryptionMode>("EncryptMode", EncryptionMode.VIGNERE))
    private val keyA = register(Settings.integerBuilder("KeyA").withValue(3).withRange(0, 26).withStep(1).withVisibility { mode.value != EncryptionMode.VIGNERE })
    private val keyB = register(Settings.integerBuilder("KeyB").withValue(10).withRange(0, 26).withStep(1).withVisibility { mode.value != EncryptionMode.VIGNERE })
    private val vignereKey = register(Settings.stringBuilder("vignereKey").withValue("defaultKey"))
    private val delimiterEnabled = register(Settings.b("Delimiter", false))
    val delimiterSetting = register(Settings.s("delimiterV", "unchanged"))

    private enum class EncryptionMode {
        SHUFFLE, SHIFT, VIGNERE
    }

    private const val personFrowning = "\uD83D\uDE4D"
    private val pattern = Pattern.compile("<.*?> ")
    private val originChar = charArrayOf('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '-', '_', '/', ';', '=', '?', '+', '\u00B5', '\u00A3', '*', '^', '\u00F9', '$', '!', '{', '}', '\'', '"', '|', '&', ' ')
    private val cypher: Array<Array<Char>> =
        Array(originChar.size) { i ->
            Array(originChar.size) {
                originChar[getSensibleIndex(it + i)]
            }
        }

    private fun getSensibleIndex(origin: Int): Int {
        return if (origin >= originChar.size) {
            origin - originChar.size
        } else {
            origin
        }
    }

    private val delimiterValue: String?
        get() {
            if (delimiterSetting.value == "unchanged") {
                MessageSendHelper.sendErrorMessage("$chatName Please change the delimiter with ${formatValue("${CommandManager.prefix}set $name delimiterV <delimiter>")}")
                disable()
                return null
            }
            return delimiterSetting.value
        }

    private val vignereKeyValue: String?
        get() {
            if (vignereKey.value == "defaultKey") {
                MessageSendHelper.sendErrorMessage("$chatName Please change the key with ${formatValue("${CommandManager.prefix}set $name vignereKey <newKey>")}" +
                    ", then make sure to re-enable $name!")
                disable()
                return null
            }
            return vignereKey.value
        }

    private var previousMessage = ""

    init {
        listener<PacketEvent.Send> {
            if (it.packet !is CPacketChatMessage || mc.player == null) return@listener

            var s = it.packet.packetMessage
            if (s == previousMessage) { // fix existing issue - for some reason this is run twice sometimes (single player only)
                previousMessage = ""
                return@listener
            }

            if (delimiterEnabled.value) {
                val delimiter = delimiterValue ?: return@listener
                val sub = s.split(delimiter)

                val builder = StringBuilder()

                for (i in sub.indices) {
                    if (i % 2 == 0) {
                        builder.append(sub[i])
                    } else {
                        val encoded = encode(sub[i])
                        if (encoded.isBlank()) break

                        builder.append(personFrowning)
                        builder.append(encoded)
                        builder.append(personFrowning)
                    }
                }

                val built = builder.toString()
                if (built.isBlank()) return@listener

                s = built
            } else {
                val encoded = encode(s)
                if (encoded.isBlank()) return@listener

                s = encoded + personFrowning
            }

            if (s.length > 256) {
                MessageSendHelper.sendChatMessage("Encrypted message length was too long, couldn't send!")
                it.cancel()
                return@listener
            }

            previousMessage = s
            it.packet.packetMessage = s
        }

        listener<PacketEvent.Receive> {
            if (it.packet !is SPacketChat) return@listener
            var s = it.packet.textComponent.unformattedText

            if (!self.value && isOwn(s)) return@listener
            val matcher = pattern.matcher(s)
            var username = "unnamed"

            if (matcher.find()) {
                username = matcher.group()
                username = username.substring(1, username.length - 2)
                s = matcher.replaceFirst("")
            }

            if (!s.contains(personFrowning)) return@listener

            val sub = s.split(personFrowning)

            val builder = StringBuilder()

            for (i in sub.indices) {
                if (i % 2 == 0) {
                    builder.append(sub[i])
                } else {
                    builder.append(decode(sub[i]))
                }
            }

            s = builder.toString()
            it.packet.textComponent = TextComponentString("<" + username + "> " + TextFormatting.BOLD + "DECRYPTED" + TextFormatting.RESET + ": " + s)
        }
    }

    private fun encode(s: String): String {
        val builder = StringBuilder()

        when (mode.value as EncryptionMode) {
            EncryptionMode.SHUFFLE -> {
                builder.append(shuffle(getKey(), s))
            }

            EncryptionMode.SHIFT -> {
                try {
                    s.chars().forEachOrdered { value ->
                        builder.append(
                            (value + if (ChatAllowedCharacters.isAllowedCharacter((value + getKey()).toChar())) getKey() else 0).toChar()
                        )
                    }
                } catch (e: Exception) {
                    MessageSendHelper.sendErrorMessage(e.message.toString())
                    KamiMod.LOG.error(e)
                }
            }

            EncryptionMode.VIGNERE -> {
                val key = vignereKeyValue ?: return ""
                builder.append(encodeVignere(key, s))
            }
        }

        return builder.toString()
    }

    private fun decode(s: String): String {
        val builder = StringBuilder()

        when (mode.value as EncryptionMode) {
            EncryptionMode.SHUFFLE -> {
                builder.append(unShuffle(getKey(), s))
            }

            EncryptionMode.SHIFT -> {
                s.chars().forEachOrdered { value: Int -> builder.append((value + if (ChatAllowedCharacters.isAllowedCharacter(value.toChar())) -getKey() else 0).toChar()) }
            }

            EncryptionMode.VIGNERE -> {
                val key = vignereKeyValue ?: return ""
                builder.append(decodeVignere(key, s))
            }
        }

        return builder.toString()
    }

    private fun generateShuffleMap(seed: Int): Map<Char, Char> {
        val r = Random(seed.toLong())
        val characters = CharBuffer.wrap(originChar).chars().mapToObj { value: Int -> value.toChar() }.collect(Collectors.toList())
        val counter = ArrayList(characters)
        counter.shuffle(r)

        val map = LinkedHashMap<Char, Char>() // ordered

        for (i in characters.indices) {
            map[characters[i]] = counter[i]
        }

        return map
    }

    private fun shuffle(seed: Int, input: String): String {
        val s = generateShuffleMap(seed)
        val builder = StringBuilder()

        swapCharacters(input, s, builder)
        return builder.toString()
    }

    private fun unShuffle(seed: Int, input: String): String {
        val s = generateShuffleMap(seed)
        val builder = StringBuilder()

        swapCharacters(input, reverseMap(s), builder)
        return builder.toString()
    }

    private fun <K, V> reverseMap(map: Map<K, V>): Map<V, K> {
        return map.entries.stream().collect(Collectors.toMap({ it.value }) { it.key })
    }

    private fun swapCharacters(input: String, s: Map<Char, Char>, builder: StringBuilder) {
        CharBuffer.wrap(input.toCharArray()).chars().forEachOrdered { value: Int ->
            val c = value.toChar()

            if (s.containsKey(c)) {
                builder.append(s[c])
            } else {
                builder.append(c)
            }
        }
    }

    private fun isOwn(message: String): Boolean {
        return Pattern.compile("^<" + mc.player.name + "> ", Pattern.CASE_INSENSITIVE).matcher(message).find()
    }

    private fun getKey(): Int {
        return sqrt((keyA.value * keyB.value).toDouble()).toInt()
    }

    private fun encodeVignere(key: String, text: String): String {
        var endText = ""
        var keyPos = 0

        for (char in text.toCharArray()) {

            val index = originChar.indexOf(char)

            if (index == -1) {
                continue
            }

            endText += cypher[originChar.indexOf(key[keyPos])][index]
            keyPos++

            if (keyPos >= key.length) {
                keyPos -= key.length
            }
        }

        return endText
    }

    private fun decodeVignere(key: String, text: String): String {
        var endText = ""
        var keyPos = 0

        for (char in text.toCharArray()) {
            endText += originChar[cypher[originChar.indexOf(key[keyPos])].indexOf(char)]
            keyPos++

            if (keyPos >= key.length) {
                keyPos -= key.length
            }
        }

        return endText
    }
}