package org.kamiblue.client.module.modules.chat

import net.minecraft.util.text.TextComponentString
import net.minecraftforge.client.event.ClientChatReceivedEvent
import org.kamiblue.client.KamiMod
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.and
import org.kamiblue.client.util.atTrue
import org.kamiblue.client.util.atValue
import org.kamiblue.client.util.text.*
import org.kamiblue.event.listener.listener
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

internal object AntiSpam : Module(
    name = "AntiSpam",
    category = Category.CHAT,
    description = "Removes spam and advertising from the chat",
    showOnArray = false
) {
    private val mode0 = setting("Mode", Mode.REPLACE)
    private val mode by mode0
    private val replaceMode by setting("Replace Mode", ReplaceMode.ASTERISKS, mode0.atValue(Mode.REPLACE))
    private val page = setting("Page", Page.TYPE)

    /* Page One */
    private val discordLinks = setting("Discord", true, page.atValue(Page.TYPE))
    private val slurs = setting("Slurs", true, page.atValue(Page.TYPE))
    private val swears = setting("Swears", false, page.atValue(Page.TYPE))
    private val automated = setting("Automated", true, page.atValue(Page.TYPE))
    private val ips = setting("Server Ips", true, page.atValue(Page.TYPE))
    private val specialCharEnding = setting("Special Ending", true, page.atValue(Page.TYPE))
    private val specialCharBegin = setting("Special Begin", true, page.atValue(Page.TYPE))
    private val greenText = setting("Green Text", false, page.atValue(Page.TYPE))
    private val fancyChat by setting("Fancy Chat", false, page.atValue(Page.TYPE))

    /* Page Two */
    private val aggressiveFiltering by setting("Aggressive Filtering", true, page.atValue(Page.SETTINGS))
    private val duplicates0 = setting("Duplicates", true, page.atValue(Page.SETTINGS))
    private val duplicates by duplicates0
    private val duplicatesTimeout by setting("Duplicates Timeout", 30, 1..600, 5, page.atValue(Page.SETTINGS) and duplicates0.atTrue())
    private val filterOwn by setting("Filter Own", false, page.atValue(Page.SETTINGS))
    private val filterDMs by setting("Filter DMs", false, page.atValue(Page.SETTINGS))
    private val filterServer by setting("Filter Server", false, page.atValue(Page.SETTINGS))
    private val showBlocked by setting("Show Blocked", ShowBlocked.LOG_FILE, page.atValue(Page.SETTINGS))

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
    private val settingArray = arrayOf(
        discordLinks to SpamFilters.discordInvite,
        slurs to SpamFilters.slurs,
        swears to SpamFilters.swears,
        automated to SpamFilters.announcer,
        automated to SpamFilters.spammer,
        automated to SpamFilters.insulter,
        automated to SpamFilters.greeter,
        automated to SpamFilters.ownsMeAndAll,
        automated to SpamFilters.thanksTo,
        ips to SpamFilters.ipAddress,
        specialCharBegin to SpamFilters.specialBeginning,
        specialCharEnding to SpamFilters.specialEnding,
        greenText to SpamFilters.greenText,
    )

    init {
        onDisable {
            messageHistory.clear()
        }

        listener<ClientChatReceivedEvent> { event ->
            if (mc.player == null) return@listener

            messageHistory.values.removeIf { System.currentTimeMillis() - it > 600000 }

            if (duplicates && checkDupes(event.message.unformattedText)) {
                event.isCanceled = true
            }

            val pattern = isSpam(event.message.unformattedText)

            if (pattern != null) { // null means no pattern found
                if (mode == Mode.HIDE) {
                    event.isCanceled = true
                } else if (mode == Mode.REPLACE) {
                    event.message = TextComponentString(sanitize(event.message.formattedText, pattern, replaceMode.redaction))
                }
            }

            if (fancyChat) {
                val message = sanitizeFancyChat(event.message.unformattedText)
                if (message.trim { it <= ' ' }.isEmpty()) { // this should be removed if we are going for an intelligent de-fancy
                    event.message = TextComponentString(getUsername(event.message.unformattedText) + " [Fancychat]")
                }
            }
        }
    }

    private fun sanitize(toClean: String, matcher: String, replacement: String): String {
        return if (!aggressiveFiltering) {
            toClean.replace("\\b$matcher|$matcher\\b".toRegex(), replacement) // only check for start or end of a word
        } else { // We might encounter the scunthorpe problem, so aggressive mode is off by default.
            toClean.replace(matcher.toRegex(), replacement)
        }
    }

    private fun isSpam(message: String): String? {
        return if (!filterOwn && isOwn(message)
            || !filterDMs && MessageDetection.Direct.ANY detect message
            || !filterServer && MessageDetection.Server.ANY detect message) {
            null
        } else {
            detectSpam(removeUsername(message))
        }
    }

    private fun detectSpam(message: String): String? {
        for ((setting, strings) in settingArray) {
            val pattern = findPatterns(strings, message)

            if (setting.value && pattern != null) {
                sendResult(setting.name, message)
                return pattern
            }
        }
        return null
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

    private fun checkDupes(message: String): Boolean {
        var isDuplicate = false

        if (messageHistory.containsKey(message) && (System.currentTimeMillis() - messageHistory[message]!!) / 1000 < duplicatesTimeout) isDuplicate = true
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
        if (showBlocked == ShowBlocked.CHAT || showBlocked == ShowBlocked.BOTH) MessageSendHelper.sendChatMessage("$chatName $name: $message")
        if (showBlocked == ShowBlocked.LOG_FILE || showBlocked == ShowBlocked.BOTH) KamiMod.LOG.info("$chatName $name: $message")
    }
}
