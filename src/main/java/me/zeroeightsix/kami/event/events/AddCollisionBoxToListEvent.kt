package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.Cancellable
import me.zeroeightsix.kami.event.Event
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class AddCollisionBoxToListEvent(
    val block: Block,
    val state: IBlockState,
    val world: World,
    val pos: BlockPos,
    val entityBox: AxisAlignedBB,
    collidingBoxes: List<AxisAlignedBB>,
    val entity: Entity?
) : Event, Cancellable() {
    val collidingBoxes = ArrayList(collidingBoxes)
}