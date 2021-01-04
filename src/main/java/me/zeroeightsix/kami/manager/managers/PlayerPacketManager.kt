package me.zeroeightsix.kami.manager.managers

import me.zeroeightsix.kami.event.Phase
import me.zeroeightsix.kami.event.events.OnUpdateWalkingPlayerEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.RenderEntityEvent
import me.zeroeightsix.kami.manager.Manager
import me.zeroeightsix.kami.mixin.*
import me.zeroeightsix.kami.mixin.client.accessor.*
import me.zeroeightsix.kami.mixin.client.accessor.network.*
import me.zeroeightsix.kami.mixin.extension.*
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.TickTimer
import me.zeroeightsix.kami.util.TimeUnit
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.math.Vec2f
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener
import java.util.*

object PlayerPacketManager : Manager {

    /** TreeMap for all packets to be sent, sorted by their callers' priority */
    private val packetList = TreeMap<Module, PlayerPacket>(compareByDescending { it.modulePriority })

    var serverSidePosition: Vec3d = Vec3d.ZERO; private set
    var prevServerSidePosition: Vec3d = Vec3d.ZERO; private set

    var serverSideRotation = Vec2f.ZERO; private set
    var prevServerSideRotation = Vec2f.ZERO; private set

    var clientSidePitch = Vec2f.ZERO; private set

    var serverSideHotbar = 0; private set
    var lastSwapTime = 0L; private set

    private var spoofingHotbar = false
    private var hotbarResetTimer = TickTimer(TimeUnit.SECONDS)

    init {
        listener<OnUpdateWalkingPlayerEvent> {
            if (it.phase != Phase.PERI) return@listener
            if (packetList.isNotEmpty()) {
                packetList.values.first().apply(it) // Apply the packet from the module that has the highest priority
                packetList.clear()
            }
        }

        listener<PacketEvent.Send>(-69420) {
            if (it.packet is CPacketHeldItemChange && spoofingHotbar && it.packet.slotId != serverSideHotbar) {
                if (hotbarResetTimer.tick(2L)) {
                    spoofingHotbar = false
                } else {
                    it.cancel()
                }
            }
        }

        listener<PacketEvent.PostSend>(-6969) {
            if (it.cancelled) return@listener
            if (it.packet is CPacketPlayer) {
                if (it.packet.moving) {
                    serverSidePosition = Vec3d(it.packet.x, it.packet.y, it.packet.z)
                }
                if (it.packet.rotating) {
                    serverSideRotation = Vec2f(it.packet.yaw, it.packet.pitch)
                    Wrapper.player?.let { player -> player.rotationYawHead = it.packet.yaw }
                }
            }

            if (it.packet is CPacketHeldItemChange) {
                serverSideHotbar = it.packet.slotId
                lastSwapTime = System.currentTimeMillis()
            }
        }

        safeListener<TickEvent.ClientTickEvent>(0x2269420) {
            if (it.phase != TickEvent.Phase.START) return@safeListener
            prevServerSidePosition = serverSidePosition
            prevServerSideRotation = serverSideRotation
        }

        listener<RenderEntityEvent> {
            if (it.entity != Wrapper.player || it.entity.isRiding) return@listener

            if (it.phase == Phase.PRE) {
                with(it.entity) {
                    clientSidePitch = Vec2f(prevRotationPitch, rotationPitch)
                    prevRotationPitch = prevServerSideRotation.y
                    rotationPitch = serverSideRotation.y
                }
            }

            if (it.phase == Phase.POST) {
                with(it.entity) {
                    prevRotationPitch = clientSidePitch.x
                    rotationPitch = clientSidePitch.y
                }
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

    fun getHoldingItemStack(): ItemStack =
        Wrapper.player?.inventory?.mainInventory?.get(serverSideHotbar) ?: ItemStack.EMPTY

    fun spoofHotbar(slot: Int) {
        Wrapper.minecraft.connection?.let {
            if (serverSideHotbar != slot) {
                serverSideHotbar = slot
                it.sendPacket(CPacketHeldItemChange(slot))
                spoofingHotbar = true
            }
            hotbarResetTimer.reset()
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