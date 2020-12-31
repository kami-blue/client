package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.util.math.BlockPos

class BlockBreakEvent(val breakId: Int, val position: BlockPos, val progress: Int) : KamiEvent()