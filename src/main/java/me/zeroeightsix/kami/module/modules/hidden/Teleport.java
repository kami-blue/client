package me.zeroeightsix.kami.module.modules.hidden;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.module.Module;
import net.minecraft.util.math.Vec3d;

/**
 * Created by d1gress/Qther on 26/11/2019.
 */

@Module.Info(name = "Teleport", description = "you aren't supposed to see this.", category = Module.Category.HIDDEN)
public class Teleport extends Module {

    private long lastTp;
    private Vec3d lastPos;
    public static Vec3d finalPos;
    public static double blocksPerTeleport;

    @Override
    public void onUpdate() {
        if(finalPos == null) {
            Command.sendErrorMessage("Position not set, use .tp");
            this.disable();
            return;
        }

        Vec3d tpDirectionVec = finalPos.subtract(mc.player.posX, mc.player.posY, mc.player.posZ).normalize();

        if (mc.world.isBlockLoaded(mc.player.getPosition())) {
            lastPos = new Vec3d(mc.player.posX, mc.player.posY, mc.player.posZ);
            if (finalPos.distanceTo(new Vec3d(mc.player.posX, mc.player.posY, mc.player.posZ)) < 0.3 || blocksPerTeleport == 0) {
                Command.sendChatMessage("Teleport Finished!");
                this.disable();
            } else {
                mc.player.setVelocity(0, 0, 0);
            }

            if (finalPos.distanceTo(new Vec3d(mc.player.posX, mc.player.posY, mc.player.posZ)) >= blocksPerTeleport) {
                final Vec3d vec = tpDirectionVec.scale(blocksPerTeleport);
                mc.player.setPosition(mc.player.posX + vec.x, mc.player.posY + vec.y, mc.player.posZ + vec.z);
            } else {
                final Vec3d vec = tpDirectionVec.scale(finalPos.distanceTo(new Vec3d(mc.player.posX, mc.player.posY, mc.player.posZ)));
                mc.player.setPosition(mc.player.posX + vec.x, mc.player.posY + vec.y, mc.player.posZ + vec.z);
                this.disable();
            }
            lastTp = System.currentTimeMillis();
        } else if (lastTp + 2000L > System.currentTimeMillis()) {
            mc.player.setPosition(lastPos.x, lastPos.y, lastPos.z);
        }
    }

}
