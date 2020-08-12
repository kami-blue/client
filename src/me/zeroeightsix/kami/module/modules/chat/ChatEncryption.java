// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.chat;

import net.minecraft.util.text.ITextComponent;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.Collection;
import java.util.ArrayList;
import java.util.stream.Collector;
import java.nio.CharBuffer;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.regex.Matcher;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.network.play.server.SPacketChat;
import java.util.function.Predicate;
import me.zeroeightsix.kami.command.Command;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.network.play.client.CPacketChatMessage;
import me.zeroeightsix.kami.setting.Settings;
import me.zero.alpine.listener.EventHandler;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.setting.Setting;
import java.util.regex.Pattern;
import me.zeroeightsix.kami.module.Module;

@Info(name = "ChatEncryption", description = "Encrypts and decrypts chat messages (Delimiter %)", category = Category.CHAT)
public class ChatEncryption extends Module
{
    private static final char[] ORIGIN_CHARS;
    private final Pattern CHAT_PATTERN;
    private Setting<EncryptionMode> mode;
    private Setting<Integer> key;
    private Setting<Boolean> delim;
    @EventHandler
    private Listener<PacketEvent.Send> sendListener;
    @EventHandler
    private Listener<PacketEvent.Receive> receiveListener;
    
    public ChatEncryption() {
        this.CHAT_PATTERN = Pattern.compile("<.*?> ");
        this.mode = this.register(Settings.e("Mode", EncryptionMode.SHUFFLE));
        this.key = this.register(Settings.i("Key", 6));
        this.delim = this.register(Settings.b("Delimiter", true));
        String s;
        StringBuilder builder;
        String s2;
        this.sendListener = new Listener<PacketEvent.Send>(event -> {
            if (event.getPacket() instanceof CPacketChatMessage) {
                s = ((CPacketChatMessage)event.getPacket()).func_149439_c();
                if (this.delim.getValue()) {
                    if (!s.startsWith("%")) {
                        return;
                    }
                    else {
                        s = s.substring(1);
                    }
                }
                builder = new StringBuilder();
                switch (this.mode.getValue()) {
                    case SHUFFLE: {
                        builder.append(this.shuffle(this.key.getValue(), s));
                        builder.append("\ud83d\ude4d");
                        break;
                    }
                    case SHIFT: {
                        s.chars().forEachOrdered(value -> builder.append((char)(value + (ChatAllowedCharacters.func_71566_a((char)(value + this.key.getValue())) ? this.key.getValue() : 0))));
                        builder.append("\ud83d\ude48");
                        break;
                    }
                }
                s2 = builder.toString();
                if (s2.length() > 256) {
                    Command.sendChatMessage("Encrypted message length was too long, couldn't send!");
                    event.cancel();
                }
                else {
                    ((CPacketChatMessage)event.getPacket()).field_149440_a = s2;
                }
            }
            return;
        }, (Predicate<PacketEvent.Send>[])new Predicate[0]);
        String s3;
        Matcher matcher;
        String username;
        String username2;
        StringBuilder builder2;
        String s4;
        String s5;
        SPacketChat sPacketChat;
        final TextComponentString field_148919_a;
        this.receiveListener = new Listener<PacketEvent.Receive>(event -> {
            if (event.getPacket() instanceof SPacketChat) {
                s3 = ((SPacketChat)event.getPacket()).func_148915_c().func_150260_c();
                matcher = this.CHAT_PATTERN.matcher(s3);
                username = "unnamed";
                if (matcher.find()) {
                    username2 = matcher.group();
                    username = username2.substring(1, username2.length() - 2);
                    s3 = matcher.replaceFirst("");
                }
                builder2 = new StringBuilder();
                switch (this.mode.getValue()) {
                    case SHUFFLE: {
                        if (!s3.endsWith("\ud83d\ude4d")) {
                            return;
                        }
                        else {
                            s4 = s3.substring(0, s3.length() - 2);
                            builder2.append(this.unshuffle(this.key.getValue(), s4));
                            break;
                        }
                        break;
                    }
                    case SHIFT: {
                        if (!s3.endsWith("\ud83d\ude48")) {
                            return;
                        }
                        else {
                            s5 = s3.substring(0, s3.length() - 2);
                            s5.chars().forEachOrdered(value -> builder2.append((char)(value + (ChatAllowedCharacters.func_71566_a((char)value) ? (-this.key.getValue()) : 0))));
                            break;
                        }
                        break;
                    }
                }
                sPacketChat = (SPacketChat)event.getPacket();
                new TextComponentString(Command.SECTIONSIGN() + "b" + username + Command.SECTIONSIGN() + "r: " + builder2.toString());
                sPacketChat.field_148919_a = (ITextComponent)field_148919_a;
            }
        }, (Predicate<PacketEvent.Receive>[])new Predicate[0]);
    }
    
    private static <K, V> Map<V, K> reverseMap(final Map<K, V> map) {
        return map.entrySet().stream().collect(Collectors.toMap((Function<? super Object, ? extends V>)Map.Entry::getValue, (Function<? super Object, ? extends K>)Map.Entry::getKey));
    }
    
    private Map<Character, Character> generateShuffleMap(final int seed) {
        final Random r = new Random(seed);
        final List<Character> characters = CharBuffer.wrap(ChatEncryption.ORIGIN_CHARS).chars().mapToObj(value -> Character.valueOf((char)value)).collect((Collector<? super Object, ?, List<Character>>)Collectors.toList());
        final List<Character> counter = new ArrayList<Character>(characters);
        Collections.shuffle(counter, r);
        final Map<Character, Character> map = new LinkedHashMap<Character, Character>();
        for (int i = 0; i < characters.size(); ++i) {
            map.put(characters.get(i), counter.get(i));
        }
        return map;
    }
    
    private String shuffle(final int seed, final String input) {
        final Map<Character, Character> s = this.generateShuffleMap(seed);
        final StringBuilder builder = new StringBuilder();
        this.swapCharacters(input, s, builder);
        return builder.toString();
    }
    
    private String unshuffle(final int seed, final String input) {
        final Map<Character, Character> s = this.generateShuffleMap(seed);
        final StringBuilder builder = new StringBuilder();
        this.swapCharacters(input, reverseMap(s), builder);
        return builder.toString();
    }
    
    private void swapCharacters(final String input, final Map<Character, Character> s, final StringBuilder builder) {
        final char c;
        CharBuffer.wrap(input.toCharArray()).chars().forEachOrdered(value -> {
            c = (char)value;
            if (s.containsKey(c)) {
                builder.append(s.get(c));
            }
            else {
                builder.append(c);
            }
        });
    }
    
    static {
        ORIGIN_CHARS = new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '-', '_', '/', ';', '=', '?', '+', 'µ', '£', '*', '^', '\u00f9', '$', '!', '{', '}', '\'', '\"', '|', '&' };
    }
    
    private enum EncryptionMode
    {
        SHUFFLE, 
        SHIFT;
    }
}
