package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.command.syntax.parsers.ModuleParser;
import net.minecraft.util.math.Vec3d;

import static me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage;

public class VClipCommand extends Command {
    public VClipCommand() {
        super("vclip", new ChunkBuilder()
                        .append("x", true, new ModuleParser())
                        .append("blocks per tp", false).build());
                setDescription("Vertical Clip");
    }
    public void call(String[] args) {
        sendChatMessage("Teleporting you " + args[0] + " block[s] in the air.");
        double blocksPerTeleport = 10000.0d;
        if (args.length >= 2 && args[1] != null) {
            blocksPerTeleport = Double.parseDouble(args[1]);
        }
        TeleportCommand.blocksPerTeleport = blocksPerTeleport;
        TeleportCommand.teleport(mc, new Vec3d(mc.player.posX, mc.player.posY + Double.parseDouble(args[0]), mc.player.posZ), true);
    }
}
