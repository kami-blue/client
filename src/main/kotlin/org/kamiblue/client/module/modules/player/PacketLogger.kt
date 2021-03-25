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
import org.kamiblue.event.listener.listener
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
    private val captureTiming by setting("Capture Timing", CaptureTiming.POST, description = "Sets point of time for scan event.")
    private val packetSide by setting("Packet Side", PacketSide.BOTH, description = "Log packets from the server, from the client, or both.")
    private val ignoreKeepAlive by setting("Ignore Keep Alive", true, description = "Ignore both incoming and outgoing KeepAlive packets.")
    private val ignoreChunkLoading by setting("Ignore Chunk Loading", true, description = "Ignore chunk loading and unloading packets.")
    private val ignoreUnknown by setting("Ignore Unknown Packets", false, description = "Ignore packets that aren't explicitly handled.")
    private val ignoreChat by setting("Ignore Chat", true, description = "Ignore chat packets.")
    private val ignoreCancelled by setting("Ignore Cancelled", true, description = "Ignore cancelled packets.")

    private val fileTimeFormatter = DateTimeFormatter.ofPattern("HH-mm-ss_SSS")

    private var start = 0L
    private var last = 0L
    private var lastTick = 0L
    private val timer = TickTimer(TimeUnit.SECONDS)

    private const val directory = "${KamiMod.DIRECTORY}packetLogs"
    private var filename = ""
    private var lines = ArrayList<String>()

    private enum class PacketSide(override val displayName: String) : DisplayEnum {
        CLIENT("Client"),
        SERVER("Server"),
        BOTH("Both")
    }

    private enum class CaptureTiming {
        PRE, POST
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
                synchronized(this@PacketLogger) {
                    val current = System.currentTimeMillis()
                    lines.add("Tick Pulse,,${current - start},${current - lastTick}\n")
                    lastTick = current
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

        listener<PacketEvent.Receive>(Int.MAX_VALUE) {
            if (captureTiming != CaptureTiming.PRE || ignoreCancelled && it.cancelled) { 
                return@listener
            }

            receivePacket(it.packet)
        }

        listener<PacketEvent.PostReceive>(Int.MIN_VALUE) {
            if (captureTiming != CaptureTiming.POST || ignoreCancelled && it.cancelled) { 
                return@listener
            }

            receivePacket(it.packet)
        }

        listener<PacketEvent.Send>(Int.MAX_VALUE) {
            if (captureTiming != CaptureTiming.PRE || ignoreCancelled && it.cancelled) {
                return@listener
            }

            sendPacket(it.packet)
        }

        listener<PacketEvent.PostSend>(Int.MIN_VALUE) {
            if (captureTiming != CaptureTiming.POST || ignoreCancelled && it.cancelled) {
                return@listener
            }

            sendPacket(it.packet)
        }
    }

    private fun receivePacket(packet: Packet<*>) {
        if (packetSide == PacketSide.SERVER || packetSide == PacketSide.BOTH) {
            when (packet) {
                is SPacketBlockChange -> {
                    logServer(packet) {
                        "x" to packet.blockPosition.x
                        "y" to packet.blockPosition.y
                        "z" to packet.blockPosition.z
                    }
                }
                is SPacketChat -> {
                    if (!ignoreChat) {
                        logServer(packet) {
                            "unformattedText" to packet.chatComponent.unformattedText
                            "type" to packet.type
                            "itSystem" to packet.isSystem
                        }
                    }
                }
                is SPacketChunkData -> {
                    logServer(packet) {
                        "chunkX" to packet.chunkX
                        "chunkZ" to packet.chunkZ
                        "extractedSize" to packet.extractedSize
                    }
                }
                is SPacketConfirmTransaction -> {
                    logServer(packet) {
                        "windowId" to packet.windowId
                        "transactionID" to packet.actionNumber
                        "accepted" to packet.wasAccepted()
                    }
                }
                is SPacketDestroyEntities -> {
                    logServer(packet) {
                        "entityIDs" to buildString {
                            for (entry in packet.entityIDs) {
                                append("> ")
                                append(entry)
                                append(' ')
                            }
                        }
                    }
                }
                is SPacketEntityMetadata -> {
                    logServer(packet) {
                        "dataEntries" to buildString {
                            for (entry in packet.dataManagerEntries) {
                                append("> isDirty: ")
                                append(entry.isDirty)

                                append(" key: ")
                                append(entry.key)

                                append(" value: ")
                                append(entry.value)

                                append(' ')
                            }
                        }
                    }
                }
                is SPacketEntityProperties -> {
                    logServer(packet) {
                        "entityID" to packet.entityId
                    }
                }
                is SPacketEntityTeleport -> {
                    logServer(packet) {
                        "x" to packet.x
                        "y" to packet.y
                        "z" to packet.z
                        "yaw" to packet.yaw
                        "pitch" to packet.pitch
                        "entityID" to packet.entityId
                    }
                }
                is SPacketKeepAlive -> {
                    if (!ignoreKeepAlive) {
                        logServer(packet) {
                            "id" to packet.id
                        }
                    }
                }
                is SPacketMultiBlockChange -> {
                    logServer(packet) {
                        "changedBlocks" to buildString {
                            for (changedBlock in packet.changedBlocks) {
                                append("> x: ")
                                append(changedBlock.pos.x)

                                append("y: ")
                                append(changedBlock.pos.y)

                                append("z: ")
                                append(changedBlock.pos.z)

                                append(' ')
                            }
                        }
                    }
                }
                is SPacketPlayerPosLook -> {
                    logServer(packet) {
                        "x" to packet.x
                        "y" to packet.y
                        "z" to packet.z
                        "yaw" to packet.yaw
                        "pitch" to packet.pitch
                        "teleportID" to packet.teleportId
                        "flags" to buildString {
                            for (entry in packet.flags) {
                                append("> ")
                                append(entry.name)
                                append(' ')
                            }
                        }
                    }
                }
                is SPacketSoundEffect -> {
                    logServer(packet) {
                        "sound" to packet.sound.soundName
                        "category" to packet.category
                        "posX" to packet.x
                        "posY" to packet.y
                        "posZ" to packet.z
                        "volume" to packet.volume
                        "pitch" to packet.pitch
                    }
                }
                is SPacketSpawnObject -> {
                    logServer(packet) {
                        "entityID" to packet.entityID
                        "data" to packet.data
                    }
                }
                is SPacketTeams -> {
                    logServer(packet) {
                        "action" to packet.action
                        "type" to packet.displayName
                        "itSystem" to packet.color
                    }
                }
                is SPacketTimeUpdate -> {
                    logServer(packet) {
                        "totalWorldTime" to packet.totalWorldTime
                        "worldTime" to packet.worldTime
                    }
                }
                is SPacketUnloadChunk -> {
                    if (!ignoreChunkLoading) {
                        logServer(packet) {
                            "x" to packet.x
                            "z" to packet.z
                        }
                    }
                }
                is SPacketUpdateTileEntity -> {
                    logServer(packet) {
                        "x" to packet.pos.x
                        "y" to packet.pos.y
                        "z" to packet.pos.z
                    }
                }
                is SPacketOpenWindow -> {
                    logServer(packet) {
                        "guiId" to packet.guiId
                        "entityId" to packet.entityId
                        "slotCount" to packet.slotCount
                        "windowId" to packet.windowId
                        "windowTitle" to packet.windowTitle
                    }
                }
                is SPacketWindowItems -> {
                    logServer(packet) {
                        "windowId" to packet.windowId
                        "itemStacks" to buildString {
                            for (entry in packet.itemStacks) {
                                append("> ")
                                append(entry.displayName)
                                append(' ')
                            }
                        }
                    }
                }
                is SPacketSetSlot -> {
                    logServer(packet) {
                        "slot" to packet.slot
                        "stack" to packet.stack.displayName
                        "windowId" to packet.windowId
                    }
                }
                is SPacketEntity.S15PacketEntityRelMove -> {
                    logServer(packet) {
                        "x" to packet.x
                        "y" to packet.y
                        "z" to packet.z
                        "yaw" to packet.yaw
                        "pitch" to packet.pitch
                        "isRotating" to packet.isRotating
                        "onGround" to packet.onGround
                    }
                }
                else -> {
                    if (!ignoreUnknown) {
                        logServer(packet) {
                            + "Not Registered in PacketLogger"
                        }
                    }
                }
            }
        }
    }

    private fun sendPacket(packet: Packet<*>) {
        if (packetSide == PacketSide.CLIENT || packetSide == PacketSide.BOTH) {
            when (packet) {
                is CPacketAnimation -> {
                    logClient(packet) {
                        "hand" to packet.hand
                    }
                }
                is CPacketChatMessage -> {
                    if (!ignoreChat) {
                        logClient(packet) {
                            "message" to packet.message
                        }
                    }
                }
                is CPacketClickWindow -> {
                    logClient(packet) {
                        "windowId" to packet.windowId
                        "slotID" to packet.slotId
                        "mouseButton" to packet.usedButton
                        "clickType" to packet.clickType
                        "transactionID" to packet.actionNumber
                        "clickedItem" to packet.clickedItem
                    }
                }
                is CPacketConfirmTeleport -> {
                    logClient(packet) {
                        "teleportID" to packet.teleportId
                    }
                }
                is CPacketEntityAction -> {
                    logClient(packet) {
                        "action" to packet.action.name
                        "auxData" to packet.auxData
                    }
                }
                is CPacketHeldItemChange -> {
                    logClient(packet) {
                        "slotID" to packet.slotId
                    }
                }
                is CPacketKeepAlive -> {
                    if (!ignoreKeepAlive) {
                        logClient(packet) {
                            "ket" to packet.key
                        }
                    }
                }
                is CPacketPlayer.Rotation -> {
                    logClient(packet) {
                        "yaw" to packet.yaw
                        "pitch" to packet.pitch
                        "onGround" to packet.isOnGround
                    }
                }
                is CPacketPlayer.Position -> {
                    logClient(packet) {
                        "x" to packet.x
                        "y" to packet.y
                        "z" to packet.z
                        "onGround" to packet.isOnGround
                    }
                }
                is CPacketPlayer.PositionRotation -> {
                    logClient(packet) {
                        "x" to packet.x
                        "y" to packet.y
                        "z" to packet.z
                        "yaw" to packet.yaw
                        "pitch" to packet.pitch
                        "onGround" to packet.isOnGround
                    }
                }
                is CPacketPlayer -> {
                    logClient(packet) {
                        "onGround" to packet.isOnGround
                    }
                }
                is CPacketPlayerDigging -> {
                    logClient(packet) {
                        "x" to packet.position.x
                        "y" to packet.position.y
                        "z" to packet.position.z
                        "facing" to packet.facing
                        "action" to packet.action
                    }
                }
                is CPacketPlayerTryUseItem -> {
                    logClient(packet) {
                        "hand" to packet.hand
                    }
                }
                is CPacketPlayerTryUseItemOnBlock -> {
                    logClient(packet) {
                        "x" to packet.pos.x
                        "y" to packet.pos.y
                        "z" to packet.pos.z
                        "side" to packet.direction
                        "hitVecX" to packet.facingX
                        "hitVecY" to packet.facingY
                        "hitVecZ" to packet.facingZ
                    }
                }
                is CPacketUseEntity -> {
                    @Suppress("UNNECESSARY_SAFE_CALL")
                    logClient(packet) {
                        "action" to packet.action
                        "action" to packet.hand
                        "hitVecX" to packet.hitVec?.x
                        "hitVecX" to packet.hitVec?.y
                        "hitVecX" to packet.hitVec?.z
                    }
                }
                is CPacketCloseWindow -> {
                    logClient (packet) {
                        "windowID" to packet.windowID
                    }
                }
                else -> {
                    if (!ignoreUnknown) {
                        logClient(packet) {
                            + "Not Registered in PacketLogger"
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

    private inline fun logClient(packet: Packet<*>, block: PacketLogBuilder.() -> Unit) {
        PacketLogBuilder(PacketSide.CLIENT, packet).apply(block).build()
    }

    private inline fun logServer(packet: Packet<*>, block: PacketLogBuilder.() -> Unit) {
        PacketLogBuilder(PacketSide.SERVER, packet).apply(block).build()
    }

    private class PacketLogBuilder(val side: PacketSide, val packet: Packet<*>) {
        private val stringBuilder = StringBuilder()

        init {
            stringBuilder.apply {
                append(side.displayName)
                append(',')

                append(packet.javaClass.simpleName)
                append(',')

                append(System.currentTimeMillis() - start)
                append(',')

                append(System.currentTimeMillis() - last)
                append(',')
            }
        }

        operator fun String.unaryPlus() {
            stringBuilder.append(this)
        }

        infix fun String.to(value: Any?) {
            if (value != null) {
                add(this, value.toString())
            }
        }

        infix fun String.to(value: String?) {
            if (value != null) {
                add(this, value)
            }
        }

        fun add(key: String, value: String) {
            stringBuilder.apply {
                append(key)
                append(": ")
                append(value)
                append(' ')
            }
        }

        fun build() {
            val string = stringBuilder.run {
                append('\n')
                toString()
            }

            synchronized(PacketLogger) {
                lines.add(string)
                last = System.currentTimeMillis()
            }

            if (logInChat) {
                MessageSendHelper.sendChatMessage(string)
            }
        }
    }
}
