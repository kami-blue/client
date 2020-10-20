package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.text.MessageSendHelper
import java.io.*
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

@Module.Info(
        name = "PacketLogger",
        description = "Logs sent packets to a file",
        category = Module.Category.PLAYER
)
object PacketLogger : Module() {
    private val append = register(Settings.b("Append", false))
    private val clear = register(Settings.b("Clear", false))

    private const val filename = "KAMIBluePackets.txt"
    private val lines = ArrayList<String>()
    private val FORMAT = SimpleDateFormat("HH:mm:ss.SSS")

    override fun onEnable() {
        if (mc.player == null) disable() else if (append.value) readToList()
    }

    override fun onDisable() {
        if (mc.player != null) write()
    }

    init {
        listener<PacketEvent.Send> {
            if (mc.player == null) {
                disable()
                return@listener
            }

            /* see https://kotlinlang.org/docs/reference/basic-types.html#string-templates for usage of $*/
            lines.add("""
                ${FORMAT.format(Date())}
                ${it.packet.javaClass.simpleName}
                ${it.packet.javaClass}
                ${it.packet}
                """)
        }

        listener<SafeTickEvent> {
            if (mc.player.ticksExisted % 200 == 0) write()
        }
    }

    private fun write() {
        try {
            FileWriter(filename).also {
                for (line in lines) it.write(line)
                it.close()
            }
        } catch (e: IOException) {
            KamiMod.log.error("$chatName Error saving!")
            e.printStackTrace()
        }
        lines.clear()
    }

    private fun readToList() {
        val bufferedReader: BufferedReader

        try {
            bufferedReader = BufferedReader(InputStreamReader(FileInputStream(filename), StandardCharsets.UTF_8))
            var line: String
            lines.clear()
            while (bufferedReader.readLine().also { line = it } != null) {
                lines.add(line)
            }
            bufferedReader.close()
        } catch (ignored: IOException) {
        }
    }

    init {
        clear.settingListener = Setting.SettingListeners {
            if (clear.value) {
                lines.clear()
                write()
                MessageSendHelper.sendChatMessage("$chatName Packet log cleared!")
                clear.value = false
            }
        }
    }
}