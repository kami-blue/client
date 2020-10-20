package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.MovementUtils
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.io.*

@Module.Info(
        name = "LoginMessage",
        description = "Sends a given message to public chat on login.",
        category = Module.Category.CHAT,
        showOnArray = false
)
object LoginMessage : Module() {
    private val sendAfterMoving = register(Settings.b("SendAfterMoving", false))

    private var loginMessage: String? = null
    private var sent = false
    private var moved = false

    init {
        listener<ConnectionEvent.Disconnect> {
            sent = false
            moved = false
        }

        listener<SafeTickEvent> { event ->
            if (event.phase != TickEvent.Phase.END) return@listener

            if (!sent && (!sendAfterMoving.value || moved)) {
                loginMessage?.let {
                    MessageSendHelper.sendServerMessage(it)
                    sent = true
                }
            }

            if (!moved) moved = MovementUtils.isMoving
        }
    }

    override fun onEnable() {
        try {
            MessageSendHelper.sendChatMessage("$chatName Trying to find '&7loginmsg.txt&f'")
            val reader = BufferedReader(InputStreamReader(FileInputStream("loginmsg.txt"), "UTF-8"))

            loginMessage = reader.readLine()
            MessageSendHelper.sendChatMessage("$chatName Found '&7loginmsg.txt&f'!")

            reader.close()
        } catch (e: FileNotFoundException) {
            MessageSendHelper.sendErrorMessage("$chatName The file '&7loginmsg.txt&f' was not found in your .minecraft folder. Create it and add a message to enable this module.")
            disable()
        } catch (e: IOException) {
            KamiMod.log.error(e)
            disable()
        }
    }
}
