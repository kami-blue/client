// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.chat;

import me.zeroeightsix.kami.util.FileHelper;
import java.util.Iterator;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketChatMessage;
import me.zeroeightsix.kami.util.ChatTextUtils;
import me.zeroeightsix.kami.module.ModuleManager;
import java.util.ArrayList;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.Setting;
import java.util.TimerTask;
import java.util.Timer;
import java.util.Random;
import java.util.List;
import me.zeroeightsix.kami.module.Module;

@Info(name = "Spammer", category = Category.CHAT, description = "SPAM")
public class Spammer extends Module
{
    private static final String fileName = "Hephaestus_Spammer.txt";
    private static final String defaultMessage = "Join 0b0t.org - Worlds oldest Minecraft Server!";
    private static List<String> spamMessages;
    private static Random rnd;
    private static Timer timer;
    private static TimerTask task;
    private Setting<Boolean> random;
    private Setting<Boolean> greentext;
    private Setting<Boolean> randomsuffix;
    private Setting<Integer> delay;
    private Setting<Boolean> readfile;
    
    public Spammer() {
        this.random = this.register(Settings.b("Random", false));
        this.greentext = this.register(Settings.b("Greentext", false));
        this.randomsuffix = this.register(Settings.b("Anti Spam", true));
        this.delay = this.register((Setting<Integer>)Settings.integerBuilder("Send Delay").withRange(100, 60000).withValue(4000).build());
        this.readfile = this.register(Settings.b("Load File", false));
    }
    
    public void onEnable() {
        this.readSpamFile();
        Spammer.timer = new Timer();
        if (Spammer.mc.field_71439_g == null) {
            this.disable();
            return;
        }
        Spammer.task = new TimerTask() {
            @Override
            public void run() {
                Spammer.this.runCycle();
            }
        };
        Spammer.timer.schedule(Spammer.task, 0L, this.delay.getValue());
    }
    
    public void onDisable() {
        Spammer.timer.cancel();
        Spammer.timer.purge();
        Spammer.spamMessages.clear();
    }
    
    private void runCycle() {
        if (Spammer.mc.field_71439_g == null) {
            return;
        }
        if (this.readfile.getValue()) {
            this.readSpamFile();
            this.readfile.setValue(false);
        }
        if (Spammer.spamMessages.size() > 0) {
            String messageOut;
            if (this.random.getValue()) {
                final int index = Spammer.rnd.nextInt(Spammer.spamMessages.size());
                messageOut = Spammer.spamMessages.get(index);
                Spammer.spamMessages.remove(index);
            }
            else {
                messageOut = Spammer.spamMessages.get(0);
                Spammer.spamMessages.remove(0);
            }
            Spammer.spamMessages.add(messageOut);
            if (this.greentext.getValue()) {
                messageOut = "> " + messageOut;
            }
            int reserved = 0;
            final ArrayList<String> messageAppendix = new ArrayList<String>();
            if (ModuleManager.isModuleEnabled("ChatSuffix")) {
                reserved += " \u23d0 \u5342s\u1d1b\u0280\u1d00\u2122 ".length();
            }
            if (this.randomsuffix.getValue()) {
                messageAppendix.add(ChatTextUtils.generateRandomHexSuffix(2));
            }
            if (messageAppendix.size() > 0) {
                final StringBuilder sb = new StringBuilder();
                sb.append(" ");
                for (final String msg : messageAppendix) {
                    sb.append(msg);
                }
                messageOut = ChatTextUtils.cropMaxLengthMessage(messageOut, sb.toString().length() + reserved);
                messageOut += sb.toString();
            }
            Spammer.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new CPacketChatMessage(messageOut.replaceAll("§", "")));
        }
    }
    
    private void readSpamFile() {
        final List<String> fileInput = FileHelper.readTextFileAllLines("Astra_Spammer.txt");
        final Iterator<String> i = fileInput.iterator();
        Spammer.spamMessages.clear();
        while (i.hasNext()) {
            final String s = i.next();
            if (!s.replaceAll("\\s", "").isEmpty()) {
                Spammer.spamMessages.add(s);
            }
        }
        if (Spammer.spamMessages.size() == 0) {
            Spammer.spamMessages.add("Another goon down, thanks to tux and astramod!");
        }
    }
    
    static {
        Spammer.spamMessages = new ArrayList<String>();
        Spammer.rnd = new Random();
    }
}
