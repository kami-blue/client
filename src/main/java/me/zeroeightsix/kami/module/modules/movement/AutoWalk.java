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

import java.util.Objects;

import static me.zeroeightsix.kami.util.MathsUtils.getPlayerCardinal;

/**
 * Created by 086 on 16/12/2017.
 * Greatly modified by Dewy on the 10th of May, 2020.
 */
@Module.Info(
        name = "AutoWalk",
        category = Module.Category.MOVEMENT,
        description = "Automatically walks somewhere"
)
public class AutoWalk extends Module {
    public Setting<AutoWalkMode> mode = register(Settings.e("Direction", AutoWalkMode.BARITONE));
    public static String direction;
    private boolean disableBaritone = false;

    @EventHandler
    private Listener<InputUpdateEvent> inputUpdateEventListener = new Listener<>(event -> {
        switch (mode.getValue()) {
            case FORWARD:
                disableBaritone = false;
                event.getMovementInput().moveForward = 1;
                break;
            case BACKWARDS:
                disableBaritone = false;
                event.getMovementInput().moveForward = -1;
                break;
            case BARITONE:
                disableBaritone = true;
                break;
        }
    });

    @EventHandler
    public Listener<ServerDisconnectedEvent> kickListener = new Listener<>(event -> {
        if (mode.getValue().equals(AutoWalkMode.BARITONE) && isEnabled()) {
            disable();
        }
    });

    public void onEnable() {
        if (!mode.getValue().equals(AutoWalkMode.BARITONE)) return;
        switch (Objects.requireNonNull(getPlayerCardinal(mc))) {
            case POS_Z:
                BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ((int) mc.player.posX, (int) mc.player.posZ + Integer.MAX_VALUE));
                break;
            case NEG_X_POS_Z:
                BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ((int) mc.player.posX - Integer.MAX_VALUE, (int) mc.player.posZ + Integer.MAX_VALUE));
                break;
            case NEG_X:
                BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ((int) mc.player.posX - Integer.MAX_VALUE, (int) mc.player.posZ));
                break;
            case NEG_X_NEG_Z:
                BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ((int) mc.player.posX - Integer.MAX_VALUE, (int) mc.player.posZ - Integer.MAX_VALUE));
                break;
            case NEG_Z:
                BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ((int) mc.player.posX, (int) mc.player.posZ - Integer.MAX_VALUE));
                break;
            case POS_X_NEG_Z:
                BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ((int) mc.player.posX + Integer.MAX_VALUE, (int) mc.player.posZ - Integer.MAX_VALUE));
                break;
            case POS_X:
                BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ((int) mc.player.posX + Integer.MAX_VALUE, (int) mc.player.posZ));
                break;
            case POS_X_POS_Z:
                BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ((int) mc.player.posX + Integer.MAX_VALUE, (int) mc.player.posZ + Integer.MAX_VALUE));
                break;
        }
        direction = Objects.requireNonNull(getPlayerCardinal(mc)).name;
    }

    public String getHudInfo() {
        if (BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().getGoal() != null) {
            return direction;
        } else {
            switch (mode.getValue()) {
                case BARITONE:
                    return "NONE";
                case FORWARD:
                    return "FORWARD";
                case BACKWARDS:
                    return "BACKWARDS";
                default:
                    return "";
            }
        }
    }

    public void onDisable() {
        if (disableBaritone) {
            BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
        }
    }

    public enum AutoWalkMode {
        FORWARD, BACKWARDS, BARITONE
    }
}
