package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.text.MessageSendHelper
import java.io.*

@Module.Info(
        name = "LoginMessage",
        description = "Sends a given message to public chat on login.",
        category = Module.Category.CHAT,
        showOnArray = Module.ShowOnArray.OFF
)
object LoginMessage : Module() {
    private var loginMessage: String? = null
    private var sent = false

    init {
        listener<ConnectionEvent.Disconnect> {
            sent = false
        }
    }

    override fun onEnable() {
        val reader: BufferedReader

        try {
            MessageSendHelper.sendChatMessage("$chatName Trying to find '&7loginmsg.txt&f'")
            reader = BufferedReader(InputStreamReader(FileInputStream("loginmsg.txt"), "UTF-8"))

            loginMessage = reader.readLine()

            reader.close()
        } catch (e: FileNotFoundException) {
            MessageSendHelper.sendErrorMessage("$chatName The file '&7loginmsg.txt&f' was not found in your .minecraft folder. Create it and add a message to enable this module.")
            disable()
            return
        } catch (e: IOException) {
            KamiMod.log.error(e)
            disable()
            return
        }
        MessageSendHelper.sendChatMessage("$chatName Found '&7loginmsg.txt&f'!")
    }

    override fun onUpdate(event: SafeTickEvent) {
        if (!sent && loginMessage != null) {
            mc.player.sendChatMessage(loginMessage!!)
            sent = true
        }
    }
}
