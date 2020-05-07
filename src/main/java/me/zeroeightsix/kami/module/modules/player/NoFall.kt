package me.zeroeightsix.kami.module.modules.player

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.EntityUtil
import net.minecraft.init.Items
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.EnumHand
import net.minecraft.util.math.RayTraceResult

/**
 * Created by 086 on 19/11/2017.
 * Updated by dominikaaaa on 05/03/20
 */
@Module.Info(
        name = "NoFall",
        category = Module.Category.PLAYER,
        description = "Prevents fall damage"
)
class NoFall : Module() {
    private val fallMode = register(Settings.e<FallMode>("Mode", FallMode.PACKET))
    private val pickup = register(Settings.booleanBuilder("Pickup").withValue(true).withVisibility { fallMode.value == FallMode.BUCKET }.build())
    private val distance = register(Settings.integerBuilder("Distance").withValue(3).withMinimum(1).withMaximum(10).withVisibility { fallMode.value == FallMode.BUCKET }.build())
    private val pickupDelay = register(Settings.integerBuilder("Pickup Delay").withValue(300).withMinimum(100).withMaximum(1000).withVisibility { fallMode.value == FallMode.BUCKET && pickup.value }.build())
    private var last: Long = 0

    @EventHandler
    var sendListener = Listener(EventHook { event: PacketEvent.Send ->
        if (fallMode.value == FallMode.PACKET && event.packet is CPacketPlayer) {
            (event.packet as CPacketPlayer).onGround = true
        }
    })

    override fun onUpdate() {
        if (fallMode.value == FallMode.BUCKET && mc.player.fallDistance >= distance.value && !EntityUtil.isAboveWater(mc.player) && System.currentTimeMillis() - last > 100) {
            val posVec = mc.player.positionVector
            val result = mc.world.rayTraceBlocks(posVec, posVec.add(0.0, -5.33, 0.0), true, true, false)
            if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
                var hand = EnumHand.MAIN_HAND
                if (mc.player.heldItemOffhand.getItem() === Items.WATER_BUCKET) hand = EnumHand.OFF_HAND else if (mc.player.heldItemMainhand.getItem() !== Items.WATER_BUCKET) {
                    for (i in 0..8) if (mc.player.inventory.getStackInSlot(i).getItem() === Items.WATER_BUCKET) {
                        mc.player.inventory.currentItem = i
                        mc.player.rotationPitch = 90f
                        last = System.currentTimeMillis()
                        return
                    }
                    return
                }
                mc.player.rotationPitch = 90f
                mc.playerController.processRightClick(mc.player, mc.world, hand)
            }
            if (pickup.value) {
                Thread(Runnable {
                    try {
                        Thread.sleep(pickupDelay.value.toLong())
                    } catch (ignored: InterruptedException) {
                    }
                    mc.player.rotationPitch = 90f
                    mc.rightClickMouse()
                }).start()
            }
        }
    }

    private enum class FallMode {
        BUCKET, PACKET
    }
}