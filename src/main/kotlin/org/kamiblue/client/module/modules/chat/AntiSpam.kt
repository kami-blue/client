package org.kamiblue.client.module.modules.chat

import net.minecraft.util.text.TextComponentString
import net.minecraftforge.client.event.ClientChatReceivedEvent
import org.kamiblue.client.KamiMod
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
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
    private val mode = setting("Mode", Mode.REPLACE)
    private val replaceMode = setting("Replace Mode", ReplaceMode.ASTERISKS, { mode.value == Mode.REPLACE })
    private val page = setting("Page", Page.TYPE)

    /* Page One */
    private val discordLinks = setting("Discord", true, { page.value == Page.TYPE })
    private val slurs = setting("Slurs", true, { page.value == Page.TYPE })
    private val swears = setting("Swears", false, { page.value == Page.TYPE })
    private val automated = setting("Automated", true, { page.value == Page.TYPE })
    private val ips = setting("Server Ips", true, { page.value == Page.TYPE })
    private val specialCharEnding = setting("Special Ending", true, { page.value == Page.TYPE })
    private val specialCharBegin = setting("Special Begin", true, { page.value == Page.TYPE })
    private val greenText = setting("Green Text", false, { page.value == Page.TYPE })
    private val fancyChat = setting("Fancy Chat", false, { page.value == Page.TYPE })

    /* Page Two */
    private val aggressiveFiltering = setting("Aggressive Filtering", true, { page.value == Page.SETTINGS })
    private val duplicates = setting("Duplicates", true, { page.value == Page.SETTINGS })
    private val duplicatesTimeout = setting("Duplicates Timeout", 30, 1..600, 5, { duplicates.value && page.value == Page.SETTINGS })
    private val filterOwn = setting("Filter Own", false, { page.value == Page.SETTINGS })
    private val filterDMs = setting("Filter DMs", false, { page.value == Page.SETTINGS })
    private val filterServer = setting("Filter Server", false, { page.value == Page.SETTINGS })
    private val showBlocked = setting("Show Blocked", ShowBlocked.LOG_FILE, { page.value == Page.SETTINGS })

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
        onDisable {
            messageHistory.clear()
        }

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
                    event.message = TextComponentString(sanitize(event.message.formattedText, pattern, replaceMode.value.redaction))
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

    private fun sanitize(toClean: String, matcher: String, replacement: String): String {
        return if (!aggressiveFiltering.value) {
            toClean.replace("\\b$matcher|$matcher\\b".toRegex(), replacement) // only check for start or end of a word
        } else { // We might encounter the scunthorpe problem, so aggressive mode is off by default.
            toClean.replace(matcher.toRegex(), replacement)
        }
    }

    private fun isSpam(message: String): String? {
        return if (!filterOwn.value && isOwn(message)
            || !filterDMs.value && MessageDetection.Direct.ANY detect message
            || !filterServer.value && MessageDetection.Server.ANY detect message) {
            null
        } else {
            detectSpam(removeUsername(message))
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
