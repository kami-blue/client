package me.zeroeightsix.kami.module.modules.player

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.PlayerMoveEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.client.CPacketInput
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.network.play.client.CPacketVehicleMove
import net.minecraftforge.client.event.PlayerSPPushOutOfBlocksEvent

/**
 * Created by 086 on 22/12/2017.
 */
@Module.Info(
        name = "Freecam",
        category = Module.Category.PLAYER,
        description = "Leave your body and transcend into the realm of the gods"
)
class Freecam : Module() {
    private val speed = register(Settings.i("Speed", 5)) // /100 in practice
    private val packetCancel = register(Settings.b("Packet Cancel", false))

    private var posX = 0.0
    private var posY = 0.0
    private var posZ = 0.0
    private var pitch = 0f
    private var yaw = 0f
    private var clonedPlayer: EntityOtherPlayerMP? = null
    private var isRidingEntity = false
    private var ridingEntity: Entity? = null

    override fun onEnable() {
        if (mc.player != null) {
            isRidingEntity = mc.player.getRidingEntity() != null
            if (mc.player.getRidingEntity() == null) {
                posX = mc.player.posX
                posY = mc.player.posY
                posZ = mc.player.posZ
            } else {
                ridingEntity = mc.player.getRidingEntity()
                mc.player.dismountRidingEntity()
            }

            pitch = mc.player.rotationPitch
            yaw = mc.player.rotationYaw
            clonedPlayer = EntityOtherPlayerMP(mc.world, mc.getSession().profile)
            clonedPlayer!!.copyLocationAndAnglesFrom(mc.player)
            clonedPlayer!!.rotationYawHead = mc.player.rotationYawHead

            mc.world.addEntityToWorld(-100, clonedPlayer)
            mc.player.capabilities.isFlying = true
            mc.player.capabilities.flySpeed = speed.value / 100f
            mc.player.noClip = true

            // WebringOfTheDamned
            // This is needed for some reason, as is the converse in onDisable.
            mc.renderChunksMany = false
            mc.renderGlobal.loadRenderers()
        }
    }

    override fun onDisable() {
        val localPlayer: EntityPlayer? = mc.player
        if (localPlayer != null) {
            mc.player.setPositionAndRotation(posX, posY, posZ, yaw, pitch)
            mc.world.removeEntityFromWorld(-100)

            clonedPlayer = null
            posZ = 0.0
            posY = posZ
            posX = posY
            yaw = 0f
            pitch = yaw

            mc.player.capabilities.isFlying = false //getModManager().getMod("ElytraFlight").isEnabled();
            mc.player.capabilities.flySpeed = 0.05f
            mc.player.noClip = false
            mc.player.motionZ = 0.0
            mc.player.motionY = mc.player.motionZ
            mc.player.motionX = mc.player.motionY

            if (isRidingEntity) {
                mc.player.startRiding(ridingEntity, true)
            }

            // WebringOfTheDamned
            // This is needed for some reason, as is the converse in onEnable.
            mc.renderChunksMany = true
            mc.renderGlobal.loadRenderers()
        }
    }

    override fun onUpdate() {
        mc.player.capabilities.isFlying = true
        mc.player.capabilities.flySpeed = speed.value / 100f
        mc.player.noClip = true
        mc.player.onGround = false
        mc.player.fallDistance = 0f
    }

    @EventHandler
    private val moveListener = Listener(EventHook { event: PlayerMoveEvent? -> mc.player.noClip = true })

    @EventHandler
    private val pushListener = Listener(EventHook { event: PlayerSPPushOutOfBlocksEvent -> event.isCanceled = true })

    @EventHandler
    private val sendListener = Listener(EventHook { event: PacketEvent.Send ->
        if (event.packet is CPacketPlayer || event.packet is CPacketInput) {
            event.cancel()
        }
        if (packetCancel.value && (event.packet is CPacketUseEntity || event.packet is CPacketVehicleMove)) {
            event.cancel()
        }
    })
}