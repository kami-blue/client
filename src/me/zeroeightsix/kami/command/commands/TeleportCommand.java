// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.command.commands;

import net.minecraft.util.math.Vec3d;
import me.zeroeightsix.kami.module.modules.hidden.Teleport;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.command.syntax.SyntaxParser;
import me.zeroeightsix.kami.command.syntax.parsers.ModuleParser;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import java.text.DecimalFormat;
import net.minecraft.client.Minecraft;
import me.zeroeightsix.kami.command.Command;

public class TeleportCommand extends Command
{
    Minecraft mc;
    DecimalFormat df;
    
    public TeleportCommand() {
        super("tp", new ChunkBuilder().append("x/stop", true, new ModuleParser()).append("y", true).append("z", true).append("blocks per tp", false).build());
        this.mc = Minecraft.func_71410_x();
        this.df = new DecimalFormat("#.###");
        this.setDescription("Potentia teleport exploit");
    }
    
    @Override
    public void call(final String[] args) {
        if (args[0].equalsIgnoreCase("stop")) {
            Command.sendChatMessage("Teleport Cancelled!");
            ModuleManager.getModuleByName("Teleport").disable();
            return;
        }
        if (args.length >= 4 && args[3] != null) {
            Teleport.blocksPerTeleport = Double.valueOf(args[3]);
        }
        else {
            Teleport.blocksPerTeleport = 10000.0;
        }
        if (args.length >= 3) {
            try {
                final double x = args[0].equals("~") ? this.mc.field_71439_g.field_70165_t : ((args[0].charAt(0) == '~') ? (Double.parseDouble(args[0].substring(1)) + this.mc.field_71439_g.field_70165_t) : Double.parseDouble(args[0]));
                final double y = args[1].equals("~") ? this.mc.field_71439_g.field_70163_u : ((args[1].charAt(0) == '~') ? (Double.parseDouble(args[1].substring(1)) + this.mc.field_71439_g.field_70163_u) : Double.parseDouble(args[1]));
                final double z = args[2].equals("~") ? this.mc.field_71439_g.field_70161_v : ((args[2].charAt(0) == '~') ? (Double.parseDouble(args[2].substring(1)) + this.mc.field_71439_g.field_70161_v) : Double.parseDouble(args[2]));
                Teleport.finalPos = new Vec3d(x, y, z);
                ModuleManager.getModuleByName("Teleport").enable();
                Command.sendChatMessage("\n&aTeleporting to \n&cX: &b" + this.df.format(x) + "&a, \n&cY: &b" + this.df.format(y) + "&a, \n&cZ: &b" + this.df.format(z) + "\n&aat &b" + this.df.format(Teleport.blocksPerTeleport) + "&c blocks per teleport.");
            }
            catch (NullPointerException e) {
                Command.sendErrorMessage("Null Pointer Exception Caught!");
            }
        }
    }
}
