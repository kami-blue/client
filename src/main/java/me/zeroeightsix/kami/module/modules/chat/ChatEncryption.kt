package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendErrorMessage
import net.minecraft.network.play.client.CPacketChatMessage
import net.minecraft.network.play.server.SPacketChat
import net.minecraft.util.ChatAllowedCharacters
import net.minecraft.util.text.TextComponentString
import java.nio.CharBuffer
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors
import kotlin.math.sqrt

@Module.Info(
        name = "ChatEncryption",
        description = "Encrypts and decrypts chat messages",
        category = Module.Category.CHAT
)
object ChatEncryption : Module() {
    private val self = register(Settings.b("DecryptOwn", true))
    private val mode = register(Settings.e<EncryptionMode>("Mode", EncryptionMode.SHUFFLE))
    private val keyA = register(Settings.integerBuilder("KeyA").withValue(3).withRange(0, 26).withStep(1))
    private val keyB = register(Settings.integerBuilder("KeyB").withValue(10).withRange(0, 26).withStep(1))
    private val delimiterSetting = register(Settings.b("Delimiter", true))
    val delimiterValue = register(Settings.s("delimiterV", "unchanged"))

    private enum class EncryptionMode {
        SHUFFLE, SHIFT
    }

    private val pattern = Pattern.compile("<.*?> ")
    private val originChar = charArrayOf('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '-', '_', '/', ';', '=', '?', '+', '\u00B5', '\u00A3', '*', '^', '\u00F9', '$', '!', '{', '}', '\'', '"', '|', '&')
    private val delimiter: String?
        get() {
            if (delimiterValue.value.equals("unchanged", ignoreCase = true)) {
                sendErrorMessage(chatName + " Please change the delimiter with &7" + Command.getCommandPrefix() + "chatencryption&f, disabling")
                disable()
                return null
            }
            return delimiterValue.value
        }

    init {
        listener<PacketEvent.Send> {
            if (it.packet !is CPacketChatMessage || mc.player == null) return@listener
            var s = it.packet.getMessage()
            if (delimiterSetting.value) {
                if (delimiter == null || !s.startsWith(delimiter!!)) return@listener
                s = s.substring(1)
            }
            val builder = StringBuilder()
            when (mode.value as EncryptionMode) {
                EncryptionMode.SHUFFLE -> {
                    builder.append(shuffle(getKey(), s))
                    builder.append("\uD83D\uDE4D")
                }
                EncryptionMode.SHIFT -> {
                    s.chars().forEachOrdered { value: Int -> builder.append((value + if (ChatAllowedCharacters.isAllowedCharacter((value + getKey()).toChar())) getKey() else 0).toChar()) }
                    builder.append("\uD83D\uDE48")
                }
            }
            s = builder.toString()
            if (s.length > 256) {
                sendChatMessage("Encrypted message length was too long, couldn't send!")
                it.cancel()
                return@listener
            }
            it.packet.message = s
        }

        listener<PacketEvent.Receive> {
            if (it.packet !is SPacketChat) return@listener
            var s = it.packet.getChatComponent().unformattedText
            if (!self.value && isOwn(s)) return@listener
            val matcher = pattern.matcher(s)
            var username = "unnamed"
            if (matcher.find()) {
                username = matcher.group()
                username = username.substring(1, username.length - 2)
                s = matcher.replaceFirst("")
            }
            val builder = StringBuilder()
            val substring = s.substring(0, s.length - 2)
            when (mode.value as EncryptionMode) {
                EncryptionMode.SHUFFLE -> {
                    if (!s.endsWith("\uD83D\uDE4D")) return@listener
                    s = substring
                    builder.append(unShuffle(getKey(), s))
                }
                EncryptionMode.SHIFT -> {
                    if (!s.endsWith("\uD83D\uDE48")) return@listener
                    s = substring
                    s.chars().forEachOrdered { value: Int -> builder.append((value + if (ChatAllowedCharacters.isAllowedCharacter(value.toChar())) -getKey() else 0).toChar()) }
                }
            }
            it.packet.chatComponent = TextComponentString("<" + username + "> " + KamiMod.color + "lDECRYPTED" + KamiMod.color + "r: " + builder.toString())
        }
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
            if (s.containsKey(c)) builder.append(s[c]) else builder.append(c)
        }
    }

    private fun isOwn(message: String): Boolean {
        return Pattern.compile("^<" + mc.player.name + "> ", Pattern.CASE_INSENSITIVE).matcher(message).find()
    }

    private fun getKey(): Int {
        return sqrt((keyA.value * keyB.value).toDouble()).toInt()
    }
}