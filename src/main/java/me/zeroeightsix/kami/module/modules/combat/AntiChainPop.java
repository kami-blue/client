package me.zeroeightsix.kami.module.modules.combat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.network.play.server.SPacketEntityStatus;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;
import static me.zeroeightsix.kami.module.modules.gui.InfoOverlay.getItems;

/**
 * @author S-B99
 * Created by S-B99 on 25/03/20
 *
 * Event / Packet mode taken from CliNet
 * https://github.com/DarkiBoi/CliNet/blob/fd225a5c8cc373974b0c9a3457acbeed206e8cca/src/main/java/me/zeroeightsix/kami/module/modules/combat/TotemPopCounter.java
 */
@Module.Info(name = "AntiChainPop", description = "Enables Surround when popping a totem", category = Module.Category.COMBAT)
public class AntiChainPop extends Module {
    private Setting<Mode> mode = register(Settings.e("Mode", Mode.PACKET));
    private int totems = 0;

    @EventHandler
    public Listener<PacketEvent.Receive> selfPopListener = new Listener<>(event -> {
        if (mc.player == null || !mode.getValue().equals(Mode.PACKET)) return;

        if (event.getPacket() instanceof SPacketEntityStatus) {
            SPacketEntityStatus packet = (SPacketEntityStatus) event.getPacket();
            if (packet.getOpCode() == 35) {
                Entity entity = packet.getEntity(mc.world);
                if (entity.getDisplayName().equals(mc.player.getDisplayName()))
                    packetMode();
            }
        }

    });

    public void onUpdate() {
        if (mc.player == null) return;
        if (mode.getValue().equals(Mode.ITEMS)) {
            itemMode();
        }
    }

    private void itemMode() {
        int old = totems;
        if (getItems(Items.TOTEM_OF_UNDYING) < old) {
            Surround surround = (Surround) MODULE_MANAGER.getModule(Surround.class);
            surround.autoDisable.setValue(true);
            surround.enable();
        }
        totems = getItems(Items.TOTEM_OF_UNDYING);
    }

    private void packetMode() {
        Surround surround = (Surround) MODULE_MANAGER.getModule(Surround.class);
        surround.autoDisable.setValue(true);
        surround.enable();
    }

    public void onEnable() { totems = 0; }
    public void onDisable() { totems = 0; }
    private enum Mode { ITEMS, PACKET }
}
