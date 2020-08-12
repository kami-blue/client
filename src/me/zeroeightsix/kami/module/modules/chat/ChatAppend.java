// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.chat;

import java.util.function.Predicate;
import java.util.Random;
import net.minecraft.network.play.client.CPacketChatMessage;
import me.zeroeightsix.kami.setting.Settings;
import me.zero.alpine.listener.EventHandler;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "Chat-Append", category = Category.CHAT, description = "Modifies your chat messages")
public class ChatAppend extends Module
{
    private Setting<Boolean> commands;
    private String[] suffixes;
    private Setting<AppendModes> appendMode;
    @EventHandler
    public Listener<PacketEvent.Send> listener;
    
    public ChatAppend() {
        this.commands = this.register(Settings.b("Commands", false));
        this.suffixes = new String[] { " \u2326 \u3010\uff21\uff53\uff54\uff52\uff41\u3011", " » \u1d00s\u1d1b\u0280\u1d00", " \u300b \u300eA\u300f\u300es\u300f\u300et\u300f\u300er\u300f\u300ea\u300f" };
        String s;
        int rnd;
        this.listener = new Listener<PacketEvent.Send>(event -> {
            if (event.getPacket() instanceof CPacketChatMessage) {
                s = ((CPacketChatMessage)event.getPacket()).func_149439_c();
                if (!s.startsWith("/") || this.commands.getValue()) {
                    switch (this.appendMode.getValue()) {
                        case MODE1: {
                            s += this.suffixes[0];
                            break;
                        }
                        case MODE2: {
                            s += this.suffixes[1];
                            break;
                        }
                        case MODE3: {
                            s += this.suffixes[2];
                            break;
                        }
                        case MODE4: {
                            rnd = new Random().nextInt(this.suffixes.length);
                            s += this.suffixes[rnd];
                            break;
                        }
                    }
                    if (s.length() >= 256) {
                        s = s.substring(0, 256);
                    }
                    ((CPacketChatMessage)event.getPacket()).field_149440_a = s;
                }
            }
            return;
        }, (Predicate<PacketEvent.Send>[])new Predicate[0]);
        this.appendMode = this.register(Settings.e("Append Mode", AppendModes.MODE1));
    }
    
    private enum AppendModes
    {
        MODE1, 
        MODE2, 
        MODE3, 
        MODE4;
    }
}
