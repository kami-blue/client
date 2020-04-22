package me.zeroeightsix.kami.module.modules.combat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.module.Module;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

/**
 * @author dominikaaaa
 * Thanks cookie uwu
 */
@Module.Info(
        name = "Criticals",
        category = Module.Category.COMBAT,
        description = "Always do critical attacks"
)
public class Criticals extends Module {
    @EventHandler
    private Listener<AttackEntityEvent> attackEntityEventListener = new Listener<>(event -> {
        if (!mc.player.isInWater() && !mc.player.isInLava()) {
            if (mc.player.onGround) { /* lol Minecraft checks for criticals if you're not on a block so just say you're not */
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.1f, mc.player.posZ, false));
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                mc.player.onCriticalHit(event.getTarget());
            }
        }
    });
}