package me.zeroeightsix.kami.module.modules.player

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.mixin.extension.blockHitDelay
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.threads.defaultScope
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener

object FastBreak : Module(
    name = "FastBreak",
    category = Category.PLAYER,
    description = "Breaks block faster and nullifies the break delay"
) {
    private val delay = setting("Delay", 0, 0..5, 1)
    private val packetMine = setting("PacketMine", true)
    private val sneakTrigger = setting("SneakTrigger", true, { packetMine.value })

    init {
        listener<PacketEvent.Send> {
            if (it.packet !is CPacketPlayerDigging || !packetMine.value || !((sneakTrigger.value && mc.player.isSneaking) || !sneakTrigger.value)) return@listener
            val packet = it.packet

            if (packet.action == CPacketPlayerDigging.Action.START_DESTROY_BLOCK) {
                /* Spams stop digging packets so the blocks will actually be mined after the server side breaking animation */
                defaultScope.launch {
                    val startTime = System.currentTimeMillis()
                    while (!mc.world.isAirBlock(packet.position) && System.currentTimeMillis() - startTime < 10000L) { /* Stops running if the block is mined or it took too long */
                        mc.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, packet.position, packet.facing))
                        delay(200L)
                    }
                }
            } else if (packet.action == CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK) {
                it.cancel() /* Cancels aborting packets */
            }
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (delay.value != 5 && playerController.blockHitDelay == 5) playerController.blockHitDelay = delay.value
        }
    }
}