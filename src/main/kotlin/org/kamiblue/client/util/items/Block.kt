package org.kamiblue.client.util.items

import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.item.Item
import net.minecraft.util.math.BlockPos
import org.kamiblue.client.util.Wrapper

val Block.item: Item get() = Item.getItemFromBlock(this)

val Block.id: Int get() = Block.getIdFromBlock(this)

val IBlockState.isFullBox: Boolean
    get() = Wrapper.world?.let {
        this.getCollisionBoundingBox(it, BlockPos.ORIGIN)
    } == Block.FULL_BLOCK_AABB