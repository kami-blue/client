// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module.modules.misc;

import me.zeroeightsix.kami.command.Command;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.util.math.BlockPos;
import java.util.function.Predicate;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.network.play.client.CPacketUpdateSign;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import me.zero.alpine.listener.EventHandler;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.module.Module;

@Info(name = "ConsoleSpam", description = "Spams Spigot consoles by sending invalid UpdateSign packets", category = Category.MISC)
public class ConsoleSpam extends Module
{
    @EventHandler
    public Listener<PacketEvent.Send> sendListener;
    
    public ConsoleSpam() {
        BlockPos location;
        NetHandlerPlayClient field_71174_a;
        final CPacketUpdateSign cPacketUpdateSign;
        this.sendListener = new Listener<PacketEvent.Send>(event -> {
            if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
                location = ((CPacketPlayerTryUseItemOnBlock)event.getPacket()).func_187023_a();
                field_71174_a = Wrapper.getPlayer().field_71174_a;
                new CPacketUpdateSign(location, new TileEntitySign().field_145915_a);
                field_71174_a.func_147297_a((Packet)cPacketUpdateSign);
            }
        }, (Predicate<PacketEvent.Send>[])new Predicate[0]);
    }
    
    public void onEnable() {
        Command.sendChatMessage("[ConsoleSpam] Every time you right click a sign, a warning will appear in console.");
        Command.sendChatMessage("[ConsoleSpam] Use an autoclicker to automate this process.");
    }
}
