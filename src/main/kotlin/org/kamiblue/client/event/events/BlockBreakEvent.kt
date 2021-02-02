package org.kamiblue.client.event.events

import org.kamiblue.client.event.Event
import net.minecraft.util.math.BlockPos

class BlockBreakEvent(val breakId: Int, val position: BlockPos, val progress: Int) : Event