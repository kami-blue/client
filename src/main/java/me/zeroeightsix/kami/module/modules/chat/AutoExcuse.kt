package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.network.play.server.SPacketUpdateHealth
import java.io.File

@Module.Info(
        name = "AutoExcuse",
        description = "Makes an excuse for you when you die",
        category = Module.Category.CHAT
)
object AutoExcuse : Module() {
    private const val CLIENT_NAME = "%CLIENT%"
    private val mode = register(Settings.e<Mode>("Mode", Mode.DEFAULT))
    private val file = File("excuses.txt")

    private var excuses: MutableList<String> = mutableListOf(
            "Sorry, im using $CLIENT_NAME client",
            "My ping is so bad",
            "I was changing my config :(",
            "Why did my AutoTotem break",
            "I was desynced",
            "Stupid hackers killed me",
            "Wow, so many try hards",
            "Lagggg",
            "I wasn't trying",
            "I'm not using $CLIENT_NAME client"
    )

    private val backup = excuses.toMutableList()

    private val clients = arrayOf(
            "Future",
            "Salhack",
            "Pyro",
            "Impact"
    )

    private var timer = TimerUtils.TickTimer(TimerUtils.TimeUnit.SECONDS)

    init {
        listener<PacketEvent.Receive> {
            if (mc.player == null || it.packet !is SPacketUpdateHealth) return@listener
            if (it.packet.health <= 0f && timer.tick(3L)) {
                    MessageSendHelper.sendServerMessage(getExcuse())
            }
        }
    }

    override fun onToggle() {
        if (mode.value == Mode.READ_FROM_FILE) {
            if (file.exists() && file.length() != 0L) {
                excuses.clear()
                file.forEachLine { excuses.add(it) }
            } else {
                MessageSendHelper.sendErrorMessage("$chatName File is empty / file does not exist! Disabling.")
                MessageSendHelper.sendWarningMessage("$chatName To create custom excuses, create a file named 'excuses.txt' inside your '.minecraft' folder.")
                disable()
            }
        } else excuses = backup
    }

    private fun getExcuse() = excuses.random().replace(CLIENT_NAME, clients.random())

    private enum class Mode{
        READ_FROM_FILE,
        DEFAULT
    }
}
