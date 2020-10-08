package me.zeroeightsix.kami.module.modules.movement

import baritone.api.BaritoneAPI
import me.zeroeightsix.kami.event.events.AddCollisionBoxToListEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.EntityUtils
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.block.BlockLiquid
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityBoat
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper

@Module.Info(
        name = "Jesus",
        description = "Allows you to walk on water",
        category = Module.Category.MOVEMENT
)
object Jesus : Module() {
    private val baritoneCompat = register(Settings.b("BaritoneCompatibility", true))

    override fun onToggle() {
        if (mc.player != null && baritoneCompat.value) {
            BaritoneAPI.getSettings().assumeWalkOnWater.value = isEnabled
        }
    }

    override fun onUpdate(event: SafeTickEvent) {
        if (EntityUtils.isInWater(mc.player) && !mc.player.isSneaking) {
            mc.player.motionY = 0.1
            if (mc.player.getRidingEntity() != null && mc.player.getRidingEntity() !is EntityBoat) {
                mc.player.getRidingEntity()!!.motionY = 0.3
            }
        }
    }

    init {
        listener<AddCollisionBoxToListEvent> {
            if (it.block !is BlockLiquid || it.entity !is EntityBoat || mc.player == null || mc.player.isSneaking || mc.player.fallDistance > 3) return@listener
            if ((EntityUtils.isDrivenByPlayer(it.entity)
                            || it.entity === mc.player)
                    && !EntityUtils.isInWater(mc.player)
                    && (EntityUtils.isAboveWater(mc.player, false)
                            || EntityUtils.isAboveWater(mc.player.getRidingEntity(), false))
                    && isAboveBlock(mc.player, it.pos)) {
                val axisAlignedBB = WATER_WALK_AA.offset(it.pos)
                if (it.entityBox.intersects(axisAlignedBB)) it.collidingBoxes.add(axisAlignedBB)
                it.cancel()
            }
        }

        listener<PacketEvent.Send> {
            if (it.packet is CPacketPlayer) {
                if (EntityUtils.isAboveWater(mc.player, true) && !EntityUtils.isInWater(mc.player) && !isAboveLand(mc.player)) {
                    val ticks = mc.player.ticksExisted % 2
                    if (ticks == 0) it.packet.y += 0.02
                }
            }
        }
    }

    private val WATER_WALK_AA = AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.99, 1.0)

    private fun isAboveLand(entity: Entity): Boolean {
        val y = entity.posY - 0.01
        for (x in MathHelper.floor(entity.posX) until MathHelper.ceil(entity.posX)) for (z in MathHelper.floor(entity.posZ) until MathHelper.ceil(entity.posZ)) {
            val pos = BlockPos(x, MathHelper.floor(y), z)
            if (mc.world.getBlockState(pos).isFullBlock) return true
        }
        return false
    }

    private fun isAboveBlock(entity: Entity, pos: BlockPos): Boolean {
        return entity.posY >= pos.getY()
    }
}