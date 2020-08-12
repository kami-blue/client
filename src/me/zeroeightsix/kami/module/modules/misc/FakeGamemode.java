// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.misc;

import me.zeroeightsix.kami.command.Command;
import net.minecraft.client.Minecraft;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.world.GameType;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "FakeGamemode", description = "Fakes your current gamemode", category = Category.MISC)
public class FakeGamemode extends Module
{
    private Setting<GamemodeChanged> gamemode;
    private Setting<Boolean> disable2b;
    private GameType gameType;
    
    public FakeGamemode() {
        this.gamemode = this.register(Settings.e("Mode", GamemodeChanged.CREATIVE));
        this.disable2b = this.register(Settings.b("AntiKick 2b2t", true));
    }
    
    @Override
    public void onUpdate() {
        if (FakeGamemode.mc.field_71439_g == null) {
            return;
        }
        if (Minecraft.func_71410_x().func_147104_D() == null || (Minecraft.func_71410_x().func_147104_D() != null && Minecraft.func_71410_x().func_147104_D().field_78845_b.equalsIgnoreCase("2b2t.org"))) {
            if (FakeGamemode.mc.field_71439_g.field_71093_bK == 1 && this.disable2b.getValue()) {
                Command.sendWarningMessage("[FakeGamemode] Using this on 2b2t queue might get you kicked, please disable the AntiKick option if you're sure");
                this.disable();
            }
            return;
        }
        if (this.gamemode.getValue().equals(GamemodeChanged.CREATIVE)) {
            FakeGamemode.mc.field_71442_b.func_78746_a(this.gameType);
            FakeGamemode.mc.field_71442_b.func_78746_a(GameType.CREATIVE);
        }
        else if (this.gamemode.getValue().equals(GamemodeChanged.SURVIVAL)) {
            FakeGamemode.mc.field_71442_b.func_78746_a(this.gameType);
            FakeGamemode.mc.field_71442_b.func_78746_a(GameType.SURVIVAL);
        }
        else if (this.gamemode.getValue().equals(GamemodeChanged.ADVENTURE)) {
            FakeGamemode.mc.field_71442_b.func_78746_a(this.gameType);
            FakeGamemode.mc.field_71442_b.func_78746_a(GameType.ADVENTURE);
        }
        else if (this.gamemode.getValue().equals(GamemodeChanged.SPECTATOR)) {
            FakeGamemode.mc.field_71442_b.func_78746_a(this.gameType);
            FakeGamemode.mc.field_71442_b.func_78746_a(GameType.SPECTATOR);
        }
    }
    
    public void onEnable() {
        if (FakeGamemode.mc.field_71439_g == null) {
            this.disable();
            return;
        }
        this.gameType = FakeGamemode.mc.field_71442_b.func_178889_l();
    }
    
    public void onDisable() {
        if (FakeGamemode.mc.field_71439_g == null) {
            return;
        }
        FakeGamemode.mc.field_71442_b.func_78746_a(this.gameType);
    }
    
    private enum GamemodeChanged
    {
        SURVIVAL, 
        CREATIVE, 
        ADVENTURE, 
        SPECTATOR;
    }
}
