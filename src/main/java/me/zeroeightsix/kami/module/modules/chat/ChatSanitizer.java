package me.zeroeightsix.kami.module.modules.chat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Module.Info(
        name = "ChatSanitizer",
        description = "Cleans up chat messages instead of blocking them entirely.",
        category = Module.Category.CHAT,
        showOnArray = Module.ShowOnArray.OFF
)
public class ChatSanitizer extends Module {
    private final Setting<Boolean> sanitizeOwnMessages = register(Settings.b("SanitizeOwn", false));
    private final Setting<Boolean> slurs = register(Settings.b("Slurs", true));
    private final Setting<Boolean> swears = register(Settings.b("Swears", false));
    private final Setting<Boolean> aggressiveFilter = register(Settings.b("AggressiveFilter", false));
    private final Setting<ReplacementOptions> replacement = register(Settings.enumBuilder(ReplacementOptions.class).withName("Replacement").withValue(ReplacementOptions.Asterisk));

    private final Setting<Boolean> experimental = register(Settings.b("Experimental", false));
    private final Setting<Boolean> fancyChat = register(Settings.booleanBuilder("FancyChat").withValue(false).withVisibility(v -> experimental.getValue()).build());
    private final Setting<Boolean> suffixes = register(Settings.booleanBuilder("Suffixes").withValue(false).withVisibility(v -> experimental.getValue()).build());


    @EventHandler
    public Listener<ClientChatReceivedEvent> listener = new Listener<>(event -> {
        if (mc.player == null){
            return;
        }

        String message = removeUsername(event.getMessage().getUnformattedText());
        if(isOwn(message) && !sanitizeOwnMessages.getValue()){
            return;
        }

        if(suffixes.getValue()){
            message = sanitizeSuffixes(message);
        }

        if(fancyChat.getValue()){
            message = sanitizeFancyChat(message);

            if(message.trim().length() == 0){ // prevent empty messages and associated confusion
                message = "[Fancychat]";
            }
        }

        if(slurs.getValue()){
            message = sanitize(message, slurMatchers, replacement.getValue().getReplacement());
        }

        if(swears.getValue()){
            message = sanitize(message, swearMatchers, replacement.getValue().getReplacement());
        }
        String oldMessage = removeUsername(event.getMessage().getUnformattedText());
        if(!oldMessage.equals(message)){

            if(message.length() > 256){ // I'm not sure if message length matters at this point, but this is just for peace of mind
                message = message.substring(0, 256);
            }
            event.setMessage(new TextComponentString(getUsername(event.getMessage().getUnformattedText()) + " " + message));
        }

    });

    private String sanitize(String toClean, String[] matchers, String replacement){

        if (!aggressiveFilter.getValue()) {
            for(String matcher : matchers){
                toClean = toClean.replaceAll("\\b" + matcher + "|" + matcher + "\\b", replacement); // only check for start or end of a word
            }
        } else { // We might encounter the scunthorpe problem, so aggressive mode is off by default.
            for(String matcher : matchers){
                toClean = toClean.replaceAll(matcher, replacement);
            }
        }
        return toClean;
    }

    private static String sanitizeSuffixes(String toClean){
        return toClean.replaceAll("[\\u23D0|«].+$", "");
    }

    private static String sanitizeFancyChat(String toClean){
        return toClean.replaceAll("[^\\u0000-\\u007F]", "");
    }

    private enum ReplacementOptions {
        Asterisk("****"), Delete(""), Redact("[Redacted]");

        String replacement;

        ReplacementOptions(String s) {
            replacement = s;
        }
        public String getReplacement() {
            return replacement;
        }

    }

    //fairly simple - anarchy servers shouldn't have a chat filter so there isn't an attempt to check for bypasses
    private static final String[] slurMatchers = new String[]{"nigg(a|er)", "chink", "trann(y|ie)", "kike", "fag(got)?", "retard(ed)?"};

    private static final String[] swearMatchers = new String[]{"fuck(er)?", "shit", "cunt", "puss(ie|y)", "bitch", "twat"};

    private String getUsername(String rawMessage) {
        Matcher matcher = Pattern.compile("<[^>]*>", Pattern.CASE_INSENSITIVE).matcher(rawMessage);
        if(matcher.find()){
            return matcher.group();
        } else {
            return rawMessage.substring(0, rawMessage.indexOf(">")); // a bit hacky
        }
    }

    // Both borrowed from AntiSpam.kt
    private boolean isOwn(String message) {
        String ownFilter = "^<" + mc.player.getName() + "> ";
        return Pattern.compile(ownFilter, Pattern.CASE_INSENSITIVE).matcher(message).find();
    }

    private String removeUsername(String rawMessage) {
        return rawMessage.replaceFirst("<[^>]*> ", "");
    }


}
