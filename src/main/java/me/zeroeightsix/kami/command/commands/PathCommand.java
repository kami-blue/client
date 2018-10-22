package me.zeroeightsix.kami.command.commands;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalXZ;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.SyntaxChunk;
import me.zeroeightsix.kami.util.EntityUtil;
import me.zeroeightsix.kami.util.Wrapper;

import java.awt.*;

/**
 * Created by 086 on 25/01/2018.
 */
public class PathCommand extends Command {
    public PathCommand() {
        super("path", SyntaxChunk.EMPTY);
    }

    @Override
    public void call(String[] args) {
        BaritoneAPI.getSettings().freeLook.value = false;
        BaritoneAPI.getSettings().chatDebug.value = false;
        BaritoneAPI.getSettings().allowSprint.value = true;
        BaritoneAPI.getSettings().antiCheatCompatibility.value = true;
        BaritoneAPI.getSettings().walkWhileBreaking.value = false;
        BaritoneAPI.getSettings().colorCurrentPath.value = Color.RED;
        BaritoneAPI.getSettings().colorNextPath.value = Color.CYAN;
        BaritoneAPI.getSettings().colorBestPathSoFar.value = Color.YELLOW;

        String mode = args[0];

        if (mode == null || mode.equalsIgnoreCase("help")) {
            Command.sendChatMessage("Path controls baritone's pathfinding. Try the following commands to get started:\n" +
                    "&7path to <x> [y] <z>&r ~ &8Pathfind to a coordinate\n" +
                    "&7path thisway <length>&r ~ &8Pathfind to <length> blocks in front of you\n" +
                    "&7path stop&r ~ &8Cancels pathfinding\n" +
                    "&7path flag&r ~ &8Control baritone's settings");
            return;
        }

        switch (mode.toLowerCase()) {
            case "to":
                if (args[1] == null || args[2] == null) {
                    Command.sendChatMessage("&7to&r allows you to specify where baritone paths to. Use it as such:\n" +
                            "&7to <x> <z>\n" +
                            "&7to <x> <y> <z>");
                    return;
                }
                String sX = args[1], sY = args[2], sZ = sY == null ? null : args[3];
                boolean hasY = sZ != null;
                try {
                    int x = Integer.parseInt(sX);
                    int y = Integer.parseInt(sY);
                    int z = Integer.parseInt(hasY ? sZ : sY);

                    if (hasY) {
                        BaritoneAPI.getPathingBehavior().setGoal(new GoalBlock(x, y, z));
                    } else {
                        BaritoneAPI.getPathingBehavior().setGoal(new GoalXZ(x, z));
                    }

                    BaritoneAPI.getPathingBehavior().path();
                } catch (NumberFormatException e) {
                    Command.sendChatMessage("Coordinates must be numerical!");
                    return;
                }
                break;
            case "thisway":
                String rangeString = args[1];
                if (rangeString == null) {
                    Command.sendChatMessage("&7thisway&r allows you to pathfind in the direction you're looking at. Its only argument is how far you'd like to go.");
                    return;
                }
                try {
                    int range = Integer.parseInt(rangeString);

                    int x = (int) (EntityUtil.getRelativeX(Wrapper.getPlayer().rotationYaw) * range + Wrapper.getPlayer().posX);
                    int y = (int) (EntityUtil.getRelativeZ(Wrapper.getPlayer().rotationYaw) * range + Wrapper.getPlayer().posY);

                    BaritoneAPI.getPathingBehavior().setGoal(new GoalXZ(x, y));
                    BaritoneAPI.getPathingBehavior().path();
                } catch (NumberFormatException e) {
                    Command.sendChatMessage("Length must be numerical!");
                    return;
                }
                break;
            case "stop":
            case "cancel":
            case "abort":
            case "look,aflyingelephant!":
                if (BaritoneAPI.getPathingBehavior().isPathing() || BaritoneAPI.getPathingBehavior().getGoal() != null) {
                    BaritoneAPI.getPathingBehavior().cancel();
                    BaritoneAPI.getPathingBehavior().setGoal(null);
                    Command.sendChatMessage("Canceled pathfinding.");
                } else {
                    Command.sendChatMessage("There is no active path to cancel.");
                }
            case "flag":

                break;
            default:
                Command.sendChatMessage("That doesn't seem like a valid &7path&r command. Try &7path help&r!");
        }
    }
}
