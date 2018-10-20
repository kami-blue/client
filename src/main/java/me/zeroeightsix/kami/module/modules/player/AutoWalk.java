package me.zeroeightsix.kami.module.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import net.minecraft.pathfinding.PathPoint;
import net.minecraftforge.client.event.InputUpdateEvent;

import static me.zeroeightsix.kami.util.EntityUtil.calculateLookAt;

/**
 * Created by 086 on 16/12/2017.
 */
@Module.Info(name = "AutoWalk", category = Module.Category.PLAYER)
public class AutoWalk extends Module {

    @Setting(name = "Mode") private AutoWalkMode mode = AutoWalkMode.FORWARD;

    @EventHandler
    private Listener<InputUpdateEvent> inputUpdateEventListener = new Listener<>(event -> {
        switch (mode) {
            case FORWARD:
                event.getMovementInput().moveForward=1;
                break;
            case BACKWARDS:
                event.getMovementInput().moveForward=-1;
                break;
        }
    });

    private void lookAt(PathPoint pathPoint)
    {
        double[] v = calculateLookAt(pathPoint.x+.5f, pathPoint.y, pathPoint.z+.5f, mc.player);
        mc.player.rotationYaw = (float) v[0];
        mc.player.rotationPitch = (float) v[1];
    }

    private static enum AutoWalkMode {
        FORWARD, BACKWARDS
    }
}
