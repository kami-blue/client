package me.zeroeightsix.kami.module.modules.movement;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalXZ;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.ServerDisconnectedEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraftforge.client.event.InputUpdateEvent;

import static me.zeroeightsix.kami.util.MathsUtils.normalizeAngle;

/**
 * Created by 086 on 16/12/2017.
 * Greatly modified by Dewy on the 10th of May, 2020.
 */
@Module.Info(
        name = "AutoWalk",
        category = Module.Category.MOVEMENT,
        description = "Pathfinding in a specific direction."
)
public class AutoWalk extends Module {
    public Setting<AutoWalkMode> mode = register(Settings.e("Mode", AutoWalkMode.FORWARD));
    public static String direction;

    @EventHandler
    private Listener<InputUpdateEvent> inputUpdateEventListener = new Listener<>(event -> {
        switch (mode.getValue()) {
            case FORWARD:
                event.getMovementInput().moveForward = 1;
                break;
            case BACKWARDS:
                event.getMovementInput().moveForward = -1;
                break;
        }
    });

    @EventHandler
    public Listener<ServerDisconnectedEvent> kickListener = new Listener<>(event -> {
        if(mode.getValue().equals(AutoWalkMode.BARITONE) && isEnabled()) {
            disable();
        }
    });

    public void onEnable() {
        if (normalizeAngle(mc.player.rotationYaw) >= -22.5 && normalizeAngle(mc.player.rotationYaw) <= 22.5) { // +Z
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ((int) mc.player.posX, (int) mc.player.posZ + Integer.MAX_VALUE));

            direction = "+Z";
        } else if (normalizeAngle(mc.player.rotationYaw) >= 22.6 && normalizeAngle(mc.player.rotationYaw) <= 67.5) { // -X / +Z
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ((int) mc.player.posX - Integer.MAX_VALUE, (int) mc.player.posZ + Integer.MAX_VALUE));

            direction = "-X / +Z";
        } else if (normalizeAngle(mc.player.rotationYaw) >= 67.6 && normalizeAngle(mc.player.rotationYaw) <= 112.5) { // -X
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ((int) mc.player.posX - Integer.MAX_VALUE, (int) mc.player.posZ));

            direction = "-X";
        } else if (normalizeAngle(mc.player.rotationYaw) >= 112.6 && normalizeAngle(mc.player.rotationYaw) <= 157.5) { // -X / -Z
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ((int) mc.player.posX - Integer.MAX_VALUE, (int) mc.player.posZ - Integer.MAX_VALUE));

            direction = "-X / -Z";
        } else if (normalizeAngle(mc.player.rotationYaw) >= 157.6 || normalizeAngle(mc.player.rotationYaw) <= -157.5) { // -Z
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ((int) mc.player.posX, (int) mc.player.posZ - Integer.MAX_VALUE));

            direction = "-Z";
        } else if (normalizeAngle(mc.player.rotationYaw) >= -157.6 && normalizeAngle(mc.player.rotationYaw) <= -112.5) { // +X / -Z
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ((int) mc.player.posX + Integer.MAX_VALUE, (int) mc.player.posZ - Integer.MAX_VALUE));

            direction = "+X / -Z";
        } else if (normalizeAngle(mc.player.rotationYaw) >= -112.6 && normalizeAngle(mc.player.rotationYaw) <= -67.5) { // +X
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ((int) mc.player.posX + Integer.MAX_VALUE, (int) mc.player.posZ));

            direction = "+X";
        } else if (normalizeAngle(mc.player.rotationYaw) >= -67.6 && normalizeAngle(mc.player.rotationYaw) <= -22.6) { // +X / +Z
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ((int) mc.player.posX + Integer.MAX_VALUE, (int) mc.player.posZ + Integer.MAX_VALUE));

            direction = "+X / +Z";
        }
    }

    public String getHudInfo() {
        return direction;
    }

    public void onDisable() {
        if (mode.getValue().equals(AutoWalkMode.BARITONE)) {
            BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
        }
    }

    public enum AutoWalkMode {
        FORWARD, BACKWARDS, BARITONE
    }
}
