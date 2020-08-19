package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/**
 * Created by 086 on 11/12/2017.
 * Updated by Xiaro on 18/08/20
 */
class AddCollisionBoxToListEvent(
        val block: Block,
        val state: IBlockState,
        val world: World,
        val pos: BlockPos,
        val entityBox: AxisAlignedBB,
        collidingBoxes: List<AxisAlignedBB>,
        val entity: Entity?,
        val isBool: Boolean
) : KamiEvent() {
    val collidingBoxes = ArrayList(collidingBoxes)
}