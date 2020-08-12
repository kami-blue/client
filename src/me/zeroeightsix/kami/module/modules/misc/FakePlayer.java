// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.misc;

import java.util.Iterator;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import me.zeroeightsix.kami.module.Module;

@Info(name = "FakePlayer", category = Category.MISC, description = "Spawns a fake Player")
public class FakePlayer extends Module
{
    private List<Integer> fakePlayerIdList;
    
    public FakePlayer() {
        this.fakePlayerIdList = null;
    }
    
    @Override
    protected void onEnable() {
        if (FakePlayer.mc.field_71439_g == null || FakePlayer.mc.field_71441_e == null) {
            this.disable();
            return;
        }
        this.fakePlayerIdList = new ArrayList<Integer>();
        final EntityOtherPlayerMP fakePlayer1 = new EntityOtherPlayerMP((World)FakePlayer.mc.field_71441_e, new GameProfile(UUID.fromString("d991ed71-1d81-4fb3-8af1-f5c49ac98983"), "CrawlerExE"));
        fakePlayer1.func_82149_j((Entity)FakePlayer.mc.field_71439_g);
        fakePlayer1.field_70759_as = FakePlayer.mc.field_71439_g.field_70759_as;
        FakePlayer.mc.field_71441_e.func_73027_a(-101, (Entity)fakePlayer1);
        this.fakePlayerIdList.add(-101);
        final EntityOtherPlayerMP fakePlayer2 = new EntityOtherPlayerMP((World)FakePlayer.mc.field_71441_e, new GameProfile(UUID.fromString("d991ed71-1d81-4fb3-8af1-f5c49ac98983"), "CrawlerExE"));
        fakePlayer2.func_82149_j((Entity)FakePlayer.mc.field_71439_g);
        fakePlayer2.field_70759_as = FakePlayer.mc.field_71439_g.field_70759_as;
        fakePlayer2.field_70165_t += 2.0;
        FakePlayer.mc.field_71441_e.func_73027_a(-102, (Entity)fakePlayer2);
        this.fakePlayerIdList.add(-102);
        final EntityOtherPlayerMP fakePlayer3 = new EntityOtherPlayerMP((World)FakePlayer.mc.field_71441_e, new GameProfile(UUID.fromString("d991ed71-1d81-4fb3-8af1-f5c49ac98983"), "CrawlerExE"));
        fakePlayer3.func_82149_j((Entity)FakePlayer.mc.field_71439_g);
        fakePlayer3.field_70759_as = FakePlayer.mc.field_71439_g.field_70759_as;
        fakePlayer3.field_70165_t += 4.0;
        FakePlayer.mc.field_71441_e.func_73027_a(-103, (Entity)fakePlayer3);
        this.fakePlayerIdList.add(-103);
        final EntityOtherPlayerMP fakePlayer4 = new EntityOtherPlayerMP((World)FakePlayer.mc.field_71441_e, new GameProfile(UUID.fromString("d991ed71-1d81-4fb3-8af1-f5c49ac98983"), "CrawlerExE"));
        fakePlayer4.func_82149_j((Entity)FakePlayer.mc.field_71439_g);
        fakePlayer4.field_70759_as = FakePlayer.mc.field_71439_g.field_70759_as;
        fakePlayer4.field_70161_v += 2.0;
        FakePlayer.mc.field_71441_e.func_73027_a(-104, (Entity)fakePlayer4);
        this.fakePlayerIdList.add(-104);
        final EntityOtherPlayerMP fakePlayer5 = new EntityOtherPlayerMP((World)FakePlayer.mc.field_71441_e, new GameProfile(UUID.fromString("d991ed71-1d81-4fb3-8af1-f5c49ac98983"), "CrawlerExE"));
        fakePlayer5.func_82149_j((Entity)FakePlayer.mc.field_71439_g);
        fakePlayer5.field_70759_as = FakePlayer.mc.field_71439_g.field_70759_as;
        fakePlayer5.field_70161_v += 4.0;
        FakePlayer.mc.field_71441_e.func_73027_a(-105, (Entity)fakePlayer5);
        this.fakePlayerIdList.add(-105);
        final EntityOtherPlayerMP fakePlayer6 = new EntityOtherPlayerMP((World)FakePlayer.mc.field_71441_e, new GameProfile(UUID.fromString("d991ed71-1d81-4fb3-8af1-f5c49ac98983"), "CrawlerExE"));
        fakePlayer6.func_82149_j((Entity)FakePlayer.mc.field_71439_g);
        fakePlayer6.field_70759_as = FakePlayer.mc.field_71439_g.field_70759_as;
        fakePlayer6.field_70165_t -= 2.0;
        FakePlayer.mc.field_71441_e.func_73027_a(-106, (Entity)fakePlayer6);
        this.fakePlayerIdList.add(-106);
        final EntityOtherPlayerMP fakePlayer7 = new EntityOtherPlayerMP((World)FakePlayer.mc.field_71441_e, new GameProfile(UUID.fromString("d991ed71-1d81-4fb3-8af1-f5c49ac98983"), "CrawlerExE"));
        fakePlayer7.func_82149_j((Entity)FakePlayer.mc.field_71439_g);
        fakePlayer7.field_70759_as = FakePlayer.mc.field_71439_g.field_70759_as;
        fakePlayer7.field_70165_t -= 4.0;
        FakePlayer.mc.field_71441_e.func_73027_a(-107, (Entity)fakePlayer7);
        this.fakePlayerIdList.add(-107);
        final EntityOtherPlayerMP fakePlayer8 = new EntityOtherPlayerMP((World)FakePlayer.mc.field_71441_e, new GameProfile(UUID.fromString("d991ed71-1d81-4fb3-8af1-f5c49ac98983"), "CrawlerExE"));
        fakePlayer8.func_82149_j((Entity)FakePlayer.mc.field_71439_g);
        fakePlayer8.field_70759_as = FakePlayer.mc.field_71439_g.field_70759_as;
        fakePlayer8.field_70161_v -= 2.0;
        FakePlayer.mc.field_71441_e.func_73027_a(-108, (Entity)fakePlayer8);
        this.fakePlayerIdList.add(-108);
        final EntityOtherPlayerMP fakePlayer9 = new EntityOtherPlayerMP((World)FakePlayer.mc.field_71441_e, new GameProfile(UUID.fromString("d991ed71-1d81-4fb3-8af1-f5c49ac98983"), "CrawlerExE"));
        fakePlayer9.func_82149_j((Entity)FakePlayer.mc.field_71439_g);
        fakePlayer9.field_70759_as = FakePlayer.mc.field_71439_g.field_70759_as;
        fakePlayer9.field_70161_v -= 4.0;
        FakePlayer.mc.field_71441_e.func_73027_a(-109, (Entity)fakePlayer9);
        this.fakePlayerIdList.add(-109);
    }
    
    @Override
    public void onUpdate() {
        if (this.fakePlayerIdList == null || this.fakePlayerIdList.isEmpty()) {
            this.disable();
        }
    }
    
    @Override
    protected void onDisable() {
        if (FakePlayer.mc.field_71439_g == null || FakePlayer.mc.field_71441_e == null) {
            return;
        }
        if (this.fakePlayerIdList != null) {
            for (final int id : this.fakePlayerIdList) {
                FakePlayer.mc.field_71441_e.func_73028_b(id);
            }
        }
    }
}
