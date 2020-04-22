package me.zeroeightsix.kami.module.modules.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.GuiScreenEvent;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.event.events.ServerConnectedEvent;
import me.zeroeightsix.kami.event.events.ServerDisconnectedEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.play.server.SPacketSoundEffect;

import java.util.Random;

import static me.zeroeightsix.kami.KamiMod.EVENT_BUS;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage;

/**
 * Created by 086 on 22/03/2018.
 * Updated by Qther on 05/03/20
 * Updated by dominikaaaa on 30/03/20
 */
@Module.Info(
        name = "AutoFish",
        category = Module.Category.MISC,
        description = "Automatically catch fish",
        alwaysListening = true
)
public class AutoFish extends Module {
    private boolean recastHide = false;
    private static ServerData cServer;
    Random random;

    private Setting<Boolean> defaultSetting = register(Settings.b("Defaults", false));
    private Setting<Integer> baseDelay = register(Settings.integerBuilder("Throw Delay").withValue(450).withMinimum(50).withMaximum(1000).build());
    private Setting<Integer> extraDelay = register(Settings.integerBuilder("Catch Delay").withValue(300).withMinimum(0).withMaximum(1000).build());
    private Setting<Integer> variation = register(Settings.integerBuilder("Variation").withValue(50).withMinimum(0).withMaximum(1000).build());
    private Setting<Boolean> recast = register(Settings.booleanBuilder("Recast").withValue(false).withVisibility(v -> recastHide).build());

    public void onUpdate() {
        if (defaultSetting.getValue()) defaults();
        if (mc.player != null && recast.getValue()) {
            mc.rightClickMouse();
            recast.setValue(false);
        }
    }

    @EventHandler
    public Listener<ServerDisconnectedEvent> disconnectedEventListener = new Listener<>(event -> {
        if (isDisabled()) return;
        recast.setValue(true);
    });

    @EventHandler
    private Listener<PacketEvent.Receive> receiveListener = new Listener<>(this::invoke);

    private void invoke(PacketEvent.Receive e) {
        if (isEnabled() && e.getPacket() instanceof SPacketSoundEffect) {
            SPacketSoundEffect pck = (SPacketSoundEffect) e.getPacket();
            if (pck.getSound().getSoundName().toString().toLowerCase().contains("entity.bobber.splash")) {
                if (mc.player.fishEntity == null) return;
                int soundX = (int) pck.getX();
                int soundZ = (int) pck.getZ();
                int fishX = (int) mc.player.fishEntity.posX;
                int fishZ = (int) mc.player.fishEntity.posZ;
                if (kindaEquals(soundX, fishX) && kindaEquals(fishZ, soundZ)) {
                    new Thread(() -> {
                        random = new Random();
                        try {
                            Thread.sleep(extraDelay.getValue() + random.ints(1, -variation.getValue(), variation.getValue()).findFirst().getAsInt());
                        } catch (InterruptedException ignored) {
                        }
                        mc.rightClickMouse();
                        random = new Random();
                        try {
                            Thread.sleep(baseDelay.getValue() + random.ints(1, -variation.getValue(), variation.getValue()).findFirst().getAsInt());
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        mc.rightClickMouse();
                    }).start();
                }
            }
        }
    }

    @EventHandler
    public Listener<GuiScreenEvent.Closed> serverConnectedEvent = new Listener<>(event -> {
        if (isEnabled() && event.getScreen() instanceof GuiConnecting) {
            cServer = mc.currentServerData;
            EVENT_BUS.post(new ServerConnectedEvent());
        }
    });

    @EventHandler
    public Listener<GuiScreenEvent.Displayed> serverDisconnectedEvent = new Listener<>(event -> {
        if (isEnabled() && event.getScreen() instanceof GuiDisconnected && (cServer != null || mc.currentServerData != null)) {
            EVENT_BUS.post(new ServerDisconnectedEvent());
        }
    });

    private boolean kindaEquals(int kara, int ni) {
        return ni == kara || ni == kara - 1 || ni == kara + 1;
    }

    private void defaults() {
        baseDelay.setValue(450);
        extraDelay.setValue(300);
        variation.setValue(50);
        defaultSetting.setValue(false);
        sendChatMessage(getChatName() + "Set to defaults!");
        closeSettings();
    }
}
