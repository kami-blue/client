package me.zeroeightsix.kami.module.modules.movement

import baritone.api.BaritoneAPI
import com.google.common.collect.Lists
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.LocalPlayerUpdateEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.EntityUtils.getRidingEntity
import me.zeroeightsix.kami.util.PacketHelper
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.Packet
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayer.PositionRotation
import net.minecraft.util.math.AxisAlignedBB
import net.minecraftforge.fml.client.FMLClientHandler
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer
import kotlin.math.max

/**
 * @author dominikaaaa (Mode.VANILLA)
 * @author fr1kin (Mode.PACKET)
 *
 * The packet mode code is licensed under MIT and can be found here:
 * https://github.com/fr1kin/ForgeHax/blob/2011740/src/main/java/com/matt/forgehax/mods/StepMod.java
 */
@Module.Info(
        name = "Step",
        description = "Changes the vanilla behavior for stepping up blocks",
        category = Module.Category.MOVEMENT
)
class Step : Module() {
    private val mode: Setting<Mode> = register(Settings.e("Mode", Mode.PACKET))
    private var baritoneCompat = register(Settings.b("BaritoneCompatibility", true))
    private val speed = register(Settings.integerBuilder("Speed").withMinimum(1).withMaximum(100).withValue(40).withVisibility { mode.value == Mode.VANILLA }.build())
    private val height = register(Settings.floatBuilder("Height").withRange(0.0f, 10.0f).withValue(1.0f).withVisibility { mode.value == Mode.PACKET }.build())
    private val downStep = register(Settings.booleanBuilder("DownStep").withValue(false).build())
    private val entityStep = register(Settings.booleanBuilder("Entities").withValue(true).withVisibility { mode.value == Mode.PACKET }.build())

    private var previousPositionPacket: CPacketPlayer? = null
    private var wasOnGround = false
    private val defaultHeight = 0.6f

    private enum class Mode {
        VANILLA, PACKET

    }

    override fun onToggle() {
        if (mc.player != null && baritoneCompat.value) {
            BaritoneAPI.getSettings().assumeStep.value = isEnabled
        }
    }

    /**
     * Vanilla mode.
     */
    override fun onUpdate() {
        if (mode.value == Mode.VANILLA) {
            if (mc.player.onGround && !mc.player.isOnLadder && !mc.player.isInWater && !mc.player.isInLava) {
                if (mc.player.collidedHorizontally) {
                    mc.player.motionY = speed.value / 100.0
                } else if (downStep.value) {
                    mc.player.motionY = -(speed.value / 100.0)
                }
            }
        }
    }

    /**
     * Disable states to reset whatever was done in Packet mode
     */
    override fun onEnable() {
        if (mc.player != null) {
            wasOnGround = mc.player.onGround
        }
    }

    override fun onDisable() {
        if (mc.player != null) {
            mc.player.stepHeight = defaultHeight
        }
        if (getRidingEntity() != null) {
            getRidingEntity()?.stepHeight = 1f
        }
    }

    /**
     * Everything onwards is Packet mode
     */
    @EventHandler
    var listener = Listener(EventHook { event: LocalPlayerUpdateEvent ->
        if (mc.player == null || mode.value != Mode.PACKET || mc.player.isElytraFlying) return@EventHook
        val player = event.entityLiving as EntityPlayer
        updateStepHeight(player)
        updateUnStep(player)

        if (getRidingEntity() != null) {
            if (entityStep.value) {
                getRidingEntity()?.stepHeight = 256f
            } else {
                getRidingEntity()?.stepHeight = 1f
            }
        }
    })

    @EventHandler
    var packetListener = Listener(EventHook { event: PacketEvent.Send ->
        if (mc.player == null || mode.value != Mode.PACKET || mc.player.isElytraFlying) return@EventHook

        if (event.packet is CPacketPlayer.Position || event.packet is PositionRotation) {
            val packetPlayer = event.packet as CPacketPlayer

            if (previousPositionPacket != null && !PacketHelper.isIgnored(event.packet)) {
                val diffY = packetPlayer.getY(0.0) - previousPositionPacket!!.getY(0.0)

                /**
                 * The Y difference must be between 0.6 and 1.25
                 */
                if (diffY > defaultHeight && diffY <= 1.25) {
                    val sendList: MutableList<Packet<*>> = Lists.newArrayList()

                    /**
                     * Send additional packets to bypass NCP
                     */
                    val x = previousPositionPacket!!.getX(0.0)
                    val y = previousPositionPacket!!.getY(0.0)
                    val z = previousPositionPacket!!.getZ(0.0)
                    sendList.add(CPacketPlayer.Position(x, y + 0.419, z, true))
                    sendList.add(CPacketPlayer.Position(x, y + 0.753, z, true))
                    sendList.add(CPacketPlayer.Position(packetPlayer.getX(0.0), packetPlayer.getY(0.0), packetPlayer.getZ(0.0), packetPlayer.isOnGround))

                    for (toSend in sendList) {
                        PacketHelper.ignore(toSend)
                        FMLClientHandler.instance().clientToServerNetworkManager.sendPacket(toSend)
                    }
                    event.cancel()
                }
            }
            previousPositionPacket = event.packet as CPacketPlayer
        }
    })

    /**
     * Update player step height to the height setting
     */
    private fun updateStepHeight(player: EntityPlayer) {
        player.stepHeight = if (player.onGround) height.value else defaultHeight
    }

    /**
     * Used to finish walking off the edge and actually touch the block. Required on NCP
     */
    private fun updateUnStep(player: EntityPlayer) {
        try {
            if (downStep.value && wasOnGround && !player.onGround && player.motionY <= 0) {
                unStep(player)
            }
        } finally {
            wasOnGround = player.onGround
        }
    }

    /**
     * When talking off of the edge of a block, move yourself downwards
     */
    private fun unStep(player: EntityPlayer) {
        val range = player.entityBoundingBox.expand(0.0, (-height.value).toDouble(), 0.0).contract(0.0, player.height.toDouble(), 0.0)

        if (!player.world.collidesWithAnyBlock(range)) {
            return
        }

        val collisionBoxes = player.world.getCollisionBoxes(player, range)
        val newY = AtomicReference(0.0)

        collisionBoxes.forEach(Consumer { box: AxisAlignedBB -> newY.set(max(newY.get(), box.maxY)) })
        player.setPositionAndUpdate(player.posX, newY.get(), player.posZ)
    }

}