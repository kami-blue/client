package me.zeroeightsix.kami.module.modules.chat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.util.MessageSendHelper;
import net.minecraft.network.play.server.SPacketChat;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

@Module.Info(
        name = "LoginMessage",
        description = "Sends a given message to public chat on login.",
        category = Module.Category.CHAT,
        showOnArray = Module.ShowOnArray.OFF
)
public class LoginMessage extends Module {
    private String loginMessage;
    private boolean sent = false;

    @Override
    protected void onEnable() {
        BufferedReader reader;

        try {
            MessageSendHelper.sendChatMessage(getChatName() + "Finding login message from loginmsg.txt...");
            reader = new BufferedReader(new FileReader("loginmsg.txt"));

            loginMessage = reader.readLine();

            reader.close();
        } catch (FileNotFoundException e) {
            MessageSendHelper.sendErrorMessage(getChatName() + "The file '&7loginmsg.txt&f' was not found in your .minecraft folder. Create it and add a message to enable this module.");
            disable();
        } catch (IOException e) {
            KamiMod.log.error(e);
        }
    }

    @EventHandler
    public Listener<PacketEvent.Receive> serverConnectedEventListener = new Listener<>(event -> {
        if (event.getPacket() instanceof SPacketChat && !sent) {
            mc.player.sendChatMessage(loginMessage);

            sent = true;
        }
    });
}
