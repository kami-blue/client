package me.zeroeightsix.kami.module.modules.movement

import baritone.api.BaritoneAPI
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.KamiEvent
import me.zeroeightsix.kami.event.events.AddCollisionBoxToListEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.player.Freecam
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.EntityUtil
import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.block.BlockLiquid
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityBoat
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper

/**
 * Created by 086 on 11/12/2017.
 */
@Module.Info(
        name = "Jesus",
        description = "Allows you to walk on water",
        category = Module.Category.MOVEMENT
)
class Jesus : Module() {
    private var baritoneCompat = register(Settings.b("BaritoneCompatibility", true))

    override fun onToggle() {
        if (mc.player != null && baritoneCompat.value) {
            BaritoneAPI.getSettings().assumeWalkOnWater.value = isEnabled
        }
    }

    override fun onUpdate() {
        if (!KamiMod.MODULE_MANAGER.isModuleEnabled(Freecam::class.java)) {
            if (EntityUtil.isInWater(mc.player) && !mc.player.isSneaking) {
                mc.player.motionY = 0.1
                if (mc.player.getRidingEntity() != null && mc.player.getRidingEntity() !is EntityBoat) {
                    mc.player.getRidingEntity()!!.motionY = 0.3
                }
            }
        }
    }

    @EventHandler
    private val addCollisionBoxToListEventListener = Listener(EventHook { event: AddCollisionBoxToListEvent ->
        if (mc.player != null && event.block is BlockLiquid
                && (EntityUtil.isDrivenByPlayer(event.entity) || event.entity === mc.player)
                && event.entity !is EntityBoat
                && !mc.player.isSneaking
                && mc.player.fallDistance < 3 && !EntityUtil.isInWater(mc.player)
                && (EntityUtil.isAboveWater(mc.player, false) || EntityUtil.isAboveWater(mc.player.getRidingEntity(), false))
                && isAboveBlock(mc.player, event.pos)) {
            val axisAlignedBB = WATER_WALK_AA.offset(event.pos)
            if (event.entityBox.intersects(axisAlignedBB)) event.collidingBoxes.add(axisAlignedBB)
            event.cancel()
        }
    })

    @EventHandler
    private val packetEventSendListener = Listener(EventHook { event: PacketEvent.Send ->
        if (event.era == KamiEvent.Era.PRE) {
            if (event.packet is CPacketPlayer) {
                if (EntityUtil.isAboveWater(mc.player, true) && !EntityUtil.isInWater(mc.player) && !isAboveLand(mc.player)) {
                    val ticks = mc.player.ticksExisted % 2
                    if (ticks == 0) (event.packet as CPacketPlayer).y += 0.02
                }
            }
        }
    })

    companion object {
        private val WATER_WALK_AA = AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.99, 1.0)

        private fun isAboveLand(entity: Entity?): Boolean {
            if (entity == null) return false
            val y = entity.posY - 0.01
            for (x in MathHelper.floor(entity.posX) until MathHelper.ceil(entity.posX)) for (z in MathHelper.floor(entity.posZ) until MathHelper.ceil(entity.posZ)) {
                val pos = BlockPos(x, MathHelper.floor(y), z)
                if (Wrapper.getWorld().getBlockState(pos).block.isFullBlock(Wrapper.getWorld().getBlockState(pos))) return true
            }
            return false
        }

        private fun isAboveBlock(entity: Entity, pos: BlockPos): Boolean {
            return entity.posY >= pos.getY()
        }
    }
}