package me.zeroeightsix.kami.module.modules.chat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.MessageDetectionHelper
import me.zeroeightsix.kami.util.MessageSendHelper
import net.minecraftforge.client.event.ClientChatReceivedEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer
import java.util.regex.Pattern
import java.util.stream.Collectors

/**
 * @author hub
 * @author dominikaaaa
 * Created 19 November 2019 by hub
 * Updated 12 January 2020 by hub
 * Updated 19 February 2020 by aUniqueUser
 * Updated by dominikaaaa on 19/04/20
 * Updated by Humboldt123 28/05/20 
 */
@Module.Info(
        name = "AntiSpam",
        category = Module.Category.CHAT,
        description = "Removes spam and advertising from the chat",
        showOnArray = Module.ShowOnArray.OFF
)
class AntiSpam : Module() {
    private val p = register(Settings.e<Page>("Page", Page.ONE))

    /* Page One */
    private val discordLinks = register(Settings.booleanBuilder("Discord Links").withValue(true).withVisibility { p.value == Page.ONE }.build())
    private val announcers = register(Settings.booleanBuilder("Announcers").withValue(true).withVisibility { p.value == Page.ONE }.build())
    private val spammers = register(Settings.booleanBuilder("Spammers").withValue(true).withVisibility { p.value == Page.ONE }.build())
    private val insulters = register(Settings.booleanBuilder("Insulters").withValue(true).withVisibility { p.value == Page.ONE }.build())
    private val greeters = register(Settings.booleanBuilder("Greeters").withValue(true).withVisibility { p.value == Page.ONE }.build())
    private val ips = register(Settings.booleanBuilder("Server Ips").withValue(true).withVisibility { p.value == Page.ONE }.build())
    private val specialCharEnding = register(Settings.booleanBuilder("Special Ending").withValue(true).withVisibility { p.value == Page.ONE }.build())
    private val specialCharBegin = register(Settings.booleanBuilder("Special Begin").withValue(true).withVisibility { p.value == Page.ONE }.build())
    private val iJustThanksTo = register(Settings.booleanBuilder("I just...thanks to").withValue(true).withVisibility { p.value == Page.ONE }.build())

    /* Page Two */
    private val ownsMeAndAll = register(Settings.booleanBuilder("Owns Me And All").withValue(true).withVisibility { p.value == Page.TWO }.build())
    private val greenText = register(Settings.booleanBuilder("Green Text").withValue(false).withVisibility { p.value == Page.TWO }.build())
    private val numberSuffix = register(Settings.booleanBuilder("Number Ending").withValue(false).withVisibility { p.value == Page.TWO }.build())
    private val numberPrefix = register(Settings.booleanBuilder("Number Begin").withValue(false).withVisibility { p.value == Page.TWO }.build())
    private val duplicates = register(Settings.booleanBuilder("Duplicates").withValue(true).withVisibility { p.value == Page.TWO }.build())
    private val duplicatesTimeout = register(Settings.integerBuilder("Duplicates Timeout").withMinimum(1).withValue(30).withMaximum(600).withVisibility { duplicates.value && p.value == Page.TWO }.build())
    private val webLinks = register(Settings.booleanBuilder("Web Links").withValue(false).withVisibility { p.value == Page.TWO }.build())
    private val filterOwn = register(Settings.booleanBuilder("Filter Own").withValue(false).withVisibility { p.value == Page.TWO }.build())
    private val filterDMs = register(Settings.booleanBuilder("Filter DMs").withValue(false).withVisibility { p.value == Page.TWO }.build())
    private val filterServer = register(Settings.booleanBuilder("Filter server").withValue(false).withVisibility { p.value == Page.TWO }.build())
    private val showBlocked = register(Settings.enumBuilder(ShowBlocked::class.java).withName("Show Blocked").withValue(ShowBlocked.LOG_FILE).withVisibility { p.value == Page.TWO }.build())
    private var messageHistory: ConcurrentHashMap<String, Long>? = null

    private enum class Page {
        ONE, TWO
    }

    private enum class ShowBlocked {
        NONE, LOG_FILE, CHAT, BOTH
    }

    @EventHandler
    private val listener = Listener(EventHook { event: ClientChatReceivedEvent ->
        if (mc.player == null) return@EventHook

        /* leijurv's sexy lambda to remove older entries in messageHistory */
        messageHistory!!.entries
            .stream()
            .filter { entry: Map.Entry<String, Long> -> entry.value < System.currentTimeMillis() - 10 * 60 * 1000 } // 10 is delay in minutes
            .collect(Collectors.toList())
            .forEach(Consumer { entry: Map.Entry<String, Long> -> messageHistory!!.remove(entry.key) })
        if (isSpam(event.message.unformattedText)) {
            event.isCanceled = true
        }
    })

    public override fun onEnable() {
        messageHistory = ConcurrentHashMap()
    }

    public override fun onDisable() {
        messageHistory = null
    }

    private fun isSpam(message: String): Boolean {
        /* Quick bandaid fix for mc.player being null when the module is being registered, so don't register it with the map */
        val ownMessage = "^<" + mc.player.name + "> "
        return if (!filterOwn.value && isOwn(ownMessage, message) || MessageDetectionHelper.isDirect(!filterDMs.value, message) || MessageDetectionHelper.isDirectOther(!filterDMs.value, message) || MessageDetectionHelper.isQueue(!filterServer.value, message) || MessageDetectionHelper.isRestart(!filterServer.value, message)) {
            false
        } else {
            detectSpam(removeUsername(message))
        }
    }

    private fun removeUsername(username: String): String {
        return username.replace("<[^>]*> ".toRegex(), "")
    }

    private fun detectSpam(message: String): Boolean {
        for ((key, value) in settingMap) {
            if (key.value && findPatterns(value, message)) {
                sendResult(key.name, message)
                return true
            }
        }

        if (duplicates.value) {
            if (messageHistory == null) messageHistory = ConcurrentHashMap()
            var isDuplicate = false

            if (messageHistory!!.containsKey(message) && (System.currentTimeMillis() - messageHistory!![message]!!) / 1000 < duplicatesTimeout.value) isDuplicate = true
            messageHistory!![message] = System.currentTimeMillis()

            if (isDuplicate) {
                if (showBlocked.value == ShowBlocked.CHAT || showBlocked.value == ShowBlocked.BOTH) MessageSendHelper.sendChatMessage("$chatName Duplicate: $message")
                if (showBlocked.value == ShowBlocked.LOG_FILE || showBlocked.value == ShowBlocked.BOTH) KamiMod.log.info("$chatName Duplicate: $message")
            }
        }
        return false
    }

    private fun isOwn(ownFilter: String, message: String): Boolean {
        return Pattern.compile(ownFilter, Pattern.CASE_INSENSITIVE).matcher(message).find()
    }

    private fun findPatterns(patterns: Array<String>, string: String): Boolean {
        var cString = string
        cString = cString.replace("<[^>]*> ".toRegex(), "") // remove username first
        for (pattern in patterns) {
            if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(cString).find()) {
                return true
            }
        }
        return false
    }

    private val settingMap: HashMap<Setting<Boolean>, Array<String>> = object : HashMap<Setting<Boolean>, Array<String>>() {
        init {
            put(greenText, FilterPatterns.GREEN_TEXT)
            put(specialCharBegin, FilterPatterns.SPECIAL_BEGINNING)
            put(specialCharEnding, FilterPatterns.SPECIAL_ENDING)
            put(ownsMeAndAll, FilterPatterns.OWNS_ME_AND_ALL)
            put(iJustThanksTo, FilterPatterns.I_JUST_THANKS_TO)
            put(numberSuffix, FilterPatterns.NUMBER_SUFFIX)
            put(numberPrefix, FilterPatterns.NUMBER_PREFIX)
            put(discordLinks, FilterPatterns.DISCORD)
            put(webLinks, FilterPatterns.WEB_LINK)
            put(ips, FilterPatterns.IP_ADDR)
            put(announcers, FilterPatterns.ANNOUNCER)
            put(spammers, FilterPatterns.SPAMMER)
            put(insulters, FilterPatterns.INSULTER)
            put(greeters, FilterPatterns.GREETER)
        }
    }

    private object FilterPatterns {
        val ANNOUNCER = arrayOf( // RusherHack b8
                "I just walked .+ feet!",
                "I just placed a .+!",
                "I just attacked .+ with a .+!",
                "I just dropped a .+!",
                "I just opened chat!",
                "I just opened my console!",
                "I just opened my GUI!",
                "I just went into full screen mode!",
                "I just paused my game!",
                "I just opened my inventory!",
                "I just looked at the player list!",
                "I just took a screen shot!",
                "I just swaped hands!",
                "I just ducked!",
                "I just changed perspectives!",
                "I just jumped!",
                "I just ate a .+!",
                "I just crafted .+ .+!",
                "I just picked up a .+!",
                "I just smelted .+ .+!",
                "I just respawned!",  // RusherHack b11
                "I just attacked .+ with my hands",
                "I just broke a .+!",  // WWE
                "I recently walked .+ blocks",
                "I just droped a .+ called, .+!",
                "I just placed a block called, .+!",
                "Im currently breaking a block called, .+!",
                "I just broke a block called, .+!",
                "I just opened chat!",
                "I just opened chat and typed a slash!",
                "I just paused my game!",
                "I just opened my inventory!",
                "I just looked at the player list!",
                "I just changed perspectives, now im in .+!",
                "I just crouched!",
                "I just jumped!",
                "I just attacked a entity called, .+ with a .+",
                "Im currently eatting a peice of food called, .+!",
                "Im currently using a item called, .+!",
                "I just toggled full screen mode!",
                "I just took a screen shot!",
                "I just swaped hands and now theres a .+ in my main hand and a .+ in my off hand!",
                "I just used pick block on a block called, .+!",
                "Ra just completed his blazing ark",
                "Its a new day yes it is",  // DotGod.CC
                "I just placed .+ thanks to (http:\\/\\/)?DotGod\\.CC!",
                "I just flew .+ meters like a butterfly thanks to (http:\\/\\/)?DotGod\\.CC!")
        val SPAMMER = arrayOf( //WWE
                "WWE Client's spammer",
                "Lol get gud",
                "Future client is bad",
                "WWE > Future",
                "WWE > Impact",
                "Default Message",
                "IKnowImEZ is a god",
                "THEREALWWEFAN231 is a god",
                "WWE Client made by IKnowImEZ/THEREALWWEFAN231",
                "WWE Client was the first public client to have Path Finder/New Chunks",
                "WWE Client was the first public client to have color signs",
                "WWE Client was the first client to have Teleport Finder",
                "WWE Client was the first client to have Tunneller & Tunneller Back Fill",
                "Zispanos") // This one is recent but it's annoying as FUCK.
        val INSULTER = arrayOf( // WWE
                ".+ Download WWE utility mod, Its free!",
                ".+ 4b4t is da best mintscreft serber",
                ".+ dont abouse",
                ".+ you cuck",
                ".+ https://www.youtube.com/channel/UCJGCNPEjvsCn0FKw3zso0TA",
                ".+ is my step dad",
                ".+ again daddy!",
                "dont worry .+ it happens to every one",
                ".+ dont buy future it's crap, compared to WWE!",
                "What are you, fucking gay, .+?",
                "Did you know? .+ hates you, .+",
                "You are literally 10, .+",
                ".+ finally lost their virginity, sadly they lost it to .+... yeah, that's unfortunate.",
                ".+, don't be upset, it's not like anyone cares about you, fag.",
                ".+, see that rubbish bin over there? Get your ass in it, or I'll get .+ to whoop your ass.",
                ".+, may I borrow that dirt block? that guy named .+ needs it...",
                "Yo, .+, btfo you virgin",
                "Hey .+ want to play some High School RP with me and .+?",
                ".+ is an Archon player. Why is he on here? Fucking factions player.",
                "Did you know? .+ just joined The Vortex Coalition!",
                ".+ has successfully conducted the cactus dupe and duped a itemhand!",
                ".+, are you even human? You act like my dog, holy shit.",
                ".+, you were never loved by your family.",
                "Come on .+, you hurt .+'s feelings. You meany.",
                "Stop trying to meme .+, you can't do that. kek",
                ".+, .+ is gay. Don't go near him.",
                "Whoa .+ didn't mean to offend you, .+.",
                ".+ im not pvping .+, im WWE'ing .+.",
                "Did you know? .+ just joined The Vortex Coalition!",
                ".+, are you even human? You act like my dog, holy shit.")
        val GREETER = arrayOf( // WWE
                "Bye, Bye .+",
                "Farwell, .+",  // Others(?)
                "See you next time, .+",
                "Catch ya later, .+",
                "Bye, .+",
                "Welcome, .+",
                "Hey, .+",  // Vanilla MC / Essentials MC
                ".+ joined the game",
                ".+ has joined",
                ".+ joined the lobby",
                "Welcome .+",
                ".+ left the game")
        val DISCORD = arrayOf(
                "discord.gg",
                "discordapp.com",
                "discord.io",
                "invite.gg")
        val NUMBER_SUFFIX = arrayOf(
                ".+\\d{3,}$")
        val NUMBER_PREFIX = arrayOf(
                "\\d{3,}.*$")
        val GREEN_TEXT = arrayOf(
                "^>.+$")
        val WEB_LINK = arrayOf(
                "http:\\/\\/",
                "https:\\/\\/",
                "www.")
        val IP_ADDR = arrayOf(
                "\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\:\\d{1,5}\\b",
                "\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}",
                "^(?:http(?:s)?:\\/\\/)?(?:[^\\.]+\\.)?.*\\..*\\..*$",
                ".*\\..*\\:\\d{1,5}$")
        val OWNS_ME_AND_ALL = arrayOf(
                "owns me and all")
        val I_JUST_THANKS_TO = arrayOf(
                "i just.*thanks to",
                "i just.*using")
        val SPECIAL_BEGINNING = arrayOf(
                "^[.,/?!()\\[\\]{}<|\\-+=\\\\]")
        val SPECIAL_ENDING = arrayOf(
                "[/@#^()\\[\\]{}<>|\\-+=\\\\]$")
    }

    private fun sendResult(name: String, message: String) {
        if (showBlocked.value == ShowBlocked.CHAT || showBlocked.value == ShowBlocked.BOTH) MessageSendHelper.sendChatMessage("$chatName $name: $message")
        if (showBlocked.value == ShowBlocked.LOG_FILE || showBlocked.value == ShowBlocked.BOTH) KamiMod.log.info("$chatName $name: $message")
    }
}
