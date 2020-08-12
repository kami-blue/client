// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.gui;

import me.zeroeightsix.kami.command.Command;
import net.minecraft.util.text.TextFormatting;
import java.util.function.Predicate;
import net.minecraft.network.play.server.SPacketChat;
import me.zeroeightsix.kami.setting.Settings;
import me.zero.alpine.listener.EventHandler;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.util.TimeUtil;
import me.zeroeightsix.kami.util.ColourUtils;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "ChatTimestamp", category = Category.GUI)
public class ChatTimestamp extends Module
{
    private Setting<ColourUtils.ColourCode> firstColour;
    private Setting<ColourUtils.ColourCode> secondColour;
    private Setting<TimeUtil.TimeType> timeTypeSetting;
    private Setting<TimeUtil.TimeUnit> timeUnitSetting;
    private Setting<Boolean> doLocale;
    @EventHandler
    public Listener<PacketEvent.Receive> listener;
    
    public ChatTimestamp() {
        this.firstColour = this.register(Settings.e("First Colour", ColourUtils.ColourCode.GREY));
        this.secondColour = this.register(Settings.e("Second Colour", ColourUtils.ColourCode.WHITE));
        this.timeTypeSetting = this.register(Settings.e("Time Format", TimeUtil.TimeType.HHMM));
        this.timeUnitSetting = this.register(Settings.e("Time Unit", TimeUtil.TimeUnit.h12));
        this.doLocale = this.register(Settings.b("Show AMPM", true));
        SPacketChat sPacketChat;
        this.listener = new Listener<PacketEvent.Receive>(event -> {
            if (ChatTimestamp.mc.field_71439_g != null && !this.isDisabled()) {
                if (!(!(event.getPacket() instanceof SPacketChat))) {
                    sPacketChat = (SPacketChat)event.getPacket();
                    if (this.addTime(sPacketChat.func_148915_c().func_150260_c())) {
                        event.cancel();
                    }
                }
            }
        }, (Predicate<PacketEvent.Receive>[])new Predicate[0]);
    }
    
    private boolean addTime(final String message) {
        Command.sendRawChatMessage("<" + TimeUtil.getFinalTime(this.secondColour.getValue(), this.firstColour.getValue(), this.timeUnitSetting.getValue(), this.timeTypeSetting.getValue(), this.doLocale.getValue()) + TextFormatting.RESET + "> " + message);
        return true;
    }
}
