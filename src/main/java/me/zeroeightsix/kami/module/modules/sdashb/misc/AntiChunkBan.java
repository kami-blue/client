package me.zeroeightsix.kami.module.modules.sdashb.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.server.SPacketChunkData;

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
    private Setting<Boolean> noPacket = register(Settings.b("No Chunk Packet",false));
    private Setting<Boolean> kill = register(Settings.b("/kill", false));
    private Setting<Float> delayTime = register(Settings.f("Kill Delay", 10));
    private Setting<Boolean> disable = register(Settings.b("Disable After Kill", false));

    @Override
    public void onUpdate() {
        if (mc.player == null) return;

        if (kill.getValue()) {
            if (Minecraft.getMinecraft().getCurrentServerData() != null) {
                if (startTime == 0) startTime = System.currentTimeMillis();
                if (startTime + delayTime.getValue() <= System.currentTimeMillis()) {
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

    @EventHandler
    Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {
        if (noPacket.getValue()) {
            if (mc.player == null) return;
            if (event.getPacket() instanceof SPacketChunkData) {
                event.cancel();
            }
        }
    });
}