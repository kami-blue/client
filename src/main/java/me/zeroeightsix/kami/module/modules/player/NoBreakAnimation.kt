package me.zeroeightsix.kami.module.modules.player

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos

@Module.Info(
        name = "NoBreakAnimation",
        category = Module.Category.PLAYER,
        description = "Prevents block break animation server side"
)
object NoBreakAnimation : Module() {
    private var isMining = false
    private var lastPos: BlockPos? = null
    private var lastFacing: EnumFacing? = null

    @EventHandler
    private val listener = Listener(EventHook { event: PacketEvent.Send ->
        if (event.packet is CPacketPlayerDigging) {
            val cPacketPlayerDigging = event.packet
            // skip crystals and living entities
            for (entity in mc.world.getEntitiesWithinAABBExcludingEntity(null, AxisAlignedBB(cPacketPlayerDigging.position))) {
                if (entity is EntityEnderCrystal) {
                    resetMining()
                    return@EventHook
                }
                if (entity is EntityLivingBase) {
                    resetMining()
                    return@EventHook
                }
            }
            if (cPacketPlayerDigging.action == CPacketPlayerDigging.Action.START_DESTROY_BLOCK) {
                isMining = true
                setMiningInfo(cPacketPlayerDigging.position, cPacketPlayerDigging.facing)
            }
            if (cPacketPlayerDigging.action == CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK) {
                resetMining()
            }
        }
    })

    override fun onUpdate() {
        if (!mc.gameSettings.keyBindAttack.isKeyDown) {
            resetMining()
            return
        }
        if (isMining && lastPos != null && lastFacing != null) {
            mc.player.connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, lastPos, lastFacing))
        }
    }

    private fun setMiningInfo(lastPos: BlockPos, lastFacing: EnumFacing) {
        this.lastPos = lastPos
        this.lastFacing = lastFacing
    }

    fun resetMining() {
        isMining = false
        lastPos = null
        lastFacing = null
    }
}