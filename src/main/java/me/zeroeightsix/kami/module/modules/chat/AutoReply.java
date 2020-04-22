package me.zeroeightsix.kami.module.modules.chat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.modules.misc.AntiAFK;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.network.play.server.SPacketChat;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendServerMessage;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendWarningMessage;

/**
 * @author Diamarald
 * Updated by dominikaaaa on 03/03/20
 */
@Module.Info(
        name = "AutoReply",
        description = "Automatically replies to messages",
        category = Module.Category.CHAT
)
public class AutoReply extends Module {
    public Setting<Boolean> customMessage = register(Settings.b("Custom Message", false));
    public Setting<String> message = register(Settings.stringBuilder("Custom Text").withValue("Use &7" + Command.getCommandPrefix() + "autoreply&r to modify this").withConsumer((old, value) -> {}).withVisibility(v -> customMessage.getValue()).build());
    public Setting<Boolean> customListener = register(Settings.b("Custom Listener", false));
    public Setting<String> listener = register(Settings.stringBuilder("Custom Listener Name").withValue("unchanged").withConsumer((old, value) -> {}).withVisibility(v -> customListener.getValue()).build());
    public Setting<Boolean> customReplyCommand = register(Settings.b("Custom Reply Command", false));
    public Setting<String> replyCommand = register(Settings.stringBuilder("Custom Reply Command").withValue("unchanged").withConsumer((old, value) -> {}).withVisibility(v -> customReplyCommand.getValue()).build());

    private String listenerDefault = "whispers:";
    private String replyCommandDefault = "r";

    @EventHandler
    public Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {
        if (MODULE_MANAGER.isModuleEnabled(AntiAFK.class) && MODULE_MANAGER.getModuleT(AntiAFK.class).autoReply.getValue()) return;
        if (event.getPacket() instanceof SPacketChat && ((SPacketChat) event.getPacket()).getChatComponent().getUnformattedText().contains(listenerDefault) && !((SPacketChat) event.getPacket()).getChatComponent().getUnformattedText().contains(mc.player.getName())) {
            if (customMessage.getValue()) {
               sendServerMessage("/" + replyCommandDefault + " " + message.getValue());
            } else {
                sendServerMessage("/" + replyCommandDefault + " I just automatically replied, thanks to KAMI Blue's AutoReply module!");
            }
        }
    });

    private static long startTime = 0;
    @Override
    public void onUpdate() {
        if (customListener.getValue()) listenerDefault = listener.getValue();
        else listenerDefault = "whispers:";

        if (customReplyCommand.getValue()) replyCommandDefault = replyCommand.getName();
        else replyCommandDefault = "r";

        if (startTime == 0) startTime = System.currentTimeMillis();
        if (startTime + 5000 <= System.currentTimeMillis()) { // 5 seconds in milliseconds
            if (customListener.getValue() && listener.getValue().equalsIgnoreCase("unchanged") && mc.player != null) {
                sendWarningMessage(getChatName() + " Warning: In order to use the custom listener, please run the &7" + Command.getCommandPrefix() + "autoreply&r =LISTENERNAME command to change it");
            }
            if (customReplyCommand.getValue() && replyCommand.getValue().equalsIgnoreCase("unchanged") && mc.player != null) {
                sendWarningMessage(getChatName() + " Warning: In order to use the custom reply command, please run the &7" + Command.getCommandPrefix() + "autoreply&r -REPLYCOMMAND command to change it");
            }
            startTime = System.currentTimeMillis();
        }
    }
}
