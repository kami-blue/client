package me.zeroeightsix.kami.module.modules.chat;

import com.mrpowergamerbr.temmiewebhook.DiscordMessage;
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.event.events.GuiScreenEvent;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.event.events.ServerConnectedEvent;
import me.zeroeightsix.kami.event.events.ServerDisconnectedEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.modules.client.InfoOverlay;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.TimeUtil;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.play.server.SPacketChat;

import static me.zeroeightsix.kami.KamiMod.EVENT_BUS;
import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;
import static me.zeroeightsix.kami.util.MessageDetectionHelper.*;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendErrorMessage;

/**
 * @author dominikaaaa
 * Created by dominikaaaa on 26/03/20
 * Updated by dominikaaaa on 28/03/20
 */
@Module.Info(
        name = "DiscordNotifs",
        category = Module.Category.CHAT,
        description = "Sends your chat to a set Discord channel",
        showOnArray = Module.ShowOnArray.OFF,
        alwaysListening = true
)
public class DiscordNotifs extends Module {
    private Setting<Boolean> timeout = register(Settings.b("Timeout", true));
    private Setting<Integer> timeoutTime = register(Settings.integerBuilder().withName("Seconds").withMinimum(0).withMaximum(120).withValue(10).withVisibility(v -> timeout.getValue()).build());
    private Setting<Boolean> time = register(Settings.b("Timestamp", true));
    private Setting<Boolean> importantPings = register(Settings.b("Important Pings", false));
    private Setting<Boolean> disconnect = register(Settings.b("Disconnect Msgs", true));
    private Setting<Boolean> all = register(Settings.b("All Messages", false));
    private Setting<Boolean> queue = register(Settings.booleanBuilder("Queue Position").withValue(true).withVisibility(v -> !all.getValue()).build());
    private Setting<Boolean> restart = register(Settings.booleanBuilder("Restart Msgs").withValue(true).withVisibility(v -> !all.getValue()).build());
    private Setting<Boolean> direct = register(Settings.booleanBuilder("Received DMs").withValue(true).withVisibility(v -> !all.getValue()).build());
    private Setting<Boolean> directSent = register(Settings.booleanBuilder("Send DMs").withValue(true).withVisibility(v -> !all.getValue()).build());
    public Setting<String> url = register(Settings.s("URL", "unchanged"));
    public Setting<String> pingID = register(Settings.s("Ping ID", "unchanged"));
    public Setting<String> avatar = register(Settings.s("Avatar", KamiMod.GITHUB_LINK + "assets/raw/assets/assets/icons/kami.png"));

    private static ServerData cServer;

    /* Listeners to send the messages */
    @EventHandler
    public Listener<PacketEvent.Receive> listener0 = new Listener<>(event -> {
        if (mc.player == null || isDisabled()) return;
        if (!(event.getPacket() instanceof SPacketChat)) return;

        SPacketChat sPacketChat = (SPacketChat) event.getPacket();
        String message = sPacketChat.getChatComponent().getUnformattedText();
        if (timeout(message) && shouldSend(all.getValue(), restart.getValue(), direct.getValue(), directSent.getValue(), queue.getValue(), importantPings.getValue(), message)) {
            sendMessage(getPingID(message) + getMessageType(direct.getValue(), directSent.getValue(), message, getServer()) + getTime() + message, avatar.getValue());
        }
    });

    @EventHandler
    public Listener<ServerConnectedEvent> listener1 = new Listener<>(event -> {
        if (isDisabled()) return;
        if (!disconnect.getValue()) return;
        sendMessage(getPingID("KamiBlueMessageType1") + getTime() + getMessageType(direct.getValue(), directSent.getValue(), "KamiBlueMessageType1", getServer()), avatar.getValue());
    });

    @EventHandler
    public Listener<ServerDisconnectedEvent> listener2 = new Listener<>(event -> {
        if (isDisabled()) return;
        if (!disconnect.getValue()) return;
        sendMessage(getPingID("KamiBlueMessageType2") + getTime() + getMessageType(direct.getValue(), directSent.getValue(), "KamiBlueMessageType2", getServer()), avatar.getValue());
    });

    /* Getters for messages */
    private static long startTime = 0;
    private boolean timeout(String message) {
        if (!timeout.getValue()) return true;
        else if (isRestart(restart.getValue(), message) || isDirect(direct.getValue(), message) || isDirectOther(directSent.getValue(), message)) return true;
        if (startTime == 0) startTime = System.currentTimeMillis();
        if (startTime + (timeoutTime.getValue() * 1000) <= System.currentTimeMillis()) { // 1 timeout = 1 second = 1000 ms
            startTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    /* Text formatting and misc methods */
    private String getPingID(String message) {
        if (isRestart(restart.getValue(), message) || isDirect(direct.getValue(), message) || isDirectOther(directSent.getValue(), message) || isImportantQueue(importantPings.getValue(), message)) return formatPingID();
        else if ((message.equals("KamiBlueMessageType1")) || (message.equals("KamiBlueMessageType2"))) return formatPingID();
        else return "";
    }

    private String formatPingID() {
        if (!importantPings.getValue()) return "";
        else return "<@!" + pingID.getValue() + ">: ";
    }

    private String getServer() {
        if (cServer == null) return "the server";
        else return cServer.serverIP;
    }

    private String getTime() {
        if (!time.getValue()) return "";
        InfoOverlay info = MODULE_MANAGER.getModuleT(InfoOverlay.class);
        return "[" + TimeUtil.getFinalTime(info.timeUnitSetting.getValue(), info.timeTypeSetting.getValue(), info.doLocale.getValue()) + "] ";
    }

    private void sendMessage(String content, String avatarUrl) {
        TemmieWebhook tm = new TemmieWebhook(url.getValue());
        DiscordMessage dm = new DiscordMessage(KamiMod.MODNAME + " " + KamiMod.MODVER, content, avatarUrl);
        tm.sendMessage(dm);
    }

    /* Always on status code */
    public void onUpdate() {
        if (isDisabled()) return;
        if (url.getValue().equals("unchanged")) {
            sendErrorMessage(getChatName() + "You must first set a webhook url with the '&7" + Command.getCommandPrefix() + "discordnotifs&r' command");
            disable();
        }
        else if (pingID.getValue().equals("unchanged") && importantPings.getValue()) {
            sendErrorMessage(getChatName() + "For Pings to work, you must set a Discord ID with the '&7" + Command.getCommandPrefix() + "discordnotifs&r' command");
            disable();
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
}
