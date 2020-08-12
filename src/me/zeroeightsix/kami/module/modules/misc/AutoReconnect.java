// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.misc;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiDisconnected;
import java.util.function.Predicate;
import net.minecraft.client.multiplayer.GuiConnecting;
import me.zeroeightsix.kami.setting.Settings;
import me.zero.alpine.listener.EventHandler;
import me.zeroeightsix.kami.event.events.GuiScreenEvent;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.multiplayer.ServerData;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "AutoReconnect", description = "Automatically reconnects after being disconnected", category = Category.MISC, alwaysListening = true)
public class AutoReconnect extends Module
{
    private Setting<Integer> seconds;
    private static ServerData cServer;
    @EventHandler
    public Listener<GuiScreenEvent.Closed> closedListener;
    @EventHandler
    public Listener<GuiScreenEvent.Displayed> displayedListener;
    
    public AutoReconnect() {
        this.seconds = this.register((Setting<Integer>)Settings.integerBuilder("Seconds").withValue(5).withMinimum(0).build());
        this.closedListener = new Listener<GuiScreenEvent.Closed>(event -> {
            if (event.getScreen() instanceof GuiConnecting) {
                AutoReconnect.cServer = AutoReconnect.mc.field_71422_O;
            }
            return;
        }, (Predicate<GuiScreenEvent.Closed>[])new Predicate[0]);
        this.displayedListener = new Listener<GuiScreenEvent.Displayed>(event -> {
            if (this.isEnabled() && event.getScreen() instanceof GuiDisconnected && (AutoReconnect.cServer != null || AutoReconnect.mc.field_71422_O != null)) {
                event.setScreen((GuiScreen)new KamiGuiDisconnected((GuiDisconnected)event.getScreen()));
            }
        }, (Predicate<GuiScreenEvent.Displayed>[])new Predicate[0]);
    }
    
    private class KamiGuiDisconnected extends GuiDisconnected
    {
        int millis;
        long cTime;
        
        public KamiGuiDisconnected(final GuiDisconnected disconnected) {
            super(disconnected.field_146307_h, disconnected.field_146306_a, disconnected.field_146304_f);
            this.millis = AutoReconnect.this.seconds.getValue() * 1000;
            this.cTime = System.currentTimeMillis();
        }
        
        public void func_73876_c() {
            if (this.millis <= 0) {
                this.field_146297_k.func_147108_a((GuiScreen)new GuiConnecting(this.field_146307_h, this.field_146297_k, (AutoReconnect.cServer == null) ? this.field_146297_k.field_71422_O : AutoReconnect.cServer));
            }
        }
        
        public void func_73863_a(final int mouseX, final int mouseY, final float partialTicks) {
            super.func_73863_a(mouseX, mouseY, partialTicks);
            final long a = System.currentTimeMillis();
            this.millis -= (int)(a - this.cTime);
            this.cTime = a;
            final String s = "Reconnecting in " + Math.max(0.0, Math.floor(this.millis / 100.0) / 10.0) + "s";
            this.field_146289_q.func_175065_a(s, (float)(this.field_146294_l / 2 - this.field_146289_q.func_78256_a(s) / 2), (float)(this.field_146295_m - 16), 16777215, true);
        }
    }
}
