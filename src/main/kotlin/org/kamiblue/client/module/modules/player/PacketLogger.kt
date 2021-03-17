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

        listener<PacketEvent.PostReceive>(Int.MIN_VALUE) {
            if (ignoreCancelled && it.cancelled) return@listener

            if (packetSide == PacketSide.SERVER || packetSide == PacketSide.BOTH) {
                when (it.packet) {
                    is SPacketBlockChange -> {
                        log(it) {
                            "x" to it.packet.blockPosition.x
                            "y" to it.packet.blockPosition.y
                            "z" to it.packet.blockPosition.z
                        }
                    }
                    is SPacketChat -> {
                        if (!ignoreChat) {
                            log(it) {
                                "unformattedText" to it.packet.chatComponent.unformattedText
                                "type" to it.packet.type
                                "itSystem" to it.packet.isSystem
                            }
                        }
                    }
                    is SPacketChunkData -> {
                        log(it) {
                            "chunkX" to it.packet.chunkX
                            "chunkZ" to it.packet.chunkZ
                            "extractedSize" to it.packet.extractedSize
                        }
                    }
                    is SPacketConfirmTransaction -> {
                        log(it) {
                            "windowId" to it.packet.windowId
                            "transactionID" to it.packet.actionNumber
                            "accepted" to it.packet.wasAccepted()
                        }
                    }
                    is SPacketDestroyEntities -> {
                        log(it) {
                            "entityIDs" to buildString {
                                for (entry in it.packet.entityIDs) {
                                    append("> ")
                                    append(entry)
                                    append(' ')
                                }
                            }
                        }
                    }
                    is SPacketEntityMetadata -> {
                        log(it) {
                            "dataEntries" to buildString {
                                for (entry in it.packet.dataManagerEntries) {
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
                        log(it) {
                            "entityID" to it.packet.entityId
                        }
                    }
                    is SPacketEntityTeleport -> {
                        log(it) {
                            "x" to it.packet.x
                            "y" to it.packet.y
                            "z" to it.packet.z
                            "yaw" to it.packet.yaw
                            "pitch" to it.packet.pitch
                            "entityID" to it.packet.entityId
                        }
                    }
                    is SPacketKeepAlive -> {
                        if (!ignoreKeepAlive) {
                            log(it) {
                                "id" to it.packet.id
                            }
                        }
                    }
                    is SPacketMultiBlockChange -> {
                        log(it) {
                            "changedBlocks" to buildString {
                                for (changedBlock in it.packet.changedBlocks) {
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
                        log(it) {
                            "x" to it.packet.x
                            "y" to it.packet.y
                            "z" to it.packet.z
                            "yaw" to it.packet.yaw
                            "pitch" to it.packet.pitch
                            "teleportID" to it.packet.teleportId
                            "flags" to buildString {
                                for (entry in it.packet.flags) {
                                    append("> ")
                                    append(entry.name)
                                    append(' ')
                                }
                            }
                        }
                    }
                    is SPacketSoundEffect -> {
                        log(it) {
                            "sound" to it.packet.sound.soundName
                            "category" to it.packet.category
                            "posX" to it.packet.x
                            "posY" to it.packet.y
                            "posZ" to it.packet.z
                            "volume" to it.packet.volume
                            "pitch" to it.packet.pitch
                        }
                    }
                    is SPacketSpawnObject -> {
                        log(it) {
                            "entityID" to it.packet.entityID
                            "data" to it.packet.data
                        }
                    }
                    is SPacketTeams -> {
                        log(it) {
                            "action" to it.packet.action
                            "type" to it.packet.displayName
                            "itSystem" to it.packet.color
                        }
                    }
                    is SPacketTimeUpdate -> {
                        log(it) {
                            "totalWorldTime" to it.packet.totalWorldTime
                            "worldTime" to it.packet.worldTime
                        }
                    }
                    is SPacketUnloadChunk -> {
                        if (!ignoreChunkLoading) {
                            log(it) {
                                "x" to it.packet.x
                                "z" to it.packet.z
                            }
                        }
                    }
                    is SPacketUpdateTileEntity -> {
                        log(it) {
                            "x" to it.packet.pos.x
                            "y" to it.packet.pos.y
                            "z" to it.packet.pos.z
                        }
                    }
                    else -> {
                        if (!ignoreUnknown) {
                            log(it) {
                                + "Not Registered in PacketLogger"
                            }
                        }
                    }
                }
            }
        }

        listener<PacketEvent.PostSend>(Int.MIN_VALUE) {
            if (ignoreCancelled && it.cancelled) return@listener

            if (packetSide == PacketSide.CLIENT || packetSide == PacketSide.BOTH) {
                when (it.packet) {
                    is CPacketAnimation -> {
                        log(it) {
                            "hand" to it.packet.hand
                        }
                    }
                    is CPacketChatMessage -> {
                        if (!ignoreChat) {
                            log(it) {
                                "message" to it.packet.message
                            }
                        }
                    }
                    is CPacketClickWindow -> {
                        if (!ignoreChat) {
                            log(it) {
                                "windowId" to it.packet.windowId
                                "slotID" to it.packet.slotId
                                "mouseButton" to it.packet.usedButton
                                "clickType" to it.packet.clickType
                                "transactionID" to it.packet.actionNumber
                                "clickedItem" to it.packet.clickedItem
                            }
                        }
                    }
                    is CPacketConfirmTeleport -> {
                        log(it) {
                            "teleportID" to it.packet.teleportId
                        }
                    }
                    is CPacketEntityAction -> {
                        log(it) {
                            "action" to it.packet.action.name
                            "auxData" to it.packet.auxData
                        }
                    }
                    is CPacketHeldItemChange -> {
                        log(it) {
                            "slotID" to it.packet.slotId
                        }
                    }
                    is CPacketKeepAlive -> {
                        if (!ignoreKeepAlive) {
                            log(it) {
                                "ket" to it.packet.key
                            }
                        }
                    }
                    is CPacketPlayer.Rotation -> {
                        log(it) {
                            "yaw" to it.packet.yaw
                            "pitch" to it.packet.pitch
                            "onGround" to it.packet.isOnGround
                        }
                    }
                    is CPacketPlayer.Position -> {
                        log(it) {
                            "x" to it.packet.x
                            "y" to it.packet.y
                            "z" to it.packet.z
                            "onGround" to it.packet.isOnGround
                        }
                    }
                    is CPacketPlayer.PositionRotation -> {
                        log(it) {
                            "x" to it.packet.x
                            "y" to it.packet.y
                            "z" to it.packet.z
                            "yaw" to it.packet.yaw
                            "pitch" to it.packet.pitch
                            "onGround" to it.packet.isOnGround
                        }
                    }
                    is CPacketPlayer -> {
                        log(it) {
                            "onGround" to it.packet.isOnGround
                        }
                    }
                    is CPacketPlayerDigging -> {
                        log(it) {
                            "x" to it.packet.position.x
                            "y" to it.packet.position.y
                            "z" to it.packet.position.z
                            "facing" to it.packet.facing
                            "action" to it.packet.action
                        }
                    }
                    is CPacketPlayerTryUseItem -> {
                        log(it) {
                            "hand" to it.packet.hand
                        }
                    }
                    is CPacketPlayerTryUseItemOnBlock -> {
                        log(it) {
                            "x" to it.packet.pos.x
                            "y" to it.packet.pos.y
                            "z" to it.packet.pos.z
                            "side" to it.packet.direction
                            "hitVecX" to it.packet.facingX
                            "hitVecY" to it.packet.facingY
                            "hitVecZ" to it.packet.facingZ
                        }
                    }
                    is CPacketUseEntity -> {
                        @Suppress("UNNECESSARY_SAFE_CALL")
                        log(it) {
                            "action" to it.packet.action
                            "action" to it.packet.hand
                            "hitVecX" to it.packet.hitVec?.x
                            "hitVecX" to it.packet.hitVec?.y
                            "hitVecX" to it.packet.hitVec?.z
                        }
                    }
                    else -> {
                        if (!ignoreUnknown) {
                            log(it) {
                                + "Not Registered in PacketLogger"
                            }
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

    private inline fun log(event: PacketEvent.PostSend, block: PacketLogBuilder.() -> Unit) {
        PacketLogBuilder(PacketSide.CLIENT, event.packet).apply(block).build()
    }

    private inline fun log(event: PacketEvent.PostReceive, block: PacketLogBuilder.() -> Unit) {
        PacketLogBuilder(PacketSide.SERVER, event.packet).apply(block).build()
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
