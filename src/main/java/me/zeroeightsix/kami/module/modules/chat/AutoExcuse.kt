package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendServerMessage
import net.minecraft.network.play.server.SPacketUpdateHealth
import java.io.File

@Module.Info(
        name = "AutoExcuse",
        description = "Makes an excuse for you when you die",
        category = Module.Category.CHAT,
        modulePriority = 500
)
object AutoExcuse : Module() {
    private val mode = register(Settings.e<Mode>("Mode", Mode.INTERNAL))

    private enum class Mode {
        INTERNAL, EXTERNAL
    }

    private const val CLIENT_NAME = "%CLIENT%"
    private val defaultExcuses= arrayOf(
            "Sorry, im using $CLIENT_NAME client",
            "My ping is so bad",
            "I was changing my config :(",
            "Why did my AutoTotem break",
            "I was desynced",
            "Stupid hackers killed me",
            "Wow, so many try hards",
            "Lagggg",
            "I wasn't trying",
            "I'm not using $CLIENT_NAME client",
            "Thers to much lag",
            "My dog ate my pc",
            "Sorry, $CLIENT_NAME Client is really bad",
            "I was lagging",
            "He was cheating!",
            "Your hacking!",
            "Lol imagine actully trying",
            "I didn't move my mouse",
            "I was playing on easy mode(;",
            "My wifi went down",
            "I'm playing vanila",
            "My optifine didn't work",
            "The CPU cheated!"
    )

    private val file = File(KamiMod.DIRECTORY + "excuses.txt")
    private var loadedExcuses = defaultExcuses

    private val clients = arrayOf(
            "Future",
            "Salhack",
            "Pyro",
            "Rusherhack",
            "Impact"
    )

    private val timer = TimerUtils.TickTimer(TimerUtils.TimeUnit.SECONDS)

    init {
        listener<PacketEvent.Receive> {
            if (mc.player == null || loadedExcuses.isEmpty() || it.packet !is SPacketUpdateHealth) return@listener
            if (it.packet.health <= 0f && timer.tick(3L)) {
                sendServerMessage(getExcuse())
            }
        }
    }

    override fun onEnable() {
        loadedExcuses = if (mode.value == Mode.EXTERNAL) {
            if (file.exists()) {
                val cacheList = ArrayList<String>()
                try {
                    file.forEachLine { if (it.isNotBlank()) cacheList.add(it.trim()) }
                    MessageSendHelper.sendChatMessage("$chatName Loaded spammer messages!")
                } catch (e: Exception) {
                    KamiMod.log.error("Failed loading excuses", e)
                }
                cacheList.toTypedArray()
            } else {
                file.createNewFile()
                MessageSendHelper.sendErrorMessage("$chatName Excuses file is empty!" +
                        ", please add them in the &7excuses.txt&f under the &7.minecraft/kamiblue&f directory.")
                defaultExcuses
            }
        } else {
            defaultExcuses
        }
    }

    private fun getExcuse() = loadedExcuses.random().replace(CLIENT_NAME, clients.random())
}
