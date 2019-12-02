package me.zeroeightsix.kami.module.modules.sdashb.misc;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.modules.sdashb.libs.EventStageable;
import me.zeroeightsix.kami.module.modules.sdashb.libs.network.EventReceivePacket;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.server.SPacketChunkData;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

@Module.Info(name = "AntiChunkBan", description = "Spams /kill, gets out of ban chunks.", category = Module.Category.MISC)

/***
 * Kill mode
 * @author Fums
 * @coauthor S-B99
 * Updated by S-B99 on 01/12/19
 */
/***
 * Packet mode
 *  * Author Seth
 *  * 6/2/2019 @ 1:30 PM.
 *  https://github.com/seppukudevelopment/seppuku
 */
public class AntiChunkBan extends Module {

    private static long startTime = 0;
    private double delayTime = 10.0;
    private Setting<ModeThing> modeThing = register(Settings.e("Mode", ModeThing.PACKET));
    private Setting<Boolean> disable = register(Settings.b("Disable for Kill mode", false));

    private enum ModeThing {
        PACKET, KILL
    }

    @Override
    public void onUpdate() {
        if (mc.player == null) return;

        if (modeThing.getValue().equals(ModeThing.KILL)) {
            if (Minecraft.getMinecraft().getCurrentServerData() != null) {
                if (startTime == 0) startTime = System.currentTimeMillis();
                if (startTime + delayTime <= System.currentTimeMillis()) {
                    if (Minecraft.getMinecraft().getCurrentServerData() != null) {
                        Minecraft.getMinecraft().playerController.connection.sendPacket(new CPacketChatMessage("/kill"));
                    }
                    if (mc.player.getHealth() <= 0) {
                        mc.player.respawnPlayer();
                        mc.displayGuiScreen(null);
                        if (disable.getValue()) {
                            this.disable();
                        }
                    }
                    startTime = System.currentTimeMillis();
                }
            }
        }
    }

    @Listener
    public void onReceivePacket(EventReceivePacket event) {
        if (modeThing.getValue().equals(ModeThing.PACKET)) {
            if (event.getStage() == EventStageable.EventStage.PRE) {
                if (event.getPacket() instanceof SPacketChunkData) {
                    event.setCanceled(true);
                }
            }
        }
    }
}