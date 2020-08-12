// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.chat;

import java.util.function.Predicate;
import net.minecraft.util.text.ITextComponent;
import java.util.Date;
import java.text.SimpleDateFormat;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.util.text.TextComponentString;
import me.zeroeightsix.kami.setting.Settings;
import me.zero.alpine.listener.EventHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "ChatTimeStamps", category = Category.CHAT)
public class ChatTimeStamps extends Module
{
    private Setting<Boolean> deco;
    @EventHandler
    public Listener<ClientChatReceivedEvent> listener;
    
    public ChatTimeStamps() {
        this.deco = this.register(Settings.b("Deco", true));
        final TextComponentString textComponentString;
        final TextComponentString newTextComponentString;
        this.listener = new Listener<ClientChatReceivedEvent>(event -> {
            new TextComponentString(ChatFormatting.GRAY + (this.deco.getValue() ? "<" : "") + new SimpleDateFormat("k:mm").format(new Date()) + (this.deco.getValue() ? ">" : "") + ChatFormatting.RESET + " ");
            newTextComponentString = textComponentString;
            newTextComponentString.func_150257_a(event.getMessage());
            event.setMessage((ITextComponent)newTextComponentString);
        }, (Predicate<ClientChatReceivedEvent>[])new Predicate[0]);
    }
}
