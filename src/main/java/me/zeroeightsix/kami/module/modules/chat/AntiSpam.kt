package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.text.MessageDetectionHelper
import me.zeroeightsix.kami.util.text.MessageDetectionHelper.detect
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.text.Regexes
import me.zeroeightsix.kami.util.text.SpamFilters
import net.minecraft.util.text.TextComponentString
import net.minecraftforge.client.event.ClientChatReceivedEvent
import org.kamiblue.event.listener.listener
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

@Module.Info(
        name = "AntiSpam",
        category = Module.Category.CHAT,
        description = "Removes spam and advertising from the chat",
        showOnArray = Module.ShowOnArray.OFF
)
object AntiSpam : Module() {
    private val mode = register(Settings.e<Mode>("Mode", Mode.REPLACE))
    private val replaceMode = register(Settings.enumBuilder(ReplaceMode::class.java, "ReplaceMode").withValue(ReplaceMode.ASTERISKS).withVisibility { mode.value == Mode.REPLACE })
    private val page = register(Settings.e<Page>("Page", Page.TYPE))

    /* Page One */
    private val discordLinks = register(Settings.booleanBuilder("Discord").withValue(true).withVisibility { page.value == Page.TYPE })
    private val slurs = register(Settings.booleanBuilder("Slurs").withValue(true).withVisibility { page.value == Page.TYPE })
    private val swears = register(Settings.booleanBuilder("Swears").withValue(false).withVisibility { page.value == Page.TYPE })
    private val automated = register(Settings.booleanBuilder("Automated").withValue(true).withVisibility { page.value == Page.TYPE })
    private val ips = register(Settings.booleanBuilder("ServerIps").withValue(true).withVisibility { page.value == Page.TYPE })
    private val specialCharEnding = register(Settings.booleanBuilder("SpecialEnding").withValue(true).withVisibility { page.value == Page.TYPE })
    private val specialCharBegin = register(Settings.booleanBuilder("SpecialBegin").withValue(true).withVisibility { page.value == Page.TYPE })
    private val greenText = register(Settings.booleanBuilder("GreenText").withValue(false).withVisibility { page.value == Page.TYPE })
    private val fancyChat = register(Settings.booleanBuilder("FancyChat").withValue(false).withVisibility { page.value == Page.TYPE })

    /* Page Two */
    private val aggressiveFiltering = register(Settings.booleanBuilder("AggressiveFiltering").withValue(true).withVisibility { page.value == Page.SETTINGS })
    private val duplicates = register(Settings.booleanBuilder("Duplicates").withValue(true).withVisibility { page.value == Page.SETTINGS })
    private val duplicatesTimeout = register(Settings.integerBuilder("DuplicatesTimeout").withValue(30).withRange(1, 600).withStep(5).withVisibility { duplicates.value && page.value == Page.SETTINGS })
    private val filterOwn = register(Settings.booleanBuilder("FilterOwn").withValue(false).withVisibility { page.value == Page.SETTINGS })
    private val filterDMs = register(Settings.booleanBuilder("FilterDMs").withValue(false).withVisibility { page.value == Page.SETTINGS })
    private val filterServer = register(Settings.booleanBuilder("FilterServer").withValue(false).withVisibility { page.value == Page.SETTINGS })
    private val showBlocked = register(Settings.enumBuilder(ShowBlocked::class.java, "ShowBlocked").withValue(ShowBlocked.LOG_FILE).withVisibility { page.value == Page.SETTINGS })

    private enum class Mode {
        REPLACE, HIDE
    }

    @Suppress("unused")
    private enum class ReplaceMode(val redaction: String) {
        REDACTED("[redacted]"), ASTERISKS("****")
    }

    private enum class Page {
        TYPE, SETTINGS
    }

    @Suppress("unused")
    private enum class ShowBlocked {
        NONE, LOG_FILE, CHAT, BOTH
    }

    private val messageHistory = ConcurrentHashMap<String, Long>()
    private val settingMap = hashMapOf(
            greenText to SpamFilters.greenText,
            specialCharBegin to SpamFilters.specialBeginning,
            specialCharEnding to SpamFilters.specialEnding,
            automated to SpamFilters.ownsMeAndAll,
            automated to SpamFilters.thanksTo,
            discordLinks to SpamFilters.discordInvite,
            ips to SpamFilters.ipAddress,
            automated to SpamFilters.announcer,
            automated to SpamFilters.spammer,
            automated to SpamFilters.insulter,
            automated to SpamFilters.greeter,
            slurs to SpamFilters.slurs,
            swears to SpamFilters.swears
    )

    init {
        listener<ClientChatReceivedEvent> { event ->
            if (mc.player == null) return@listener

            messageHistory.values.removeIf { System.currentTimeMillis() - it > 600000 }

            if (duplicates.value && checkDupes(event.message.unformattedText)) {
                event.isCanceled = true
            }

            val pattern = isSpam(event.message.unformattedText)
            if (pattern != null) { // null means no pattern found
                if (mode.value == Mode.HIDE) {
                    event.isCanceled = true
                } else if (mode.value == Mode.REPLACE) {
                    event.message = TextComponentString(sanitize(event.message.formattedText, pattern, (replaceMode.value as ReplaceMode).redaction))
                }
            }

            if (fancyChat.value) {
                val message = sanitizeFancyChat(event.message.unformattedText)
                if (message.trim { it <= ' ' }.isEmpty()) { // this should be removed if we are going for an intelligent de-fancy
                    event.message = TextComponentString(getUsername(event.message.unformattedText) + " [Fancychat]")
                }
            }
        }
    }

    override fun onDisable() {
        messageHistory.clear()
    }

    private fun sanitize(toClean: String, matcher: String, replacement: String): String {
        return if (!aggressiveFiltering.value) {
            toClean.replace("\\b$matcher|$matcher\\b".toRegex(), replacement) // only check for start or end of a word
        } else { // We might encounter the scunthorpe problem, so aggressive mode is off by default.
            toClean.replace(matcher.toRegex(), replacement)
        }
    }

    private fun isSpam(message: String): String? {
        return if (!filterOwn.value && isOwn(message)
                || MessageDetectionHelper.isDirect(!filterDMs.value, message)
                || message.detect(!filterServer.value, Regexes.QUEUE)
                || message.detect(!filterServer.value, Regexes.RESTART)) {
            null
        } else {
            detectSpam(removeUsername(message))
        }
    }

    private fun removeUsername(username: String): String {
        return username.replace("<[^>]*> ".toRegex(), "")
    }

    private fun getUsername(rawMessage: String): String? {
        val matcher = Pattern.compile("<[^>]*>", Pattern.CASE_INSENSITIVE).matcher(rawMessage)
        return if (matcher.find()) {
            matcher.group()
        } else {
            rawMessage.substring(0, rawMessage.indexOf(">")) // a bit hacky
        }
    }

    private fun detectSpam(message: String): String? {
        for ((key, value) in settingMap) {
            val pattern = findPatterns(value, message)
            if (key.value && pattern != null) {
                sendResult(key.name, message)
                return pattern
            }
        }
        return null
    }

    private fun checkDupes(message: String): Boolean {
        var isDuplicate = false

        if (messageHistory.containsKey(message) && (System.currentTimeMillis() - messageHistory[message]!!) / 1000 < duplicatesTimeout.value) isDuplicate = true
        messageHistory[message] = System.currentTimeMillis()

        if (isDuplicate) {
            sendResult("Duplicate", message)
        }
        return isDuplicate
    }


    private fun isOwn(message: String): Boolean {
        /* mc.player is null when the module is being registered, so this matcher isn't added alongside the other FilterPatterns */
        val ownFilter = "^<" + mc.player.name + "> "
        return Pattern.compile(ownFilter, Pattern.CASE_INSENSITIVE).matcher(message).find()
    }

    private fun findPatterns(patterns: Array<String>, string: String): String? {
        var cString = string
        cString = cString.replace("<[^>]*> ".toRegex(), "") // remove username first
        for (pattern in patterns) {
            if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(cString).find()) {
                return pattern
            }
        }
        return null
    }

    private fun sanitizeFancyChat(toClean: String): String {
        // this has the potential to be intelligent and convert to ascii instead of just delete
        return toClean.replace("[^\\u0000-\\u007F]".toRegex(), "")
    }

    private fun sendResult(name: String, message: String) {
        if (showBlocked.value == ShowBlocked.CHAT || showBlocked.value == ShowBlocked.BOTH) MessageSendHelper.sendChatMessage("$chatName $name: $message")
        if (showBlocked.value == ShowBlocked.LOG_FILE || showBlocked.value == ShowBlocked.BOTH) KamiMod.LOG.info("$chatName $name: $message")
    }
}
