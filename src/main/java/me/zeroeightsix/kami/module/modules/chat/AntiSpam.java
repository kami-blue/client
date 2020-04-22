package me.zeroeightsix.kami.module.modules.chat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static me.zeroeightsix.kami.util.MessageDetectionHelper.isDirect;
import static me.zeroeightsix.kami.util.MessageDetectionHelper.isDirectOther;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage;

/**
 * @author hub
 * @author dominikaaaa
 * Created 19 November 2019 by hub
 * Updated 12 January 2020 by hub
 * Updated 19 February 2020 by aUniqueUser
 * Updated by dominikaaaa on 19/04/20
 */
@Module.Info(
        name = "AntiSpam",
        category = Module.Category.CHAT,
        description = "Removes spam and advertising from the chat",
        showOnArray = Module.ShowOnArray.OFF
)
public class AntiSpam extends Module {

    private Setting<Page> p = register(Settings.e("Page", Page.ONE));
    /* Page One */
    private Setting<Boolean> discordLinks = register(Settings.booleanBuilder("Discord Links").withValue(true).withVisibility(v -> p.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> announcers = register(Settings.booleanBuilder("Announcers").withValue(true).withVisibility(v -> p.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> spammers = register(Settings.booleanBuilder("Spammers").withValue(true).withVisibility(v -> p.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> insulters = register(Settings.booleanBuilder("Insulters").withValue(true).withVisibility(v -> p.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> greeters = register(Settings.booleanBuilder("Greeters").withValue(true).withVisibility(v -> p.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> ips = register(Settings.booleanBuilder("Server Ips").withValue(true).withVisibility(v -> p.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> specialCharEnding = register(Settings.booleanBuilder("Special Ending").withValue(true).withVisibility(v -> p.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> specialCharBegin = register(Settings.booleanBuilder("Special Begin").withValue(true).withVisibility(v -> p.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> iJustThanksTo = register(Settings.booleanBuilder("I just...thanks to").withValue(true).withVisibility(v -> p.getValue().equals(Page.ONE)).build());
    /* I can't get settings to work in non static context for filter */
//    private Setting<Integer> characters = register(Settings.integerBuilder("Characters").withValue(15).withVisibility(v -> wordsLongerThen.getValue() && p.getValue().equals(Page.ONE)).build());

    /* Page Two */
    private Setting<Boolean> ownsMeAndAll = register(Settings.booleanBuilder("Owns Me And All").withValue(true).withVisibility(v -> p.getValue().equals(Page.TWO)).build());
    private Setting<Boolean> greenText = register(Settings.booleanBuilder("Green Text").withValue(false).withVisibility(v -> p.getValue().equals(Page.TWO)).build());
    private Setting<Boolean> numberSuffix = register(Settings.booleanBuilder("Number Ending").withValue(false).withVisibility(v -> p.getValue().equals(Page.TWO)).build());
    private Setting<Boolean> numberPrefix = register(Settings.booleanBuilder("Number Begin").withValue(false).withVisibility(v -> p.getValue().equals(Page.TWO)).build());
    private Setting<Boolean> duplicates = register(Settings.booleanBuilder("Duplicates").withValue(true).withVisibility(v -> p.getValue().equals(Page.TWO)).build());
    private Setting<Integer> duplicatesTimeout = register(Settings.integerBuilder("Duplicates Timeout").withMinimum(1).withValue(30).withMaximum(600).withVisibility(v -> duplicates.getValue() && p.getValue().equals(Page.TWO)).build());
    private Setting<Boolean> webLinks = register(Settings.booleanBuilder("Web Links").withValue(false).withVisibility(v -> p.getValue().equals(Page.TWO)).build());
    private Setting<Boolean> filterOwn = register(Settings.booleanBuilder("Filter Own").withValue(false).withVisibility(v -> p.getValue().equals(Page.TWO)).build());
    private Setting<Boolean> filterDMs = register(Settings.booleanBuilder("Filter DMs").withValue(false).withVisibility(v -> p.getValue().equals(Page.TWO)).build());
    private Setting<ShowBlocked> showBlocked = register(Settings.enumBuilder(ShowBlocked.class).withName("Show Blocked").withValue(ShowBlocked.LOG_FILE).withVisibility(v -> p.getValue().equals(Page.TWO)).build());

    private ConcurrentHashMap<String, Long> messageHistory;
    private enum Page { ONE, TWO }
    private enum ShowBlocked { NONE, LOG_FILE, CHAT }

    @EventHandler
    public Listener<ClientChatReceivedEvent> listener = new Listener<>(event -> {
        if (mc.player == null) return;

        /* leijurv's sexy lambda to remove older entries in messageHistory */
        messageHistory.entrySet()
                .stream()
                .filter(entry -> entry.getValue() < System.currentTimeMillis() - 10 * 60 * 1000) // 10 is delay in minutes
                .collect(Collectors.toList())
                .forEach(entry -> messageHistory.remove(entry.getKey()));

        if (isSpam(event.getMessage().getUnformattedText())) {
            event.setCanceled(true);
        }
    });

    @Override
    public void onEnable() {
        messageHistory = new ConcurrentHashMap<>();
    }

    @Override
    public void onDisable() {
        messageHistory = null;
    }

    private boolean isSpam(String message) {
        /* Quick bandaid fix for mc.player being null when the module is being registered, so don't register it with the map */
        final String OWN_MESSAGE = "^<" + mc.player.getName() + "> ";
        if ((!filterOwn.getValue() && isOwn(OWN_MESSAGE, message)) || isDirect(filterDMs.getValue(), message) || isDirectOther(filterDMs.getValue(), message)) {
            return false;
        } else {
            return detectSpam(removeUsername(message));
        }
    }


    private String removeUsername(String username) {
        return username.replaceAll("<[^>]*> ", "");
    }

    private boolean detectSpam(String message) {

        for (Map.Entry<Setting<Boolean>, String[]> entry : settingMap.entrySet()) {
            if (entry.getKey().getValue() && findPatterns(entry.getValue(), message)) {
                sendResult(entry.getKey().getName(), message);
                return true;
            }
        }

        if (duplicates.getValue()) {
            if (messageHistory == null) messageHistory = new ConcurrentHashMap<>();
            boolean isDuplicate = false;

            if (messageHistory.containsKey(message) && (System.currentTimeMillis() - messageHistory.get(message)) / 1000 < duplicatesTimeout.getValue())
                isDuplicate = true;

            messageHistory.put(message, System.currentTimeMillis());
            if (isDuplicate) {
                if (showBlocked.getValue().equals(ShowBlocked.CHAT))
                    sendChatMessage(getChatName() + "Duplicate: " + message);
                else if (showBlocked.getValue().equals(ShowBlocked.LOG_FILE))
                    KamiMod.log.info(getChatName() + "Duplicate: " + message);
            }
        }

        return false;
    }

    private boolean isOwn(String ownFilter, String message) {
        return Pattern.compile(ownFilter, Pattern.CASE_INSENSITIVE).matcher(message).find();
    }

    private boolean findPatterns(String[] patterns, String string) {
        string = string.replaceAll("<[^>]*> ", ""); // remove username first
        for (String pattern : patterns) {
            if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(string).find()) {
                return true;
            }
        }
        return false;
    }

    private Map<Setting<Boolean>, String[]> settingMap = new HashMap<Setting<Boolean>, String[]>() {{
        put(greenText, FilterPatterns.GREEN_TEXT);
        put(specialCharBegin, FilterPatterns.SPECIAL_BEGINNING);
        put(specialCharEnding, FilterPatterns.SPECIAL_ENDING);
        put(specialCharBegin, FilterPatterns.SPECIAL_BEGINNING);
        put(ownsMeAndAll, FilterPatterns.OWNS_ME_AND_ALL);
        put(iJustThanksTo, FilterPatterns.I_JUST_THANKS_TO);
        put(numberSuffix, FilterPatterns.NUMBER_SUFFIX);
        put(numberPrefix, FilterPatterns.NUMBER_PREFIX);
        put(discordLinks, FilterPatterns.DISCORD);
        put(webLinks, FilterPatterns.WEB_LINK);
        put(ips, FilterPatterns.IP_ADDR);
        put(announcers, FilterPatterns.ANNOUNCER);
        put(spammers, FilterPatterns.SPAMMER);
        put(insulters, FilterPatterns.INSULTER);
        put(greeters, FilterPatterns.GREETER);
    }};

    private static class FilterPatterns {
        private static final String[] ANNOUNCER = {
                // RusherHack b8
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
                "I just respawned!",
                // RusherHack b11
                "I just attacked .+ with my hands",
                "I just broke a .+!",
                // WWE
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
                "Its a new day yes it is",
                // DotGod.CC
                "I just placed .+ thanks to (http:\\/\\/)?DotGod\\.CC!",
                "I just flew .+ meters like a butterfly thanks to (http:\\/\\/)?DotGod\\.CC!",
        };

        private static final String[] SPAMMER = {
                //WWE
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
        };

        private static final String[] INSULTER = {
                // WWE
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
                ".+, are you even human? You act like my dog, holy shit.",
        };

        private static final String[] GREETER = {
                // WWE
                "Bye, Bye .+",
                "Farwell, .+",
                // Others(?)
                "See you next time, .+",
                "Catch ya later, .+",
                "Bye, .+",
                "Welcome, .+",
                "Hey, .+",
                // Vanilla MC / Essentials MC
                ".+ joined the game",
                ".+ has joined",
                ".+ joined the lobby",
                "Welcome .+",
                ".+ left the game",
        };

        private static final String[] DISCORD = {
                "discord.gg",
                "discordapp.com",
                "discord.io",
                "invite.gg",
        };

        private static final String[] NUMBER_SUFFIX = {
                ".+\\d{3,}$",
        };

        private static final String[] NUMBER_PREFIX = {
                "\\d{3,}.*$",
        };

        private static final String[] GREEN_TEXT = {
                "^>.+$",
        };

        private static final String[] WEB_LINK = {
                "http:\\/\\/",
                "https:\\/\\/",
                "www.",
        };

        private static final String[] IP_ADDR = {
                "\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\:\\d{1,5}\\b",
                "\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}",
                "^(?:http(?:s)?:\\/\\/)?(?:[^\\.]+\\.)?.*\\..*\\..*$",
                ".*\\..*\\:\\d{1,5}$",
        };

        private static final String[] OWNS_ME_AND_ALL = {
                "owns me and all",
        };

        private static final String[] I_JUST_THANKS_TO = {
                "i just.*thanks to",
                "i just.*using",
        };

        private static final String[] SPECIAL_BEGINNING = {
                "^[.,/?!()\\[\\]{}<>|\\-+=\\\\]", // the <> don't filter as the player name is removed when matching
        };

        private static final String[] SPECIAL_ENDING = {
                "[/@#^()\\[\\]{}<>|\\-+=\\\\]$",
        };
    }

    private void sendResult(String name, String message) {
        if (showBlocked.getValue().equals(ShowBlocked.CHAT)) sendChatMessage(getChatName() + name + ": " + message);
        else if (showBlocked.getValue().equals(ShowBlocked.LOG_FILE))
            KamiMod.log.info(getChatName() + name + ": " + message);
    }
}
