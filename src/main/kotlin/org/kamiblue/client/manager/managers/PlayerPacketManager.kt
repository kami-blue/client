package org.kamiblue.client.manager.managers

import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.Phase
import org.kamiblue.client.event.events.OnUpdateWalkingPlayerEvent
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.event.events.RenderEntityEvent
import org.kamiblue.client.manager.Manager
import org.kamiblue.client.mixin.*
import org.kamiblue.client.mixin.client.accessor.*
import org.kamiblue.client.mixin.client.accessor.network.*
import org.kamiblue.client.mixin.extension.*
import org.kamiblue.client.module.AbstractModule
import org.kamiblue.client.util.Wrapper
import org.kamiblue.client.util.math.Vec2f
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.event.listener.listener
import java.util.*

object PlayerPacketManager : Manager {

    /** TreeMap for all packets to be sent, sorted by their callers' priority */
    private val packetMap = TreeMap<AbstractModule, Packet>(compareByDescending { it.modulePriority })

    var serverSidePosition: Vec3d = Vec3d.ZERO; private set
    var prevServerSidePosition: Vec3d = Vec3d.ZERO; private set

    var serverSideRotation = Vec2f.ZERO; private set
    var prevServerSideRotation = Vec2f.ZERO; private set

    private var clientSidePitch = Vec2f.ZERO

    init {
        listener<OnUpdateWalkingPlayerEvent>(Int.MIN_VALUE) {
            if (it.phase != Phase.PERI || packetMap.isEmpty()) return@listener

            it.apply(packetMap.values.first())
            packetMap.clear()
        }

        listener<PacketEvent.PostSend>(-6969) {
            if (it.cancelled || it.packet !is CPacketPlayer) return@listener

            if (it.packet.moving) {
                serverSidePosition = Vec3d(it.packet.x, it.packet.y, it.packet.z)
            }

            if (it.packet.rotating) {
                serverSideRotation = Vec2f(it.packet.yaw, it.packet.pitch)
                Wrapper.player?.let { player -> player.rotationYawHead = it.packet.yaw }
            }
        }

        safeListener<TickEvent.ClientTickEvent>(0x2269420) {
            if (it.phase != TickEvent.Phase.START) return@safeListener
            prevServerSidePosition = serverSidePosition
            prevServerSideRotation = serverSideRotation
        }

        listener<RenderEntityEvent.All> {
            if (it.entity != Wrapper.player || it.entity.isRiding) return@listener

            when (it.phase) {
                Phase.PRE -> {
                    with(it.entity) {
                        clientSidePitch = Vec2f(prevRotationPitch, rotationPitch)
                        prevRotationPitch = prevServerSideRotation.y
                        rotationPitch = serverSideRotation.y
                    }
                }
                Phase.POST -> {
                    with(it.entity) {
                        prevRotationPitch = clientSidePitch.x
                        rotationPitch = clientSidePitch.y
                    }
                }
                else -> {
                    // Ignored
                }
            }
        }
    }

    inline fun AbstractModule.sendPlayerPacket(block: Packet.Builder.() -> Unit) {
        Packet.Builder().apply(block).build()?.let {
            sendPlayerPacket(it)
        }
    }

    fun AbstractModule.sendPlayerPacket(packet: Packet) {
        packetMap[this] = packet
    }

    class Packet private constructor(
        val moving: Boolean?,
        val rotating: Boolean?,
        val position: Vec3d?,
        val rotation: Vec2f?,
        val cancelAll: Boolean
    ) {
        class Builder {
            private var position: Vec3d? = null
            private var moving: Boolean? = null

            private var rotation: Vec2f? = null
            private var rotating: Boolean? = null

            private var cancelAll = false
            private var empty = true

            fun move(position: Vec3d) {
                this.position = position
                this.moving = true
                this.empty = false
            }

            fun rotate(rotation: Vec2f) {
                this.rotation = rotation
                this.rotating = true
                this.empty = false
            }

            fun cancelAll() {
                this.cancelAll = true
                this.empty = false
            }

            fun cancelMove() {
                this.position = null
                this.moving = false
                this.empty = false
            }

            fun cancelRotate() {
                this.rotation = null
                this.rotating = false
                this.empty = false
            }

            fun build() =
                if (!empty) Packet(moving, rotating, position, rotation, cancelAll) else null
        }
    }
}