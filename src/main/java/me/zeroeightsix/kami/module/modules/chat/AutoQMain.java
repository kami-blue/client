package me.zeroeightsix.kami.module.modules.chat;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.network.play.client.CPacketChatMessage;

import java.text.SimpleDateFormat;
import java.util.Date;

import static me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendWarningMessage;

/**
 * @author dominikaaaa
 * Updated by d1gress/Qther on 5/12/2019
 * Updated by dominikaaaa on 26/03/20
 */
@Module.Info(
        name = "AutoQMain",
        description = "Automatically does '/queue main' on servers",
        category = Module.Category.CHAT,
        showOnArray = Module.ShowOnArray.OFF
)
public class AutoQMain extends Module {

    private Setting<Boolean> showWarns = register(Settings.b("Show Warnings", true));
    private Setting<Boolean> connectionWarning = register(Settings.b("Connection Warning", true));
    private Setting<Boolean> dimensionWarning = register(Settings.b("Dimension Warning", true));
    private Setting<Double> delay = register(Settings.doubleBuilder("Wait time").withMinimum(0.2).withValue(7.1).withMaximum(10.0).build());

    private double delayTime;
    private double oldDelay = 0;

    @Override
    public void onUpdate() {
        if (mc.player == null) return;
        if (oldDelay == 0) oldDelay = delay.getValue();
        else if (oldDelay != delay.getValue()) {
            delayTime = delay.getValue();
            oldDelay = delay.getValue();
        }

        if (delayTime <= 0) {
            delayTime = (int) (delay.getValue() * 2400);
        } else if (delayTime > 0) {
            delayTime--;
            return;
        }

        if (mc.getCurrentServerData() == null && connectionWarning.getValue()) {
            sendMessage("&l&6Error: &r&6You are in singleplayer");
            return;
        }
        if (!mc.getCurrentServerData().serverIP.equalsIgnoreCase("2b2t.org") && connectionWarning.getValue()) {
            sendMessage("&l&6Warning: &r&6You are not connected to 2b2t.org");
            return;
        }
        if (mc.player.dimension != 1 && dimensionWarning.getValue()) {
            sendMessage("&l&6Warning: &r&6You are not in the end. Not running &b/queue main&7.");
            return;
        }
        sendQueueMain();
    }

    private void sendQueueMain() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        System.out.println(formatter.format(date));
        sendChatMessage("&7Run &b/queue main&7 at " + (formatter.format(date)));
        mc.playerController.connection.sendPacket(new CPacketChatMessage("/queue main"));
    }

    private void sendMessage(String message) { if (showWarns.getValue()) sendWarningMessage(getChatName() + message); }

    public void onDisable() { delayTime = 0; }
}
