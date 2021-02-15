package org.kamiblue.client.module.modules.player

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.network.Packet
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.*
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.KamiMod
import org.kamiblue.client.event.events.ConnectionEvent
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.mixin.extension.*
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.TickTimer
import org.kamiblue.client.util.TimeUnit
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.threads.defaultScope
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.commons.interfaces.DisplayEnum
import java.io.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

internal object PacketLogger : Module(
    name = "PacketLogger",
    description = "Logs sent packets to a file",
    category = Category.PLAYER
) {
    private val showClientTicks by setting("Show Client Ticks", true, description = "Show timestamps of client ticks.")
    private val logInChat by setting("Log In Chat", false, description = "Print packets in the chat.")
    private val packetSide by setting("Packet Side", PacketSide.BOTH, description = "Log packets from the server, from the client, or both.")
    private val ignoreKeepAlive by setting("Ignore Keep Alive", true, description = "Ignore both incoming and outgoing KeepAlive packets.")
    private val ignoreChunkLoading by setting("Ignore Chunk Loading", true, description = "Ignore chunk loading and unloading packets.")
    private val ignoreUnknown by setting("Ignore Unknown Packets", false, description = "Ignore packets that aren't explicitly handled.")
    private val ignoreChat by setting("Ignore Chat", true, description = "Ignore chat packets.")
    private val ignoreCancelled by setting("Ignore Cancelled", true, description = "Ignore cancelled packets.")

    private val fileTimeFormatter = DateTimeFormatter.ofPattern("HH-mm-ss_SSS")
    private val logTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")

    private var start = 0L
    private var last = 0L
    private val timer = TickTimer(TimeUnit.SECONDS)

    private const val directory = "${KamiMod.DIRECTORY}packetLogs"
    private var filename = ""
    private var lines = ArrayList<String>()

    private enum class PacketSide(override val displayName: String) : DisplayEnum {
        CLIENT("Client"),
        SERVER("Server"),
        BOTH("Both")
    }

    init {
        onEnable {
            start = System.currentTimeMillis()
            filename = "${fileTimeFormatter.format(LocalTime.now())}.csv"

            synchronized(this) {
                lines.add("From,Packet Name,Time Since Start (ms),Time Since Last (ms),Data\n")
            }
        }

        onDisable {
            write()
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.START) return@safeListener

            if (showClientTicks) {
                synchronized(this) {
                    lines.add("Tick Pulse - Realtime: ${logTimeFormatter.format(LocalTime.now())} - Runtime: ${System.currentTimeMillis() - start}ms\n")
                }
            }

            /* Don't let lines get too big, write periodically to the file */
            if (lines.size >= 500 || timer.tick(15L)) {
                write()
            }
        }

        safeListener<ConnectionEvent.Disconnect> {
            disable()
        }

        safeListener<PacketEvent.Receive>(Int.MIN_VALUE) {
            if (ignoreCancelled && it.cancelled) return@safeListener

            if (packetSide == PacketSide.SERVER || packetSide == PacketSide.BOTH) {
                when (it.packet) {
                    is SPacketEntityTeleport -> {
                        add(PacketSide.SERVER, it.packet,
                            "x: ${it.packet.x} " +
                                "y: ${it.packet.y} " +
                                "z: ${it.packet.z} " +
                                "pitch: ${it.packet.pitch} " +
                                "yaw: ${it.packet.yaw} " +
                                "entityID: ${it.packet.entityId}")
                    }
                    is SPacketEntityMetadata -> {
                        val dataEntry = StringBuilder().run {
                            append("dataEntries: ")
                            for (entry in it.packet.dataManagerEntries) {
                                append("> isDirty: ${entry.isDirty} key: ${entry.key} value: ${entry.value} ")
                            }
                            toString()
                        }

                        add(PacketSide.SERVER, it.packet, dataEntry)
                    }
                    is SPacketUnloadChunk -> {
                        if (!ignoreChunkLoading) {
                            add(PacketSide.SERVER, it.packet,
                                "x: ${it.packet.x} " +
                                    "z: ${it.packet.z}")
                        }
                    }
                    is SPacketDestroyEntities -> {
                        val entities = StringBuilder().run {
                            append("entityIDs: ")
                            for (entry in it.packet.entityIDs) {
                                append("> $entry ")
                            }
                            toString()
                        }

                        add(PacketSide.SERVER, it.packet, entities)
                    }
                    is SPacketPlayerPosLook -> {
                        val flags = StringBuilder().run {
                            append("flags: ")
                            for (entry in it.packet.flags) {
                                append("> ${entry.name} ")
                            }
                            toString()
                        }

                        add(PacketSide.SERVER, it.packet,
                            "x: ${it.packet.x} " +
                                "y: ${it.packet.y} " +
                                "z: ${it.packet.z} " +
                                "pitch: ${it.packet.pitch} " +
                                "yaw: ${it.packet.yaw} " +
                                "teleportID: ${it.packet.teleportId}" +
                                flags)

                    }
                    is SPacketBlockChange -> {
                        add(PacketSide.SERVER, it.packet,
                            "x: ${it.packet.blockPosition.x} " +
                                "y: ${it.packet.blockPosition.y} " +
                                "z: ${it.packet.blockPosition.z}")
                    }
                    is SPacketMultiBlockChange -> {
                        val changedBlock = StringBuilder().run {
                            append("changedBlocks: ")
                            for (changedBlock in it.packet.changedBlocks) {
                                append("> x: ${changedBlock.pos.x} y: ${changedBlock.pos.y} z: ${changedBlock.pos.z} ")
                            }
                            toString()
                        }

                        add(PacketSide.SERVER, it.packet, changedBlock)
                    }
                    is SPacketTimeUpdate -> {
                        add(PacketSide.SERVER, it.packet,
                            "totalWorldTime: ${it.packet.totalWorldTime} " +
                                "worldTime: ${it.packet.worldTime}")
                    }
                    is SPacketChat -> {
                        if (!ignoreChat) {
                            add(PacketSide.SERVER, it.packet,
                                "unformattedText: ${it.packet.chatComponent.unformattedText} " +
                                    "type: ${it.packet.type} " +
                                    "isSystem: ${it.packet.isSystem}")
                        }
                    }
                    is SPacketTeams -> {
                        add(PacketSide.SERVER, it.packet,
                            "action: ${it.packet.action} " +
                                "displayName: ${it.packet.displayName} " +
                                "color: ${it.packet.color}")
                    }
                    is SPacketChunkData -> {
                        add(PacketSide.SERVER, it.packet,
                            "chunkX: ${it.packet.chunkX} " +
                                "chunkZ: ${it.packet.chunkZ} " +
                                "extractedSize: ${it.packet.extractedSize}")
                    }
                    is SPacketEntityProperties -> {
                        add(PacketSide.SERVER, it.packet,
                            "entityID: ${it.packet.entityId}")
                    }
                    is SPacketUpdateTileEntity -> {
                        add(PacketSide.SERVER, it.packet,
                            "posX: ${it.packet.pos.x} " +
                                "posY: ${it.packet.pos.y} " +
                                "posZ: ${it.packet.pos.z}")
                    }
                    is SPacketSpawnObject -> {
                        add(PacketSide.SERVER, it.packet,
                            "entityID: ${it.packet.entityID} " +
                                "data: ${it.packet.data}")
                    }
                    is SPacketKeepAlive -> {
                        if (!ignoreKeepAlive) {
                            add(PacketSide.SERVER, it.packet,
                                "id: ${it.packet.id}")
                        }
                    }
                    else -> {
                        if (!ignoreUnknown) {
                            add(PacketSide.SERVER, it.packet, "Not Registered in PacketLogger.kt")
                        }
                    }
                }
            }
        }

        safeListener<PacketEvent.Send>(Int.MIN_VALUE) {
            if (ignoreCancelled && it.cancelled) return@safeListener

            if (packetSide == PacketSide.CLIENT || packetSide == PacketSide.BOTH) {
                when (it.packet) {
                    is CPacketAnimation -> {
                        add(PacketSide.CLIENT, it.packet,
                            "hand: ${it.packet.hand}")
                    }
                    is CPacketPlayer.Rotation -> {
                        add(PacketSide.CLIENT, it.packet,
                            "pitch: ${it.packet.pitch} " +
                                "yaw: ${it.packet.yaw} " +
                                "onGround: ${it.packet.isOnGround}")
                    }
                    is CPacketPlayer.Position -> {
                        add(PacketSide.CLIENT, it.packet,
                            "x: ${it.packet.x} " +
                                "y: ${it.packet.y} " +
                                "z: ${it.packet.z} " +
                                "onGround: ${it.packet.isOnGround}")
                    }
                    is CPacketPlayer.PositionRotation -> {
                        add(PacketSide.CLIENT, it.packet,
                            "x: ${it.packet.x} " +
                                "y: ${it.packet.y} " +
                                "z: ${it.packet.z} " +
                                "pitch: ${it.packet.pitch} " +
                                "yaw: ${it.packet.yaw} " +
                                "onGround: ${it.packet.isOnGround}")
                    }
                    is CPacketPlayerDigging -> {
                        add(PacketSide.CLIENT, it.packet,
                            "positionX: ${it.packet.position.x} " +
                                "positionY: ${it.packet.position.y} " +
                                "positionZ: ${it.packet.position.z} " +
                                "facing: ${it.packet.facing} " +
                                "action: ${it.packet.action} ")
                    }
                    is CPacketEntityAction -> {
                        add(PacketSide.CLIENT, it.packet,
                            "action: ${it.packet.action} " +
                                "auxData: ${it.packet.auxData}")
                    }
                    is CPacketUseEntity -> {
                        add(PacketSide.CLIENT, it.packet,
                            "action: ${it.packet.action} " +
                                "hand: ${it.packet.hand} " +
                                "hitVecX: ${it.packet.hitVec.x} " +
                                "hitVecY: ${it.packet.hitVec.y} " +
                                "hitVecZ: ${it.packet.hitVec.z}")
                    }
                    is CPacketPlayerTryUseItem -> {
                        add(PacketSide.CLIENT, it.packet,
                            "hand: ${it.packet.hand}")
                    }
                    is CPacketConfirmTeleport -> {
                        add(PacketSide.CLIENT, it.packet,
                            "teleportID: ${it.packet.teleportId}")
                    }
                    is CPacketChatMessage -> {
                        if (!ignoreChat) {
                            add(PacketSide.SERVER, it.packet,
                                "message: ${it.packet.message}")
                        }
                    }
                    is CPacketKeepAlive -> {
                        if (!ignoreKeepAlive) {
                            add(PacketSide.CLIENT, it.packet,
                                "key: ${it.packet.key}")
                        }
                    }
                    else -> {
                        if (!ignoreUnknown) {
                            add(PacketSide.CLIENT, it.packet, "Not Registered in PacketLogger.kt")
                        }
                    }
                }
            }
        }
    }

    private fun write() {
        val lines = synchronized(this) {
            val cache = lines
            lines = ArrayList()
            cache
        }

        defaultScope.launch(Dispatchers.IO) {
            try {
                with(File(directory)) {
                    if (!exists()) mkdir()
                }

                FileWriter("$directory/${filename}", true).buffered().use {
                    for (line in lines) it.write(line)
                }
            } catch (e: Exception) {
                KamiMod.LOG.warn("$chatName Failed saving packet log!", e)
            }
        }
    }

    /**
     * Writes a line to the csv following the format:
     * from (client or server), packet name, time since start, time since last packet, packet data
     *
     * @param side The [PacketSide] where this packet is from
     * @param packet Packet to add
     * @param data Data of the [packet] in [String]
     */
    private fun add(side: PacketSide, packet: Packet<*>, data: String) {
        val string = "${side.displayName},${packet.javaClass.simpleName},${System.currentTimeMillis() - start},${System.currentTimeMillis() - last},${data}\n"

        synchronized(this) {
            lines.add(string)
            last = System.currentTimeMillis()
        }

        if (logInChat) {
            MessageSendHelper.sendChatMessage(string)
        }
    }
}
