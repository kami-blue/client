package me.zeroeightsix.kami.util.combat

import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.block.Block
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import kotlin.math.floor

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

    enum class HoleType {
        NONE, OBBY, BEDROCK
    }
}