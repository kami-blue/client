// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.chat;

import net.minecraft.util.math.Vec3i;
import me.zeroeightsix.kami.command.Command;
import net.minecraft.entity.player.EntityPlayer;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "AutoQueueMain", category = Category.CHAT, description = "Sends AutoQueueMain")
public class AutoQueueMain extends Module
{
    private final Timer timer;
    private Setting<Integer> delay;
    private Setting<Boolean> info;
    
    public AutoQueueMain() {
        this.timer = new Timer();
        this.delay = this.register((Setting<Integer>)Settings.integerBuilder("Seconds Delay").withMinimum(0).withValue(9).withMaximum(90).build());
        this.info = this.register(Settings.b("Info Messages", false));
    }
    
    @Override
    public void onUpdate() {
        if (!this.shouldSendMessage((EntityPlayer)AutoQueueMain.mc.field_71439_g)) {
            return;
        }
        if (this.info.getValue()) {
            Command.sendChatMessage("[AutoQueueMain] Sending message: /queue main");
        }
        AutoQueueMain.mc.field_71439_g.func_71165_d("/queue main");
        this.timer.reset();
    }
    
    private boolean shouldSendMessage(final EntityPlayer player) {
        return player.field_71093_bK == 1 && this.timer.passed(this.delay.getValue() * 1000) && player.func_180425_c().equals((Object)new Vec3i(0, 240, 0));
    }
    
    public static final class Timer
    {
        private long time;
        
        Timer() {
            this.time = -1L;
        }
        
        boolean passed(final double ms) {
            return System.currentTimeMillis() - this.time >= ms;
        }
        
        public void reset() {
            this.time = System.currentTimeMillis();
        }
    }
}
