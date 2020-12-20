package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.MovementUtils
import me.zeroeightsix.kami.util.text.MessageDetectionHelper
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendServerMessage
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener
import java.io.File
import java.io.FileReader

@Module.Info(
        name = "LoginMessage",
        description = "Sends a given message to public chat on login.",
        category = Module.Category.CHAT,
        showOnArray = Module.ShowOnArray.OFF,
        modulePriority = 150
)
object LoginMessage : Module() {
    private val sendAfterMoving = register(Settings.b("SendAfterMoving", false))

    private val file = File(KamiMod.DIRECTORY + "loginmsg.txt")
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
                    if (MessageDetectionHelper.isKamiCommand(it)) {
                        MessageSendHelper.sendKamiCommand(it)
                    } else {
                        sendServerMessage(it)
                    }
                    sent = true
                }
            }

            if (!moved) moved = MovementUtils.isMoving
        }
    }

    override fun onEnable() {
        if (file.exists()) {
            val fileReader = FileReader(file)
            try {
                fileReader.readLines().getOrNull(0)?.let {
                    if (it.isNotBlank()) loginMessage = it.trim()
                }
                MessageSendHelper.sendChatMessage("$chatName Loaded login message!")
            } catch (e: Exception) {
                MessageSendHelper.sendErrorMessage("$chatName Failed loading login message, $e")
                disable()
            }
            fileReader.close()
        } else {
            file.createNewFile()
            MessageSendHelper.sendErrorMessage("$chatName Login Message file is empty!" +
                    ", please add them in the &7loginmsg.txt&f under the &7.minecraft/kamiblue&f directory.")
            disable()
        }

    }
}
