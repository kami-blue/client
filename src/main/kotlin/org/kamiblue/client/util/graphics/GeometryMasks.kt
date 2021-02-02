package org.kamiblue.client.util.graphics

import net.minecraft.util.EnumFacing
import java.util.*

object GeometryMasks {
    val FACEMAP = HashMap<EnumFacing, Int>()

    object Quad {
        const val DOWN = 0x01
        const val UP = 0x02
        const val NORTH = 0x04
        const val SOUTH = 0x08
        const val WEST = 0x10
        const val EAST = 0x20
        const val ALL = DOWN or UP or NORTH or SOUTH or WEST or EAST
    }

    object Line {
        const val DOWN_WEST = 0x11
        const val UP_WEST = 0x12
        const val DOWN_EAST = 0x21
        const val UP_EAST = 0x22
        const val DOWN_NORTH = 0x05
        const val UP_NORTH = 0x06
        const val DOWN_SOUTH = 0x09
        const val UP_SOUTH = 0x0A
        const val NORTH_WEST = 0x14
        const val NORTH_EAST = 0x24
        const val SOUTH_WEST = 0x18
        const val SOUTH_EAST = 0x28
        const val ALL = DOWN_WEST or UP_WEST or DOWN_EAST or UP_EAST or DOWN_NORTH or UP_NORTH or DOWN_SOUTH or UP_SOUTH or NORTH_WEST or NORTH_EAST or SOUTH_WEST or SOUTH_EAST
    }

    init {
        FACEMAP[EnumFacing.DOWN] = Quad.DOWN
        FACEMAP[EnumFacing.WEST] = Quad.WEST
        FACEMAP[EnumFacing.NORTH] = Quad.NORTH
        FACEMAP[EnumFacing.SOUTH] = Quad.SOUTH
        FACEMAP[EnumFacing.EAST] = Quad.EAST
        FACEMAP[EnumFacing.UP] = Quad.UP
    }
}