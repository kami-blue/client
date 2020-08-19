package me.zeroeightsix.kami.event.events

import net.minecraft.util.math.BlockPos

/**
 * Updated by Xiaro on 18/08/20
 */
class BlockBreakEvent(val breakId: Int, val position: BlockPos, val progress: Int)