package org.kamiblue.client.module.modules.movement

import org.kamiblue.client.event.events.AddCollisionBoxToListEvent
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.mixin.extension.y
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.BaritoneUtils
import org.kamiblue.client.util.EntityUtils
import org.kamiblue.client.util.threads.safeListener
import net.minecraft.block.BlockLiquid
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityBoat
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.commons.extension.ceilToInt
import org.kamiblue.commons.extension.floorToInt
import org.kamiblue.event.listener.listener

internal object Jesus : Module(
    name = "Jesus",
    description = "Allows you to walk on water",
    category = Category.MOVEMENT
) {

    private val dolphin = setting("Dolphin", false)

    private val WATER_WALK_AA = AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.99, 1.0)

    init {
        onToggle {
            BaritoneUtils.settings?.assumeWalkOnWater?.value = isEnabled
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (isInWater(player) && !player.isSneaking) {
                if (dolphin.value) {
                    player.motionY += 0.03999999910593033 // regular jump speed
                } else {
                    player.motionY = 0.1

                    if (player.ridingEntity != null && player.ridingEntity !is EntityBoat) {
                        player.ridingEntity?.motionY = 0.3
                    }
                }
            }
        }

        listener<AddCollisionBoxToListEvent> {
            if (dolphin.value || mc.player == null || mc.player.isSneaking || mc.player.fallDistance > 3) return@listener

            if (
                it.block is BlockLiquid
                && it.entity is EntityBoat
                && isAboveBlock(mc.player, it.pos)
                && (isDrivenByPlayer(it.entity) || it.entity === mc.player)
                && !isInWater(mc.player)
                && (EntityUtils.isAboveWater(mc.player, false) || EntityUtils.isAboveWater(mc.player.ridingEntity, false))
            ) {
                val axisAlignedBB = WATER_WALK_AA.offset(it.pos)
                if (it.entityBox.intersects(axisAlignedBB)) it.collidingBoxes.add(axisAlignedBB)
                it.cancel()
            }
        }

        listener<PacketEvent.Send> {
            if (dolphin.value) return@listener

            if (it.packet is CPacketPlayer
                && EntityUtils.isAboveWater(mc.player, true)
                && !isInWater(mc.player)
                && !isAboveLand(mc.player)
                && mc.player.ticksExisted % 2 == 0
            ) {
                it.packet.y += 0.02
            }
        }
    }

    private fun isAboveLand(entity: Entity): Boolean {
        val y = entity.posY - 0.01

        for (x in entity.posX.floorToInt() until entity.posX.ceilToInt()) {
            for (z in entity.posZ.floorToInt() until entity.posZ.ceilToInt()) {
                val pos = BlockPos(x, y.floorToInt(), z)
                if (mc.world.getBlockState(pos).isFullBlock) return true
            }
        }

        return false
    }

    private fun isAboveBlock(entity: Entity, pos: BlockPos): Boolean {
        return entity.posY >= pos.y
    }

    private fun isInWater(entity: Entity): Boolean {
        val y = (entity.posY + 0.01).floorToInt()

        for (x in entity.posX.floorToInt() until entity.posX.ceilToInt()) {
            for (z in entity.posZ.floorToInt() until entity.posZ.ceilToInt()) {
                val pos = BlockPos(x, y, z)
                if (mc.world.getBlockState(pos).block is BlockLiquid) return true
            }
        }

        return false
    }

    private fun isDrivenByPlayer(entity: Entity) = mc.player != null && entity == mc.player.ridingEntity
}