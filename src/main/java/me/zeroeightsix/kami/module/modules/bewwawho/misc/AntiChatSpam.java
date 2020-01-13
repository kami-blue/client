package me.zeroeightsix.kami.module.modules.bewwawho.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.network.play.server.SPacketChat;

import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Created 19 November 2019 by hub
 * Updated 12 January 2020 by hub
 */
@Module.Info(name = "AntiSpam", category = Module.Category.MISC)
public class AntiChatSpam extends Module {

    private Setting<Boolean> greenText = register(Settings.b("Green Text", false));
    private Setting<Boolean> discordLinks = register(Settings.b("Discord Links", true));
    private Setting<Boolean> webLinks = register(Settings.b("Web Links", false));
    private Setting<Boolean> announcers = register(Settings.b("Announcers", true));
    private Setting<Boolean> spammers = register(Settings.b("Spammers", true));
    private Setting<Boolean> insulters = register(Settings.b("Insulters", true));
    private Setting<Boolean> greeters = register(Settings.b("Greeters", true));
    private Setting<Boolean> tradeChat = register(Settings.b("Trade Chat", true));
    private Setting<Boolean> numberSuffix = register(Settings.b("Number Suffix", true));
    private Setting<Boolean> duplicates = register(Settings.b("Duplicates", true));
    private Setting<Integer> duplicatesTimeout = register(Settings.integerBuilder("Duplicates Timeout").withMinimum(1).withValue(30).withMaximum(600).build());
    private Setting<Boolean> filterOwn = register(Settings.b("Filter Own", false));
    private Setting<Boolean> showBlocked = register(Settings.b("Show Blocked", false));

    private ConcurrentHashMap<String, Long> messageHistory;

    @EventHandler
    public Listener<PacketEvent.Receive> listener = new Listener<>(event -> {

        if (mc.player == null || this.isDisabled()) {
            return;
        }

        if (!(event.getPacket() instanceof SPacketChat)) {
            return;
        }
        SPacketChat sPacketChat = (SPacketChat) event.getPacket();

        // servers i test on did not send ChatType.CHAT for chat messages >:(
        /*if (!sPacketChat.getType().equals(ChatType.CHAT)) {
            return;
        }*/

        if (detectSpam(sPacketChat.getChatComponent().getUnformattedText())) {
            event.cancel();
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

    private boolean detectSpam(String message) {

        if (!filterOwn.getValue() && findPatterns(FilterPatterns.OWN_MESSAGE, message)) {
            return false;
        }

        if (greenText.getValue() && findPatterns(FilterPatterns.GREEN_TEXT, message)) {
            if (showBlocked.getValue()) {
                Command.sendChatMessage("[AntiSpam] Green Text: " + message);
            }
            return true;
        }

        if (discordLinks.getValue() && findPatterns(FilterPatterns.DISCORD, message)) {
            if (showBlocked.getValue()) {
                Command.sendChatMessage("[AntiSpam] Discord Link: " + message);
            }
            return true;
        }

        if (webLinks.getValue() && findPatterns(FilterPatterns.WEB_LINK, message)) {
            if (showBlocked.getValue()) {
                Command.sendChatMessage("[AntiSpam] Web Link: " + message);
            }
            return true;
        }

        if (tradeChat.getValue() && findPatterns(FilterPatterns.TRADE_CHAT, message)) {
            if (showBlocked.getValue()) {
                Command.sendChatMessage("[AntiSpam] Trade Chat: " + message);
            }
            return true;
        }

        if (numberSuffix.getValue() && findPatterns(FilterPatterns.NUMBER_SUFFIX, message)) {
            if (showBlocked.getValue()) {
                Command.sendChatMessage("[AntiSpam] Number Suffix: " + message);
            }
            return true;
        }

        if (announcers.getValue() && findPatterns(FilterPatterns.ANNOUNCER, message)) {
            if (showBlocked.getValue()) {
                Command.sendChatMessage("[AntiSpam] Announcer: " + message);
            }
            return true;
        }

        if (spammers.getValue() && findPatterns(FilterPatterns.SPAMMER, message)) {
            if (showBlocked.getValue()) {
                Command.sendChatMessage("[AntiSpam] Spammers: " + message);
            }
            return true;
        }

        if (insulters.getValue() && findPatterns(FilterPatterns.INSULTER, message)) {
            if (showBlocked.getValue()) {
                Command.sendChatMessage("[AntiSpam] Insulter: " + message);
            }
            return true;
        }

        if (greeters.getValue() && findPatterns(FilterPatterns.GREETER, message)) {
            if (showBlocked.getValue()) {
                Command.sendChatMessage("[AntiSpam] Greeter: " + message);
            }
            return true;
        }

        if (duplicates.getValue()) {
            if (messageHistory == null) {
                messageHistory = new ConcurrentHashMap<>();
            }
            boolean isDuplicate = false;
            if (messageHistory.containsKey(message) && (System.currentTimeMillis() - messageHistory.get(message)) / 1000 < duplicatesTimeout.getValue()) {
                isDuplicate = true;
            }
            messageHistory.put(message, System.currentTimeMillis());
            if (isDuplicate) {
                if (showBlocked.getValue()) {
                    Command.sendChatMessage("[AntiSpam] Duplicate: " + message);
                }
                return true;
            }
        }

        return false;

    }

    private boolean findPatterns(String[] patterns, String string) {

        for (String pattern : patterns) {

            if (Pattern.compile(pattern).matcher(string).find()) {
                return true;
            }

        }

        return false;

    }

    private static class FilterPatterns {

        private static final String[] ANNOUNCER =
                {
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

        private static final String[] SPAMMER =
                {
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

        private static final String[] INSULTER =
                {
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

        private static final String[] GREETER =
                {
                        // WWE
                        "Bye, Bye .+",
                        "Farwell, .+",
                        // incomplete
                };

        private static final String[] DISCORD =
                {
                        "discord.gg",
                        "discordapp.com",
                        "discord.io",
                        "invite.gg",
                };

        private static final String[] NUMBER_SUFFIX =
                {
                        ".+\\d{3,}$",
                };

        private static final String[] GREEN_TEXT =
                {
                        "^<.+> >",
                };

        private static final String[] TRADE_CHAT =
                {
                        "buy",
                        "sell",
                };

        private static final String[] WEB_LINK =
                {
                        "http:\\/\\/",
                        "https:\\/\\/",
                };

        private static final String[] OWN_MESSAGE =
                {
                        "^<" + mc.player.getName() + "> ",
                        "^To .+: ",
                };
    }
}