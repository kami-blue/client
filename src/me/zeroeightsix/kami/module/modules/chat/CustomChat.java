// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.chat;

import java.util.function.Predicate;
import net.minecraft.network.play.client.CPacketChatMessage;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.setting.Settings;
import me.zero.alpine.listener.EventHandler;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "CustomChat", category = Category.CHAT, description = "Chat suffixes. memeszz poop client suffix included", showOnArray = ShowOnArray.OFF)
public class CustomChat extends Module
{
    public Setting<Boolean> startupGlobal;
    public Setting<TextMode> textMode;
    public Setting<DecoMode> decoMode;
    public Setting<Boolean> commands;
    public Setting<String> customText;
    @EventHandler
    public Listener<PacketEvent.Send> listener;
    
    public CustomChat() {
        this.startupGlobal = this.register(Settings.b("Enable Automatically", true));
        this.textMode = this.register(Settings.e("Message", TextMode.ONTOP));
        this.decoMode = this.register(Settings.e("Separator", DecoMode.NONE));
        this.commands = this.register(Settings.b("Commands", false));
        this.customText = this.register(Settings.stringBuilder("Custom Text").withValue("Use &7" + Command.getCommandPrefix() + "&rcustomchat to modify this").withConsumer((old, value) -> {}));
        String s;
        String s2;
        this.listener = new Listener<PacketEvent.Send>(event -> {
            if (event.getPacket() instanceof CPacketChatMessage) {
                s = ((CPacketChatMessage)event.getPacket()).func_149439_c();
                if (!this.commands.getValue()) {
                    if (s.startsWith("/")) {
                        return;
                    }
                    else if (s.startsWith(",")) {
                        return;
                    }
                    else if (s.startsWith(".")) {
                        return;
                    }
                    else if (s.startsWith("-")) {
                        return;
                    }
                    else if (s.startsWith(";")) {
                        return;
                    }
                    else if (s.startsWith("?")) {
                        return;
                    }
                    else if (s.startsWith("*")) {
                        return;
                    }
                    else if (s.startsWith("^")) {
                        return;
                    }
                    else if (s.startsWith("&")) {
                        return;
                    }
                }
                s2 = s + this.getFull(this.decoMode.getValue());
                if (s2.length() >= 256) {
                    s2 = s2.substring(0, 256);
                }
                ((CPacketChatMessage)event.getPacket()).field_149440_a = s2;
            }
        }, (Predicate<PacketEvent.Send>[])new Predicate[0]);
    }
    
    private String getText(final TextMode t) {
        switch (t) {
            case NAME: {
                return "\u029f\u026a\u0274\u1d1c\u0445\u1d04\u029f\u026a\u1d07\u0274\u1d1b";
            }
            case ONTOP: {
                return "\u029f\u026a\u0274\u1d1c\u0445\u1d04\u029f\u026a\u1d07\u0274\u1d1b \u1d0f\u0274 \u1d1b\u1d0f\u1d18";
            }
            case FITHACKS: {
                return "\ua730\u026a\u1d1b\u029c\u1d00\u1d04\u1d0b$";
            }
            case FITHACKSONTOP: {
                return "\ua730\u026a\u1d1b\u029c\u1d00\u1d04\u1d0b$ \u1d0f\u0274 \u1d1b\u1d0f\u1d18";
            }
            case AFRICA: {
                return "\u1d00\ua730\u0280\u026a\u1d04\u1d00 \u1d0f\u0274 \u1d1b\u1d0f\u1d18 \u1d0f\u1d21\u1d0f";
            }
            case NUTGOD: {
                return " \u0274\u1d1c\u1d1b\u0262\u1d0f\u1d05.\u1d04\u1d04 \u0fc9";
            }
            case CUSTOM: {
                return this.customText.getValue();
            }
            default: {
                return "";
            }
        }
    }
    
    private String getFull(final DecoMode d) {
        switch (d) {
            case NONE: {
                return " " + this.getText(this.textMode.getValue());
            }
            case CLASSIC: {
                return " « " + this.getText(this.textMode.getValue()) + " " + '»';
            }
            case SEPARATOR: {
                return " \u23d0 " + this.getText(this.textMode.getValue());
            }
            default: {
                return "";
            }
        }
    }
    
    public enum TextMode
    {
        NAME, 
        ONTOP, 
        FITHACKS, 
        FITHACKSONTOP, 
        AFRICA, 
        NUTGOD, 
        CUSTOM;
    }
    
    private enum DecoMode
    {
        SEPARATOR, 
        CLASSIC, 
        NONE;
    }
}
