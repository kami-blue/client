// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.movement;

import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import me.zeroeightsix.kami.module.ModuleManager;
import net.minecraft.network.play.client.CPacketPlayer;
import me.zeroeightsix.kami.event.KamiEvent;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import me.zeroeightsix.kami.util.EntityUtil;
import net.minecraft.block.BlockLiquid;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zero.alpine.listener.EventHandler;
import me.zeroeightsix.kami.event.events.AddCollisionBoxToListEvent;
import me.zero.alpine.listener.Listener;
import net.minecraft.util.math.AxisAlignedBB;
import me.zeroeightsix.kami.module.Module;

@Info(name = "Jesus", description = "Allows you to walk on water", category = Category.MOVEMENT)
public class Jesus extends Module
{
    private static final AxisAlignedBB WATER_WALK_AA;
    @EventHandler
    Listener<AddCollisionBoxToListEvent> addCollisionBoxToListEventListener;
    @EventHandler
    Listener<PacketEvent.Send> packetEventSendListener;
    
    public Jesus() {
        AxisAlignedBB axisalignedbb;
        this.addCollisionBoxToListEventListener = new Listener<AddCollisionBoxToListEvent>(event -> {
            if (Jesus.mc.field_71439_g != null && event.getBlock() instanceof BlockLiquid && (EntityUtil.isDrivenByPlayer(event.getEntity()) || event.getEntity() == Jesus.mc.field_71439_g) && !(event.getEntity() instanceof EntityBoat) && !Jesus.mc.field_71439_g.func_70093_af() && Jesus.mc.field_71439_g.field_70143_R < 3.0f && !EntityUtil.isInWater((Entity)Jesus.mc.field_71439_g) && (EntityUtil.isAboveWater((Entity)Jesus.mc.field_71439_g, false) || EntityUtil.isAboveWater(Jesus.mc.field_71439_g.func_184187_bx(), false)) && isAboveBlock((Entity)Jesus.mc.field_71439_g, event.getPos())) {
                axisalignedbb = Jesus.WATER_WALK_AA.func_186670_a(event.getPos());
                if (event.getEntityBox().func_72326_a(axisalignedbb)) {
                    event.getCollidingBoxes().add(axisalignedbb);
                }
                event.cancel();
            }
            return;
        }, (Predicate<AddCollisionBoxToListEvent>[])new Predicate[0]);
        int ticks;
        CPacketPlayer cPacketPlayer;
        this.packetEventSendListener = new Listener<PacketEvent.Send>(event -> {
            if (event.getEra() == KamiEvent.Era.PRE && event.getPacket() instanceof CPacketPlayer && EntityUtil.isAboveWater((Entity)Jesus.mc.field_71439_g, true) && !EntityUtil.isInWater((Entity)Jesus.mc.field_71439_g) && !isAboveLand((Entity)Jesus.mc.field_71439_g)) {
                ticks = Jesus.mc.field_71439_g.field_70173_aa % 2;
                if (ticks == 0) {
                    cPacketPlayer = (CPacketPlayer)event.getPacket();
                    cPacketPlayer.field_149477_b += 0.02;
                }
            }
        }, (Predicate<PacketEvent.Send>[])new Predicate[0]);
    }
    
    @Override
    public void onUpdate() {
        if (!ModuleManager.isModuleEnabled("Freecam") && EntityUtil.isInWater((Entity)Jesus.mc.field_71439_g) && !Jesus.mc.field_71439_g.func_70093_af()) {
            Jesus.mc.field_71439_g.field_70181_x = 0.1;
            if (Jesus.mc.field_71439_g.func_184187_bx() != null && !(Jesus.mc.field_71439_g.func_184187_bx() instanceof EntityBoat)) {
                Jesus.mc.field_71439_g.func_184187_bx().field_70181_x = 0.3;
            }
        }
    }
    
    private static boolean isAboveLand(final Entity entity) {
        if (entity == null) {
            return false;
        }
        final double y = entity.field_70163_u - 0.01;
        for (int x = MathHelper.func_76128_c(entity.field_70165_t); x < MathHelper.func_76143_f(entity.field_70165_t); ++x) {
            for (int z = MathHelper.func_76128_c(entity.field_70161_v); z < MathHelper.func_76143_f(entity.field_70161_v); ++z) {
                final BlockPos pos = new BlockPos(x, MathHelper.func_76128_c(y), z);
                if (Wrapper.getWorld().func_180495_p(pos).func_177230_c().func_149730_j(Wrapper.getWorld().func_180495_p(pos))) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private static boolean isAboveBlock(final Entity entity, final BlockPos pos) {
        return entity.field_70163_u >= pos.func_177956_o();
    }
    
    static {
        WATER_WALK_AA = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.99, 1.0);
    }
}
