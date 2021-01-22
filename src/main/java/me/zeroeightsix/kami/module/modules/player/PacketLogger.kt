package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.mixin.extension.*
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage
import me.zeroeightsix.kami.util.threads.onMainThread
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.*
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.io.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.ConcurrentModificationException

internal object PacketLogger : Module(
    name = "PacketLogger",
    description = "Logs sent packets to a file",
    category = Category.PLAYER
) {
    private val showClientTicks by setting("ShowClientTicks", true, description = "Show timestamps of client ticks.")
    private val logInChat by setting("LogInChat", false, description = "Print packets in the chat.")
    private val packetType by setting("PacketType", Type.BOTH, description = "Log packets from the server, from the client, or both.")
    private val ignoreKeepAlive by setting("IgnoreKeepAlive", true, description = "Ignore both incoming and outgoing KeepAlive packets.")
    private val ignoreChunkLoading by setting("IgnoreChunkLoading", true, description = "Ignore chunk loading and unloading packets.")
    private val ignoreUnknown by setting("IgnoreUnknownPackets", false, description = "Ignore packets that aren't explicitly handled.")
    private val ignoreChat by setting("IgnoreChat", true, description = "Ignore chat packets.")

    private val fileTimeFormatter = DateTimeFormatter.ofPattern("HH-mm-ss_SSS")
    private val logTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
    private var filename = ""
    private val lines = ArrayList<String>()

    private var start = 0L
    private var last = 0L

    enum class Type {
        CLIENT, SERVER, BOTH
    }

    init {
        onEnable {
            if (mc.player == null) {
                disable()
                return@onEnable
            }
            onMainThread {
                start = System.currentTimeMillis()
            }

            filename = "packet_logger_${fileTimeFormatter.format(LocalTime.now())}.csv"
            lines.add("From,Packet Name,Time Since Start (ms),Time Since Last (ms),Data\n")
        }

        onDisable {
            if (mc.player != null) write()
        }

        safeListener<PacketEvent.Receive> {
            if (packetType == Type.SERVER || packetType == Type.BOTH) {
                when (it.packet) {
                    is SPacketEntityTeleport -> {
                        add(Type.SERVER, it.packet.javaClass.simpleName,
                            "x: ${it.packet.x} " +
                            "y: ${it.packet.y} " +
                            "z: ${it.packet.z} " +
                            "pitch: ${it.packet.pitch} " +
                            "yaw: ${it.packet.yaw} " +
                            "entityId: ${it.packet.entityId}")
                    }
                    is SPacketEntityMetadata -> {
                        var data = "dataEntries: "
                        for (entry in it.packet.dataManagerEntries) {
                            data += "> isDirty: ${entry.isDirty} " +
                                    "key: ${entry.key} " +
                                    "value: ${entry.value} "
                        }
                        add(Type.SERVER, it.packet.javaClass.simpleName, data)
                    }
                    is SPacketUnloadChunk -> {
                        if (!ignoreChunkLoading) {
                            add(Type.SERVER, it.packet.javaClass.simpleName,
                                "x: ${it.packet.x} " +
                                "z: ${it.packet.z}")
                        }
                    }
                    is SPacketDestroyEntities -> {
                        var entries = "entityIDs: "
                        for (entry in it.packet.entityIDs) {
                            entries += "> $entry "
                        }
                        add(Type.SERVER, it.packet.javaClass.simpleName, entries)
                    }
                    is SPacketPlayerPosLook -> {
                        var flags = "flags: "
                        for (entry in it.packet.flags) {
                            flags += "> ${entry.name} "
                        }
                        add(Type.SERVER, it.packet.javaClass.simpleName,
                            "x: ${it.packet.x} " +
                            "y: ${it.packet.y} " +
                            "z: ${it.packet.z} " +
                            "pitch: ${it.packet.pitch} " +
                            "yaw: ${it.packet.yaw} " +
                            "teleportId: ${it.packet.teleportId}")

                    }
                    is SPacketBlockChange -> {
                        add(Type.SERVER, it.packet.javaClass.simpleName,
                            "x: ${it.packet.blockPosition.x} " +
                            "y: ${it.packet.blockPosition.y} " +
                            "z: ${it.packet.blockPosition.z}")
                    }
                    is SPacketMultiBlockChange -> {
                        var changedBlocks = "changedBlocks: "
                        for (changedBlock in it.packet.changedBlocks) {
                            changedBlocks += "> x: ${changedBlock.pos.x} y: ${changedBlock.pos.y} z: ${changedBlock.pos.z} "
                        }
                        add(Type.SERVER, it.packet.javaClass.simpleName, changedBlocks)
                    }
                    is SPacketTimeUpdate -> {
                        add(Type.SERVER, it.packet.javaClass.simpleName,
                            "totalWorldTime: ${it.packet.totalWorldTime} " +
                            "worldTime: ${it.packet.worldTime}")
                    }
                    is SPacketChat -> {
                        if (!ignoreChat) {
                            add(Type.SERVER, it.packet.javaClass.simpleName,
                                "unformattedText: ${it.packet.chatComponent.unformattedText} " +
                                "type: ${it.packet.type} " +
                                "isSystem: ${it.packet.isSystem}")
                        }
                    }
                    is SPacketTeams -> {
                        add(Type.SERVER, it.packet.javaClass.simpleName,
                            "action: ${it.packet.action} " +
                            "displayName: ${it.packet.displayName} " +
                            "color: ${it.packet.color}")
                    }
                    is SPacketChunkData -> {
                        add(Type.SERVER, it.packet.javaClass.simpleName,
                            "chunkX: ${it.packet.chunkX} " +
                            "chunkZ: ${it.packet.chunkZ} " +
                            "extractedSize: ${it.packet.extractedSize}")
                    }
                    is SPacketEntityProperties -> {
                        add(Type.SERVER, it.packet.javaClass.simpleName,
                        "entityId: ${it.packet.entityId}")
                    }
                    is SPacketUpdateTileEntity -> {
                        add(Type.SERVER, it.packet.javaClass.simpleName,
                            "posX: ${it.packet.pos.x} " +
                            "posY: ${it.packet.pos.y} " +
                            "posZ: ${it.packet.pos.z}")
                    }
                    is SPacketSpawnObject -> {
                        add(Type.SERVER, it.packet.javaClass.simpleName,
                            "entityID: ${it.packet.entityID} " +
                            "data: ${it.packet.data}")
                    }
                    is SPacketKeepAlive -> {
                        if (!ignoreKeepAlive) {
                            add(Type.SERVER, it.packet.javaClass.simpleName,
                                "id: ${it.packet.id}")
                        }
                    }
                    else -> {
                        if (!ignoreUnknown) {
                            add(Type.SERVER, it.packet.javaClass.simpleName, "Not Registered in PacketLogger.kt")
                        }
                    }
                }
                if (logInChat) sendChatMessage(lines.joinToString())
            }
        }

        /* Listen to PostSend and not Send since packets can get cancelled before we actually send them */
        safeListener<PacketEvent.PostSend> {
            if (packetType == Type.CLIENT || packetType == Type.BOTH ) {
                when (it.packet) {
                    is CPacketAnimation -> {
                        add(Type.CLIENT, it.packet.javaClass.simpleName,
                            "hand: ${it.packet.hand}")
                    }
                    is CPacketPlayer.Rotation -> {
                        add(Type.CLIENT, it.packet.javaClass.simpleName,
                            "pitch: ${it.packet.pitch} "+
                            "yaw: ${it.packet.yaw} " +
                            "onGround: ${it.packet.isOnGround}")
                    }
                    is CPacketPlayer.Position -> {
                        add(Type.CLIENT, it.packet.javaClass.simpleName,
                            "x: ${it.packet.x} " +
                            "y: ${it.packet.y} " +
                            "z: ${it.packet.z} " +
                            "onGround: ${it.packet.isOnGround}")
                    }
                    is CPacketPlayer.PositionRotation -> {
                        add(Type.CLIENT, it.packet.javaClass.simpleName,
                            "x: ${it.packet.x} " +
                            "y: ${it.packet.y} " +
                            "z: ${it.packet.z} " +
                            "pitch: ${it.packet.pitch} " +
                            "yaw: ${it.packet.yaw} " +
                            "onGround: ${it.packet.isOnGround}")
                    }
                    is CPacketPlayerDigging -> {
                        add(Type.CLIENT, it.packet.javaClass.simpleName,
                            "positionX: ${it.packet.position.x} " +
                            "positionY: ${it.packet.position.y} " +
                            "positionZ: ${it.packet.position.z} " +
                            "facing: ${it.packet.facing} " +
                            "action: ${it.packet.action} ")
                    }
                    is CPacketEntityAction -> {
                        add(Type.CLIENT, it.packet.javaClass.simpleName,
                            "action: ${it.packet.action} " +
                            "auxData: ${it.packet.auxData}")
                    }
                    is CPacketUseEntity -> {
                        add(Type.CLIENT, it.packet.javaClass.simpleName,
                            "action: ${it.packet.action} " +
                            "hand: ${it.packet.hand} " +
                            "hitVecX: ${it.packet.hitVec.x} " +
                            "hitVecY: ${it.packet.hitVec.y} " +
                            "hitVecZ: ${it.packet.hitVec.z}")
                    }
                    is CPacketPlayerTryUseItem -> {
                        add(Type.CLIENT, it.packet.javaClass.simpleName,
                            "hand: ${it.packet.hand}")
                    }
                    is CPacketConfirmTeleport -> {
                        add(Type.CLIENT, it.packet.javaClass.simpleName,
                            "teleportId: ${it.packet.teleportId}")
                    }
                    is CPacketChatMessage -> {
                        if (!ignoreChat) {
                            add(Type.SERVER, it.packet.javaClass.simpleName,
                                "message: ${it.packet.message}")
                        }
                    }
                    is CPacketKeepAlive -> {
                        if (!ignoreKeepAlive) {
                            add(Type.CLIENT, it.packet.javaClass.simpleName,
                                "key: ${it.packet.key}")
                        }
                    }
                    else -> {
                        if (!ignoreUnknown) {
                            add(Type.CLIENT, it.packet.javaClass.simpleName, "Not Registered in PacketLogger.kt")
                        }
                    }
                }
                if (logInChat) sendChatMessage(lines.joinToString())
            }
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (showClientTicks) {
                if (it.phase == TickEvent.Phase.START) lines.add("Tick Pulse - Realtime: ${logTimeFormatter.format(LocalTime.now())} - Runtime: ${System.currentTimeMillis() - start}ms\n")
                /* Don't let lines get too big, write periodically to the file */
                if (player.ticksExisted % 200 == 0 || lines.size >= 1000) write()
            }
        }

        safeListener<ConnectionEvent.Disconnect> {
            disable()
        }
    }

    private fun write() {
        try {
            FileWriter(filename, true).also {
                for (line in lines) it.write(line)
                it.close()
            }
        } catch (e: Exception) {
            when (e) {
                is IOException,
                is ConcurrentModificationException -> {
                    KamiMod.LOG.error("$chatName Error saving!")
                    e.printStackTrace()
                }
                else -> throw e
            }
        }
        lines.clear()
    }

    /**
     * Writes a line to the csv following the format:
     * from (client or server), packet name, time since start, time since last packet, packet data
     */
    private fun add(from: Type, packetName: String, data: String) {
        lines.add("${from.name},$packetName,${System.currentTimeMillis() - start},${System.currentTimeMillis() - last},$data")
        lines.add("\n")
        last = System.currentTimeMillis()
    }
}
