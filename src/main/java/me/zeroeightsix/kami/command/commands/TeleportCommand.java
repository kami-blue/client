package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.command.syntax.parsers.ModuleParser;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;

import java.text.DecimalFormat;

import static me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage;
import static me.zeroeightsix.kami.util.text.MessageSendHelper.sendErrorMessage;

/**
 * Robeart is cool and made potentia
 * Updated by d1gress/Qther on 8/12/2019.
 */
public class TeleportCommand extends Command {

    private DecimalFormat df = new DecimalFormat("#.###");
    public static long lastTp;
    public static Vec3d lastPos;
    public static Vec3d finalPos;
    public static double blocksPerTeleport;
    public static boolean disable = false;
    public static boolean feedback = true;

    public TeleportCommand() {
        super("teleport", new ChunkBuilder()
                .append("x/stop", true, new ModuleParser())
                .append("y", true)
                .append("z", true)
                .append("blocks per tp", false)
                .build(), "tp", "clip");
        setDescription("Potentia teleport exploit");
    }

    public static void teleport(Minecraft mc, Vec3d pos, boolean feedback) {
        finalPos = pos;
        disable = false;
        TeleportCommand.feedback = feedback;
        teleport(mc);
    }

    private static void teleport(Minecraft mc) {
        Vec3d tpDirectionVec = finalPos.subtract(mc.player.posX, mc.player.posY, mc.player.posZ).normalize();

        if (mc.world.isBlockLoaded(mc.player.getPosition())) {
            lastPos = new Vec3d(mc.player.posX, mc.player.posY, mc.player.posZ);
            if (finalPos.distanceTo(new Vec3d(mc.player.posX, mc.player.posY, mc.player.posZ)) < 0.3 || blocksPerTeleport == 0) {
                if (feedback) sendChatMessage("Teleport Finished!");
                disable = true;
                return;
            } else {
                mc.player.setVelocity(0, 0, 0);
            }

            if (disable) return;

            if (finalPos.distanceTo(new Vec3d(mc.player.posX, mc.player.posY, mc.player.posZ)) >= blocksPerTeleport) {
                final Vec3d vec = tpDirectionVec.scale(blocksPerTeleport);
                mc.player.setPosition(mc.player.posX + vec.x, mc.player.posY + vec.y, mc.player.posZ + vec.z);
            } else {
                final Vec3d vec = tpDirectionVec.scale(finalPos.distanceTo(new Vec3d(mc.player.posX, mc.player.posY, mc.player.posZ)));
                mc.player.setPosition(mc.player.posX + vec.x, mc.player.posY + vec.y, mc.player.posZ + vec.z);
                disable = true;
            }

            if (disable) return;
            lastTp = System.currentTimeMillis();
        } else if (lastTp + 2000L > System.currentTimeMillis()) {
            mc.player.setPosition(lastPos.x, lastPos.y, lastPos.z);
        }
    }

    @Override
    public void call(String[] args) {
        if (args[0].equalsIgnoreCase("stop")) {
            sendChatMessage("Teleport Cancelled!");
            disable = true;
            return;
        }

        blocksPerTeleport = 10000.0d;
        if (args.length >= 4 && args[3] != null) {
            blocksPerTeleport = Double.parseDouble(args[3]);
        }

        if (args.length >= 3) {
            try {
                final double x = args[0].equals("~") ? mc.player.posX : args[0].charAt(0) == '~' ? Double.parseDouble(args[0].substring(1)) + mc.player.posX : Double.parseDouble(args[0]);
                final double y = args[1].equals("~") ? mc.player.posY : args[1].charAt(0) == '~' ? Double.parseDouble(args[1].substring(1)) + mc.player.posY : Double.parseDouble(args[1]);
                final double z = args[2].equals("~") ? mc.player.posZ : args[2].charAt(0) == '~' ? Double.parseDouble(args[2].substring(1)) + mc.player.posZ : Double.parseDouble(args[2]);
                finalPos = new Vec3d(x, y, z);
                disable = false;
                feedback = true;
                teleport(mc);
                sendChatMessage("\n&aTeleporting to \n&cX: &b" + df.format(x) + "&a, \n&cY: &b" + df.format(y) + "&a, \n&cZ: &b" + df.format(z) + "\n&aat &b" + df.format(blocksPerTeleport) + "&c blocks per teleport.");
            } catch (NullPointerException e) {
                sendErrorMessage("Null Pointer Exception Caught!");
            }

        }

    }
}
