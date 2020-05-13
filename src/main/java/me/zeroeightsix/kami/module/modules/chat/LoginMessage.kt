package me.zeroeightsix.kami.module.modules.chat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.MessageSendHelper
import net.minecraft.network.play.server.SPacketChat
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException

@Module.Info(
        name = "LoginMessage",
        description = "Sends a given message to public chat on login.",
        category = Module.Category.CHAT,
        showOnArray = Module.ShowOnArray.OFF
)
class LoginMessage : Module() {
    private var loginMessage: String? = null
    private var sent = false

    override fun onEnable() {
        val reader: BufferedReader

        try {
            MessageSendHelper.sendChatMessage(chatName + "Finding login message from loginmsg.txt...")
            reader = BufferedReader(InputStreamReader(FileInputStream("loginmsg.txt"), "UTF-8"))

            loginMessage = reader.readLine()

            reader.close()
        } catch (e: FileNotFoundException) {
            MessageSendHelper.sendErrorMessage(chatName + "The file '&7loginmsg.txt&f' was not found in your .minecraft folder. Create it and add a message to enable this module.")

            disable()
        } catch (e: IOException) {
            KamiMod.log.error(e)
        }
    }

    @EventHandler
    var packetReceived = Listener(EventHook { event: PacketEvent.Receive ->
        if (event.packet is SPacketChat && !sent) {
            mc.player.sendChatMessage(loginMessage)

            sent = true
        }
    })
}
