package me.zeroeightsix.kami.util.combat

import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.block.Block
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import kotlin.math.floor
import kotlin.math.round

/**
 * @author Xiaro
 *
 * Created by Xiaro on 08/09/20
 */
object SurroundUtils {
    private val mc = Wrapper.minecraft

    @JvmStatic
    val surroundOffset = arrayOf(
        BlockPos(0, -1, 0), // down
        BlockPos(0, 0, -1), // north
        BlockPos(1, 0, 0),  // east
        BlockPos(0, 0, 1),  // south
        BlockPos(-1, 0, 0)  // west
    )

    @JvmStatic
    val surroundOffsetNoFloor = arrayOf(
        BlockPos(0, 0, -1), // north
        BlockPos(1, 0, 0),  // east
        BlockPos(0, 0, 1),  // south
        BlockPos(-1, 0, 0)  // west
    )

    @JvmStatic
    fun checkHole(entity: Entity): HoleType {
        return checkHole(BlockPos(floor(entity.posX).toInt(), floor(entity.posY).toInt(), floor(entity.posZ).toInt()))
    }

    @JvmStatic
    fun checkHole(pos: BlockPos): HoleType {
        // Must be a 1 * 3 * 1 empty space
        if (!mc.world.isAirBlock(pos) || !mc.world.isAirBlock(pos.up()) || !mc.world.isAirBlock(pos.up().up())) return HoleType.NONE

        var type = HoleType.BEDROCK
        for (offset in surroundOffset) {
            val block = mc.world.getBlockState(pos.add(offset)).block
            if (!checkBlock(block)) {
                type = HoleType.NONE
                break
            }
            if (block != Blocks.BEDROCK) type = HoleType.OBBY
        }
        return type
    }

    @JvmStatic
    fun checkBlock(block: Block): Boolean {
        return block == Blocks.BEDROCK || block == Blocks.OBSIDIAN || block == Blocks.ENDER_CHEST || block == Blocks.ANVIL
    }

    @JvmStatic
    fun centerPlayer(tp: Boolean): Boolean {
        val centerDiff = getCenterDiff()
        val centered = isCentered()
        if (!centered) {
            if (tp) {
                val posX = mc.player.posX + MathHelper.clamp(centerDiff.x, -0.2, 0.2)
                val posZ = mc.player.posZ + MathHelper.clamp(centerDiff.z, -0.2, 0.2)
                mc.player.setPosition(posX, mc.player.posY, posZ)
            } else {
                mc.player.motionX = MathHelper.clamp(centerDiff.x / 2.0, -0.2, 0.2)
                mc.player.motionZ = MathHelper.clamp(centerDiff.z / 2.0, -0.2, 0.2)
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