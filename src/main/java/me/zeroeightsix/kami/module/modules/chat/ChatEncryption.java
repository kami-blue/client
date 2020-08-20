package me.zeroeightsix.kami.module.modules.chat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.text.TextComponentString;

import java.nio.CharBuffer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static me.zeroeightsix.kami.command.Command.getCommandPrefix;
import static me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage;
import static me.zeroeightsix.kami.util.text.MessageSendHelper.sendErrorMessage;

/**
 * Created by 086 on 9/04/2018.
 * Updated by dominikaaaa on 20/04/20
 */
@Module.Info(
        name = "ChatEncryption",
        description = "Encrypts and decrypts chat messages",
        category = Module.Category.CHAT,
        showOnArray = Module.ShowOnArray.OFF
)
public class ChatEncryption extends Module {

    private Setting<Boolean> self = register(Settings.b("DecryptOwn", true));
    private Setting<EncryptionMode> mode = register(Settings.e("Mode", EncryptionMode.SHUFFLE));
    private Setting<Integer> key = register(Settings.i("Key", 6));
    private Setting<Boolean> delim = register(Settings.b("Delimiter", true));
    public static Setting<String> delimiterValue = Settings.s("delimiterV", "unchanged");

    private final Pattern CHAT_PATTERN = Pattern.compile("<.*?> ");

    private static final char[] ORIGIN_CHARS = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '-', '_', '/', ';', '=', '?', '+', '\u00B5', '\u00A3', '*', '^', '\u00F9', '$', '!', '{', '}', '\'', '"', '|', '&'};

    @EventHandler
    private Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {
        if (event.getPacket() instanceof CPacketChatMessage) {
            String s = ((CPacketChatMessage) event.getPacket()).getMessage();
            if (delim.getValue()) {
                if (getDelimiter() == null || !s.startsWith(getDelimiter())) return;
                s = s.substring(1);
            }
            StringBuilder builder = new StringBuilder();
            switch (mode.getValue()) {
                case SHUFFLE:
                    builder.append(shuffle(key.getValue(), s));
                    builder.append("\uD83D\uDE4D");
                    break;
                case SHIFT:
                    s.chars().forEachOrdered(value -> builder.append((char) (value + (ChatAllowedCharacters.isAllowedCharacter((char) (value + key.getValue())) ? key.getValue() : 0))));
                    builder.append("\uD83D\uDE48");
                    break;
            }
            s = builder.toString();
            if (s.length() > 256) {
                sendChatMessage("Encrypted message length was too long, couldn't send!");
                event.cancel();
                return;
            }
            ((CPacketChatMessage) event.getPacket()).message = s;
        }
    });

    @EventHandler
    private Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {
        if (event.getPacket() instanceof SPacketChat) {
            String s = ((SPacketChat) event.getPacket()).getChatComponent().getUnformattedText();

            if (!self.getValue() && isOwn(s)) return;

            Matcher matcher = CHAT_PATTERN.matcher(s);
            String username = "unnamed";
            if (matcher.find()) {
                username = matcher.group();
                username = username.substring(1, username.length() - 2);
                s = matcher.replaceFirst("");
            }

            StringBuilder builder = new StringBuilder();
            String substring = s.substring(0, s.length() - 2);
            switch (mode.getValue()) {
                case SHUFFLE:
                    if (!s.endsWith("\uD83D\uDE4D")) return;
                    s = substring;
                    builder.append(unshuffle(key.getValue(), s));
                    break;
                case SHIFT:
                    if (!s.endsWith("\uD83D\uDE48")) return;
                    s = substring;
                    s.chars().forEachOrdered(value -> builder.append((char) (value + (ChatAllowedCharacters.isAllowedCharacter((char) value) ? -key.getValue() : 0))));
                    break;
            }

            ((SPacketChat) event.getPacket()).chatComponent = new TextComponentString("<" + username + "> " + KamiMod.colour + "lDECRYPTED" + KamiMod.colour + "r: " + builder.toString());
        }
    });

    private Map<Character, Character> generateShuffleMap(int seed) {
        Random r = new Random(seed);
        List<Character> characters = CharBuffer.wrap(ORIGIN_CHARS).chars().mapToObj(value -> (char) value).collect(Collectors.toList());
        List<Character> counter = new ArrayList<>(characters);
        Collections.shuffle(counter, r);

        Map<Character, Character> map = new LinkedHashMap<>();  // ordered
        for (int i = 0; i < characters.size(); i++) {
            map.put(characters.get(i), counter.get(i));
        }

        return map;
    }

    private String shuffle(int seed, String input) {
        Map<Character, Character> s = generateShuffleMap(seed);
        StringBuilder builder = new StringBuilder();
        swapCharacters(input, s, builder);
        return builder.toString();
    }

    private String unshuffle(int seed, String input) {
        Map<Character, Character> s = generateShuffleMap(seed);
        StringBuilder builder = new StringBuilder();
        swapCharacters(input, reverseMap(s), builder);
        return builder.toString();
    }

    private void swapCharacters(String input, Map<Character, Character> s, StringBuilder builder) {
        CharBuffer.wrap(input.toCharArray()).chars().forEachOrdered(value -> {
            char c = (char) value;
            if (s.containsKey(c)) builder.append(s.get(c));
            else
                builder.append(c);
        });
    }

    private static <K, V> Map<V, K> reverseMap(Map<K, V> map) {
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    private enum EncryptionMode {
        SHUFFLE, SHIFT
    }

    private boolean isOwn(String message) {
        return Pattern.compile("^<" + mc.player.getName() + "> ", Pattern.CASE_INSENSITIVE).matcher(message).find();
    }

    private String getDelimiter() {
        if (delimiterValue.getValue().equalsIgnoreCase("unchanged")) {
            sendErrorMessage(getChatName() + " Please change the delimiter with &7" + getCommandPrefix() + "chatencryption&f, disabling");
            disable();
            return null;
        }
        return delimiterValue.getValue();
    }
}
