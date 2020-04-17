package me.zeroeightsix.kami.module.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.Wrapper;

import static me.zeroeightsix.kami.gui.kami.DisplayGuiScreen.getScale;
import static me.zeroeightsix.kami.util.InfoCalculator.round;

/**
 * @author S-B99
 * Thanks Brady and cooker and leij for helping me not be completely retarded 
 */
@Module.Info(name = "LagNotifier", description = "Displays a warning when the server is lagging", category = Module.Category.PLAYER)
public class LagNotifier extends Module {
    private Setting<Double> timeout = register(Settings.doubleBuilder().withName("Timeout").withValue(1.0).withMinimum(0.0).withMaximum(10.0).build());
    private long serverLastUpdated;

    @Override
    public void onRender() {
        if (!(timeout.getValue() * 1000L <= System.currentTimeMillis() - serverLastUpdated)) return;
        String text = "Server Not Responding! " + timeDifference() + "s";
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
}
