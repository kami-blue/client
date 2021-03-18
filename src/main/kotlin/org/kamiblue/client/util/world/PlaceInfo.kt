package org.kamiblue.client.util.world

import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class PlaceInfo(
    val pos: BlockPos,
    val side: EnumFacing,
    val dist: Double,
    val hitVecOffset: Vec3d,
    val hitVec: Vec3d,
    val placedPos: BlockPos
)