package me.zeroeightsix.kami.module.modules.player

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.mixin.extension.pitch
import me.zeroeightsix.kami.mixin.extension.yaw
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.threads.defaultScope
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object PacketLogger : Module(
    name = "PacketLogger",
    description = "Logs sent packets to a file",
    category = Category.PLAYER
) {
    private val append by setting("Append", false)
    private val clear = setting("Clear", false)

    private val file = File("${KamiMod.DIRECTORY}packet_logger.txt")
    private val lines = Collections.synchronizedList(ArrayList<String>())
    private val sdf = SimpleDateFormat("HH:mm:ss.SSS")

    init {
        onEnable {
            if (append) read()
        }

        onDisable {
            write()
        }

        safeListener<PacketEvent.Send> {
            lines.add("${sdf.format(Date())}\n${it.packet.javaClass.simpleName}\n${it.packet.javaClass}\n${it.packet}")
            when (it.packet) {
                is CPacketPlayerDigging -> {
                    lines.add("\nMining - ${it.packet.position}@${it.packet.facing} - ${it.packet.action}")
                }
                is CPacketPlayer -> {
                    lines.add("\nRotation - Pitch: ${it.packet.pitch} Yaw: ${it.packet.yaw}")
                }
            }
            lines.add("\n\n")
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (player.ticksExisted % 200 == 0) write()
        }
    }

    private fun write() {
        defaultScope.launch(Dispatchers.IO) {
            try {
                file.bufferedWriter().use {
                    lines.forEach(it::write)
                }
            } catch (e: Exception) {
                KamiMod.LOG.error("$chatName Error saving!", e)
            }

            lines.clear()
        }
    }

    private fun read() {
        defaultScope.launch(Dispatchers.IO) {
            lines.clear()

            try {
                file.bufferedReader().forEachLine {
                    lines.add(it)
                }
            } catch (e: Exception) {
                KamiMod.LOG.error("$chatName Error loading!", e)
            }
        }
    }

    init {
        clear.consumers.add { _, input ->
            if (input) {
                lines.clear()
                write()
                MessageSendHelper.sendChatMessage("$chatName Packet log cleared!")
                false
            } else {
                input
            }
        }
    }
}