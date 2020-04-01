package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.command.syntax.parsers.ModuleParser;
import me.zeroeightsix.kami.module.modules.hidden.Teleport;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;

import java.text.DecimalFormat;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;

/**
 * Robeart is cool and made potentia
 * Updated by d1gress/Qther on 8/12/2019.
 */

public class TeleportCommand extends Command {

    Minecraft mc = Minecraft.getMinecraft();
    DecimalFormat df = new DecimalFormat("#.###");

    public TeleportCommand() {
        super("teleport", new ChunkBuilder()
                .append("x/stop", true, new ModuleParser())
                .append("y", true)
                .append("z", true)
                .append("blocks per tp", false)
                .build(), "tp", "clip");
        setDescription("Potentia teleport exploit");
    }

    @Override
    public void call(String[] args) {
        if (args[0].equalsIgnoreCase("stop")) {
            Command.sendChatMessage("Teleport Cancelled!");
            MODULE_MANAGER.getModule(Teleport.class).disable();
            return;
        }

        if (args.length >= 4 && args[3] != null) {
            Teleport.blocksPerTeleport = Double.valueOf(args[3]);
        } else {
            Teleport.blocksPerTeleport = 10000.0d;
        }

        if (args.length >= 3) {
            try {
                final double x = args[0].equals("~") ? mc.player.posX : args[0].charAt(0) == '~' ? Double.parseDouble(args[0].substring(1)) + mc.player.posX : Double.parseDouble(args[0]);
                final double y = args[1].equals("~") ? mc.player.posY : args[1].charAt(0) == '~' ? Double.parseDouble(args[1].substring(1)) + mc.player.posY : Double.parseDouble(args[1]);
                final double z = args[2].equals("~") ? mc.player.posZ : args[2].charAt(0) == '~' ? Double.parseDouble(args[2].substring(1)) + mc.player.posZ : Double.parseDouble(args[2]);
                Teleport.finalPos = new Vec3d(x, y, z);
                MODULE_MANAGER.getModule(Teleport.class).enable();
                Command.sendChatMessage("\n&aTeleporting to \n&cX: &b" + df.format(x) + "&a, \n&cY: &b" + df.format(y) + "&a, \n&cZ: &b" + df.format(z) + "\n&aat &b" + df.format(Teleport.blocksPerTeleport) + "&c blocks per teleport.");
            } catch (NullPointerException e) {
                Command.sendErrorMessage("Null Pointer Exception Caught!");
            }

        }

    }
}
