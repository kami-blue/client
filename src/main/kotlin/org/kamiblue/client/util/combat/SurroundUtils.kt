package org.kamiblue.client.util.combat

import net.minecraft.block.Block
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.util.EntityUtils.flooredPosition
import org.kamiblue.client.util.Wrapper
import kotlin.math.round

object SurroundUtils {
    private val mc = Wrapper.minecraft

    val surroundOffset = arrayOf(
        BlockPos(0, -1, 0), // down
        BlockPos(0, 0, -1), // north
        BlockPos(1, 0, 0),  // east
        BlockPos(0, 0, 1),  // south
        BlockPos(-1, 0, 0)  // west
    )

    val surroundOffsetNoFloor = arrayOf(
        BlockPos(0, 0, -1), // north
        BlockPos(1, 0, 0),  // east
        BlockPos(0, 0, 1),  // south
        BlockPos(-1, 0, 0)  // west
    )

    fun SafeClientEvent.checkHole(entity: Entity) =
        checkHole(entity.flooredPosition)

    fun SafeClientEvent.checkHole(pos: BlockPos): HoleType {
        // Must be a 1 * 3 * 1 empty space
        if (!world.isAirBlock(pos) || !world.isAirBlock(pos.up()) || !world.isAirBlock(pos.up().up())) return HoleType.NONE

        var type = HoleType.BEDROCK

        for (offset in surroundOffset) {
            val block = world.getBlockState(pos.add(offset)).block

            if (!checkBlock(block)) {
                type = HoleType.NONE
                break
            }

            if (block != Blocks.BEDROCK) type = HoleType.OBBY
        }

        return type
    }

    fun checkBlock(block: Block): Boolean {
        return block == Blocks.BEDROCK || block == Blocks.OBSIDIAN || block == Blocks.ENDER_CHEST || block == Blocks.ANVIL
    }

    fun centerPlayer(tp: Boolean): Boolean {
        val centerDiff = getCenterDiff()
        val centered = isCentered()
        if (!centered) {
            if (tp) {
                val posX = mc.player.posX + (centerDiff.x).coerceIn(-0.2, 0.2)
                val posZ = mc.player.posZ + (centerDiff.z).coerceIn(-0.2, 0.2)
                mc.player.setPosition(posX, mc.player.posY, posZ)
            } else {
                mc.player.motionX = (centerDiff.x / 2.0).coerceIn(-0.2, 0.2)
                mc.player.motionZ = (centerDiff.z / 2.0).coerceIn(-0.2, 0.2)
            }
        }
        return centered
    }

    private fun isCentered(): Boolean {
        return getCenterDiff().length() < 0.2
    }

    private fun getCenterDiff(): Vec3d {
        return Vec3d(roundToCenter(mc.player.posX), mc.player.posY, roundToCenter(mc.player.posZ)).subtract(mc.player.positionVector)
    }

    private fun roundToCenter(doubleIn: Double): Double {
        return round(doubleIn + 0.5) - 0.5
    }

    enum class HoleType {
        NONE, OBBY, BEDROCK
    }
}