package me.zeroeightsix.kami.manager.mangers

import me.zeroeightsix.kami.event.KamiEvent
import me.zeroeightsix.kami.event.events.OnUpdateWalkingPlayerEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.RenderEntityEvent
import me.zeroeightsix.kami.manager.Manager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.math.Vec2f
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.math.Vec3d
import java.util.*

object PlayerPacketManager : Manager() {

    /** TreeMap for all packets to be sent, sorted by their callers' priority */
    private val packetList = TreeMap<Module, PlayerPacket>(compareByDescending { it.modulePriority })

    var serverSidePosition = Vec3d(0.0, 0.0, 0.0)
        private set
    var prevServerSideRotation = Vec2f(0f, 0f)
        private set
    var serverSideRotation = Vec2f(0f, 0f)
    private var clientSidePitch = Vec2f(0f, 0f)
    var serverSideHotbar = 0
        private set

    private var spoofingHotbar = false
    private var hotbarResetTimer = TimerUtils.TickTimer(TimerUtils.TimeUnit.SECONDS)

    init {
        listener<OnUpdateWalkingPlayerEvent> {
            if (it.era != KamiEvent.Era.PERI) return@listener
            prevServerSideRotation = serverSideRotation
            if (packetList.isNotEmpty()) {
                packetList.values.first().apply(it) // Apply the packet from the module that has the highest priority
                packetList.clear()
            }
        }

        listener<PacketEvent.Send> {
            with(it.packet) {
                if (this is CPacketPlayer) {
                    if (this.moving) serverSidePosition = Vec3d(this.x, this.y, this.z)
                    if (this.rotating) {
                        serverSideRotation = Vec2f(this.yaw, this.pitch)
                        Wrapper.player?.let { player -> player.rotationYawHead = this.yaw }
                    }
                }
                if (this is CPacketHeldItemChange) {
                    if (spoofingHotbar && this.slotId != serverSideHotbar) {
                        if (hotbarResetTimer.tick(1L)) {
                            spoofingHotbar = false
                            serverSideHotbar = this.slotId
                        } else {
                            it.cancel()
                        }
                    } else {
                        serverSideHotbar = this.slotId
                    }
                }
            }
        }

        listener<RenderEntityEvent.Pre> {
            if (it.entity == null || it.entity != Wrapper.player) return@listener
            with(it.entity) {
                clientSidePitch = Vec2f(prevRotationPitch, rotationPitch)
                prevRotationPitch = prevServerSideRotation.y
                rotationPitch = serverSideRotation.y
            }
        }

        listener<RenderEntityEvent.Final> {
            if (it.entity == null || it.entity != Wrapper.player) return@listener
            with(it.entity) {
                prevRotationPitch = clientSidePitch.x
                rotationPitch = clientSidePitch.y
            }
        }
    }

    /**
     * Adds a packet to the packet list
     *
     * @param packet Packet to be added
     */
    fun addPacket(caller: Module, packet: PlayerPacket) {
        if (packet.isEmpty()) return
        packetList[caller] = packet
    }

    fun spoofHotbar(slot: Int) {
        Wrapper.minecraft.connection?.let {
            it.sendPacket(CPacketHeldItemChange(slot))
            serverSideHotbar = slot
            spoofingHotbar = true
        }
    }

    fun resetHotbar() {
        if (!spoofingHotbar) return
        spoofingHotbar = false
        Wrapper.minecraft.connection?.sendPacket(CPacketHeldItemChange(Wrapper.minecraft.playerController?.currentPlayerItem ?: 0))
    }

    /**
     * Used for PlayerPacketManager. All constructor parameters are optional.
     * They are null by default. null values would not be used for modifying
     * the packet
     */
    class PlayerPacket(
            var moving: Boolean? = null,
            var rotating: Boolean? = null,
            var sprinting: Boolean? = null,
            var sneaking: Boolean? = null,
            var onGround: Boolean? = null,
            pos: Vec3d? = null,
            rotation: Vec2f? = null
    ) {
        var pos: Vec3d? = pos
            set(value) {
                moving = true
                field = value
            }

        var rotation: Vec2f? = rotation
            set(value) {
                rotating = true
                field = value
            }

        /**
         * Checks whether this packet contains values
         *
         * @return True if all values in this packet is null
         */
        fun isEmpty(): Boolean {
            return moving == null
                    && rotating == null
                    && sprinting == null
                    && sneaking == null
                    && onGround == null
                    && pos == null
                    && rotation == null
        }

        /**
         * Apply this packet on an [event]
         *
         * @param event Event to apply on
         */
        fun apply(event: OnUpdateWalkingPlayerEvent) {
            if (this.isEmpty()) return
            event.cancel()
            this.moving?.let { event.moving = it }
            this.rotating?.let { event.rotating = it }
            this.sprinting?.let { event.sprinting = it }
            this.sneaking?.let { event.sneaking = it }
            this.onGround?.let { event.onGround = it }
            this.pos?.let { event.pos = it }
            this.rotation?.let { event.rotation = it }
        }

    }
}