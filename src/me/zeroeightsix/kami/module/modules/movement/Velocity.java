// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.movement;

import java.util.function.Predicate;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import me.zeroeightsix.kami.event.KamiEvent;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.event.events.EntityEvent;
import me.zero.alpine.listener.EventHandler;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.module.Module;

@Info(name = "Velocity", description = "Modify knockback impact", category = Category.MOVEMENT)
public class Velocity extends Module
{
    private Setting<Float> horizontal;
    private Setting<Float> vertical;
    @EventHandler
    private Listener<PacketEvent.Receive> packetEventListener;
    @EventHandler
    private Listener<EntityEvent.EntityCollision> entityCollisionListener;
    
    public Velocity() {
        this.horizontal = this.register(Settings.f("Horizontal", 0.0f));
        this.vertical = this.register(Settings.f("Vertical", 0.0f));
        SPacketEntityVelocity velocity;
        final SPacketEntityVelocity sPacketEntityVelocity;
        final SPacketEntityVelocity sPacketEntityVelocity2;
        final SPacketEntityVelocity sPacketEntityVelocity3;
        SPacketExplosion sPacketExplosion;
        SPacketExplosion velocity2;
        final SPacketExplosion sPacketExplosion2;
        final SPacketExplosion sPacketExplosion3;
        this.packetEventListener = new Listener<PacketEvent.Receive>(event -> {
            if (event.getEra() == KamiEvent.Era.PRE) {
                if (event.getPacket() instanceof SPacketEntityVelocity) {
                    velocity = (SPacketEntityVelocity)event.getPacket();
                    if (velocity.func_149412_c() == Velocity.mc.field_71439_g.field_145783_c) {
                        if (this.horizontal.getValue() == 0.0f && this.vertical.getValue() == 0.0f) {
                            event.cancel();
                        }
                        sPacketEntityVelocity.field_149415_b *= (int)(Object)this.horizontal.getValue();
                        sPacketEntityVelocity2.field_149416_c *= (int)(Object)this.vertical.getValue();
                        sPacketEntityVelocity3.field_149414_d *= (int)(Object)this.horizontal.getValue();
                    }
                }
                else if (event.getPacket() instanceof SPacketExplosion) {
                    if (this.horizontal.getValue() == 0.0f && this.vertical.getValue() == 0.0f) {
                        event.cancel();
                    }
                    velocity2 = (sPacketExplosion = (SPacketExplosion)event.getPacket());
                    sPacketExplosion.field_149152_f *= this.horizontal.getValue();
                    sPacketExplosion2.field_149153_g *= this.vertical.getValue();
                    sPacketExplosion3.field_149159_h *= this.horizontal.getValue();
                }
            }
            return;
        }, (Predicate<PacketEvent.Receive>[])new Predicate[0]);
        this.entityCollisionListener = new Listener<EntityEvent.EntityCollision>(event -> {
            if (event.getEntity() == Velocity.mc.field_71439_g) {
                if (this.horizontal.getValue() == 0.0f && this.vertical.getValue() == 0.0f) {
                    event.cancel();
                }
                else {
                    event.setX(-event.getX() * this.horizontal.getValue());
                    event.setY(0.0);
                    event.setZ(-event.getZ() * this.horizontal.getValue());
                }
            }
        }, (Predicate<EntityEvent.EntityCollision>[])new Predicate[0]);
    }
}
