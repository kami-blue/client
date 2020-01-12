package me.zeroeightsix.kami.module.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import static net.minecraft.network.play.client.CPacketPlayerDigging.Action.START_DESTROY_BLOCK;
import static net.minecraft.network.play.client.CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK;

/**
 * Created 14 November 2019 by hub
 * Updated 29 November 2019 by hub
 */
@Module.Info(name = "NoBreakAnimation", category = Module.Category.PLAYER, description = "Prevents block break animation server side")
public class NoBreakAnimation extends Module {

    private boolean isMining = false;
    private BlockPos lastPos = null;
    private EnumFacing lastFacing = null;

    @EventHandler
    public Listener<PacketEvent.Send> listener = new Listener<>(event -> {
        if (event.getPacket() instanceof CPacketPlayerDigging) {
            CPacketPlayerDigging cPacketPlayerDigging = (CPacketPlayerDigging) event.getPacket();
            // skip crystals and living entities
            for (Entity entity : mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(cPacketPlayerDigging.getPosition()))) {
                if (entity instanceof EntityEnderCrystal) {
                    resetMining();
                    return;
                }
                if (entity instanceof EntityLivingBase) {
                    resetMining();
                    return;
                }
            }
            if (cPacketPlayerDigging.getAction().equals(START_DESTROY_BLOCK)) {
                isMining = true;
                setMiningInfo(cPacketPlayerDigging.getPosition(), cPacketPlayerDigging.getFacing());
            }
            if (cPacketPlayerDigging.getAction().equals(STOP_DESTROY_BLOCK)) {
                resetMining();
            }
        }
    });

    @Override
    public void onUpdate() {
        if (!mc.gameSettings.keyBindAttack.isKeyDown()) {
            resetMining();
            return;
        }
        if (isMining && lastPos != null && lastFacing != null) {
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, lastPos, lastFacing));
        }
    }

    private void setMiningInfo(BlockPos lastPos, EnumFacing lastFacing) {
        this.lastPos = lastPos;
        this.lastFacing = lastFacing;
    }

    public void resetMining() { // TODO: call in autofeetplace and autotrap when they are merged
        isMining = false;
        lastPos = null;
        lastFacing = null;
    }

}
