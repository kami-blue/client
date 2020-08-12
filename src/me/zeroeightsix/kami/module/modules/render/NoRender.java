// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.render;

import net.minecraft.network.Packet;
import java.util.function.Predicate;
import net.minecraft.network.play.server.SPacketSpawnPainting;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketSpawnExperienceOrb;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.network.play.server.SPacketSpawnGlobalEntity;
import net.minecraft.network.play.server.SPacketSpawnMob;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import me.zero.alpine.listener.EventHandler;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "NoRender", category = Category.RENDER, description = "Ignore entity spawn packets")
public class NoRender extends Module
{
    private Setting<Boolean> mob;
    private Setting<Boolean> sand;
    private Setting<Boolean> gentity;
    private Setting<Boolean> object;
    private Setting<Boolean> xp;
    private Setting<Boolean> paint;
    private Setting<Boolean> fire;
    private Setting<Boolean> explosion;
    @EventHandler
    public Listener<PacketEvent.Receive> receiveListener;
    @EventHandler
    public Listener<RenderBlockOverlayEvent> blockOverlayEventListener;
    
    public NoRender() {
        this.mob = this.register(Settings.b("Mob", false));
        this.sand = this.register(Settings.b("Sand", false));
        this.gentity = this.register(Settings.b("GEntity", false));
        this.object = this.register(Settings.b("Object", false));
        this.xp = this.register(Settings.b("XP", false));
        this.paint = this.register(Settings.b("Paintings", false));
        this.fire = this.register(Settings.b("Fire"));
        this.explosion = this.register(Settings.b("Explosions"));
        final Packet packet;
        this.receiveListener = new Listener<PacketEvent.Receive>(event -> {
            packet = event.getPacket();
            if ((packet instanceof SPacketSpawnMob && this.mob.getValue()) || (packet instanceof SPacketSpawnGlobalEntity && this.gentity.getValue()) || (packet instanceof SPacketSpawnObject && this.object.getValue()) || (packet instanceof SPacketSpawnExperienceOrb && this.xp.getValue()) || (packet instanceof SPacketSpawnObject && this.sand.getValue()) || (packet instanceof SPacketExplosion && this.explosion.getValue()) || (packet instanceof SPacketSpawnPainting && this.paint.getValue())) {
                event.cancel();
            }
            return;
        }, (Predicate<PacketEvent.Receive>[])new Predicate[0]);
        this.blockOverlayEventListener = new Listener<RenderBlockOverlayEvent>(event -> {
            if (this.fire.getValue() && event.getOverlayType() == RenderBlockOverlayEvent.OverlayType.FIRE) {
                event.setCanceled(true);
            }
        }, (Predicate<RenderBlockOverlayEvent>[])new Predicate[0]);
    }
}
