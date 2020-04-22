package me.zeroeightsix.kami.module.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.gui.GuiChat;

import static me.zeroeightsix.kami.gui.kami.DisplayGuiScreen.getScale;
import static me.zeroeightsix.kami.util.MathsUtils.round;
import static me.zeroeightsix.kami.util.WebHelper.isDown;

/**
 * @author dominikaaaa
 * Thanks Brady and cooker and leij for helping me not be completely retarded 
 *
 * Updated by dominikaaaa on 19/04/20
 */
@Module.Info(
        name = "LagNotifier",
        description = "Displays a warning when the server is lagging",
        category = Module.Category.PLAYER
)
public class LagNotifier extends Module {
    private Setting<Double> timeout = register(Settings.doubleBuilder().withName("Timeout").withValue(1.0).withMinimum(0.0).withMaximum(10.0).build());
    private long serverLastUpdated;
    String text = "Server Not Responding! ";

    @Override
    public void onRender() {
        if (mc.currentScreen != null && !(mc.currentScreen instanceof GuiChat)) return;
        if (!(timeout.getValue() * 1000L <= System.currentTimeMillis() - serverLastUpdated)) return;
        if (shouldPing()) {
            if (isDown("1.1.1.1", 80, 1000)) {
                text = "Your internet is offline! ";
            } else {
                text = "Server Not Responding! ";
            }
        }
        text = text.replaceAll("! .*", "! " + timeDifference() + "s");
        FontRenderer renderer = Wrapper.getFontRenderer();

        int divider = getScale();
        /* 217 is the offset to make it go high, bigger = higher, with 0 being center */
        renderer.drawStringWithShadow(mc.displayWidth / divider / 2 - renderer.getStringWidth(text) / 2, mc.displayHeight / divider / 2 - 217, 255, 85, 85, text);
    }

    @EventHandler
    private Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> serverLastUpdated = System.currentTimeMillis());

    private double timeDifference() {
        return round((System.currentTimeMillis() - serverLastUpdated) / 1000d, 1);
    }

    private static long startTime = 0;
    private boolean shouldPing() {
        if (startTime == 0) startTime = System.currentTimeMillis();
        if (startTime + 1000 <= System.currentTimeMillis()) { // 1 second
            startTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }
}
