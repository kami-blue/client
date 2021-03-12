package org.kamiblue.client.util.world

import kotlinx.coroutines.delay
import net.minecraft.init.Blocks
import net.minecraft.item.ItemBlock
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.manager.managers.HotbarManager.serverSideItem
import org.kamiblue.client.manager.managers.PlayerPacketManager
import org.kamiblue.client.util.math.RotationUtils.getRotationTo
import org.kamiblue.client.util.math.VectorUtils.toVec3dCenter
import org.kamiblue.client.util.threads.runSafeSuspend
import java.util.*
import java.util.function.BooleanSupplier
import kotlin.collections.ArrayList

fun SafeClientEvent.getNeighbourSequence(
    pos: BlockPos,
    attempts: Int = 3,
    range: Float = 4.25f,
    visibleSideCheck: Boolean = false,
    sides: Array<EnumFacing> = EnumFacing.values()
) =
    getNeighbourSequence(player.getPositionEyes(1.0f), pos, attempts, range, visibleSideCheck, sides, ArrayList(), HashSet())

private fun SafeClientEvent.getNeighbourSequence(
    eyePos: Vec3d,
    pos: BlockPos,
    attempts: Int,
    range: Float,
    visibleSideCheck: Boolean,
    sides: Array<EnumFacing>,
    sequence: ArrayList<PlaceInfo>,
    toIgnore: HashSet<Pair<BlockPos, EnumFacing>>
): List<PlaceInfo>? {
    for (side in sides) {
        checkNeighbour(eyePos, pos, side, range, visibleSideCheck, true, toIgnore)?.let {
            sequence.add(it)
            sequence.reverse()
            return sequence
        }
    }

    if (attempts > 1) {
        for (side in sides) {
            val newPos = pos.offset(side)

            val placeInfo = checkNeighbour(eyePos, pos, side, range, visibleSideCheck, false, toIgnore) ?: continue
            val newSequence = ArrayList(sequence)
            newSequence.add(placeInfo)

            return getNeighbourSequence(eyePos, newPos, attempts - 1, range, visibleSideCheck, sides, newSequence, toIgnore)
                ?: continue
        }
    }

    return null
}

fun SafeClientEvent.getNeighbour(
    pos: BlockPos,
    attempts: Int = 3,
    range: Float = 4.25f,
    visibleSideCheck: Boolean = false,
    sides: Array<EnumFacing> = EnumFacing.values()
) =
    getNeighbour(player.getPositionEyes(1.0f), pos, attempts, range, visibleSideCheck, sides, HashSet())

private fun SafeClientEvent.getNeighbour(
    eyePos: Vec3d,
    pos: BlockPos,
    attempts: Int,
    range: Float,
    visibleSideCheck: Boolean,
    sides: Array<EnumFacing>,
    toIgnore: HashSet<Pair<BlockPos, EnumFacing>>
): PlaceInfo? {
    for (side in sides) {
        val result = checkNeighbour(eyePos, pos, side, range, visibleSideCheck, true, toIgnore)
        if (result != null) return result
    }

    if (attempts > 1) {
        for (side in sides) {
            val newPos = pos.offset(side)
            if (!world.isPlaceable(newPos)) continue

            return getNeighbour(eyePos, newPos, attempts - 1, range, visibleSideCheck, sides, toIgnore)
                ?: continue
        }
    }

    return null
}

private fun SafeClientEvent.checkNeighbour(
    eyePos: Vec3d,
    pos: BlockPos,
    side: EnumFacing,
    range: Float,
    visibleSideCheck: Boolean,
    checkReplaceable: Boolean,
    toIgnore: HashSet<Pair<BlockPos, EnumFacing>>?
): PlaceInfo? {
    val offsetPos = pos.offset(side)
    val oppositeSide = side.opposite
    
    if (toIgnore?.add(offsetPos to oppositeSide) == false) return null
    
    val hitVec = getHitVec(offsetPos, oppositeSide)
    val dist = eyePos.distanceTo(hitVec)

    if (dist > range) return null
    if (visibleSideCheck && !getVisibleSides(offsetPos, true).contains(oppositeSide)) return null
    if (checkReplaceable && world.getBlockState(offsetPos).isReplaceable) return null
    if (!world.isPlaceable(pos)) return null
    
    val hitVecOffset = getHitVecOffset(oppositeSide)
    return PlaceInfo(offsetPos, oppositeSide, dist, hitVecOffset, hitVec, pos)
}

fun SafeClientEvent.getMiningSide(pos: BlockPos): EnumFacing? {
    val eyePos = player.getPositionEyes(1.0f)

    return getVisibleSides(pos)
        .filter { !world.getBlockState(pos.offset(it)).isFullBox }
        .minByOrNull { eyePos.squareDistanceTo(getHitVec(pos, it)) }
}

fun SafeClientEvent.getClosestVisibleSide(pos: BlockPos): EnumFacing? {
    val eyePos = player.getPositionEyes(1.0f)

    return getVisibleSides(pos)
        .minByOrNull { eyePos.squareDistanceTo(getHitVec(pos, it)) }
}

/**
 * Get the "visible" sides related to player's eye position
 */
fun SafeClientEvent.getVisibleSides(pos: BlockPos, assumeAirAsFullBox: Boolean = false): Set<EnumFacing> {
    val visibleSides = EnumSet.noneOf(EnumFacing::class.java)

    val eyePos = player.getPositionEyes(1.0f)
    val blockCenter = pos.toVec3dCenter()
    val blockState = world.getBlockState(pos)
    val isFullBox = assumeAirAsFullBox && blockState.block == Blocks.AIR || blockState.isFullBox

    return visibleSides
        .checkAxis(eyePos.x - blockCenter.x, EnumFacing.WEST, EnumFacing.EAST, !isFullBox)
        .checkAxis(eyePos.y - blockCenter.y, EnumFacing.DOWN, EnumFacing.UP, true)
        .checkAxis(eyePos.z - blockCenter.z, EnumFacing.NORTH, EnumFacing.SOUTH, !isFullBox)
}

private fun EnumSet<EnumFacing>.checkAxis(diff: Double, negativeSide: EnumFacing, positiveSide: EnumFacing, bothIfInRange: Boolean) =
    this.apply {
        when {
            diff < -0.5 -> {
                add(negativeSide)
            }
            diff > 0.5 -> {
                add(positiveSide)
            }
            else -> {
                if (bothIfInRange) {
                    add(negativeSide)
                    add(positiveSide)
                }
            }
        }
    }

fun getHitVec(pos: BlockPos, facing: EnumFacing): Vec3d {
    val vec = facing.directionVec
    return Vec3d(vec.x * 0.5 + 0.5 + pos.x, vec.y * 0.5 + 0.5 + pos.y, vec.z * 0.5 + 0.5 + pos.z)
}

fun getHitVecOffset(facing: EnumFacing): Vec3d {
    val vec = facing.directionVec
    return Vec3d(vec.x * 0.5 + 0.5, vec.y * 0.5 + 0.5, vec.z * 0.5 + 0.5)
}

suspend fun SafeClientEvent.buildStructure(
    center: BlockPos,
    structureOffset: Array<BlockPos>,
    placeSpeed: Float,
    attempts: Int,
    range: Float,
    visibleSideCheck: Boolean,
    block: BooleanSupplier
) {
    val emptySet = emptySet<BlockPos>()
    val placed = HashSet<BlockPos>()

    var placeCount = 0
    var lastInfo = getStructurePlaceInfo(center, structureOffset, emptySet, attempts, range, visibleSideCheck)

    while (lastInfo != null) {
        if (!block.asBoolean) return

        val placingInfo = getStructurePlaceInfo(center, structureOffset, placed, attempts, range, visibleSideCheck)
            ?: lastInfo

        placeCount++
        placed.add(placingInfo.placedPos)

        runSafeSuspend {
            doPlace(placingInfo, placeSpeed)
        }

        if (placeCount >= 4) {
            delay(100L)
            placeCount = 0
            placed.clear()
        }

        lastInfo = getStructurePlaceInfo(center, structureOffset, emptySet, attempts, range, visibleSideCheck)
    }
}

private fun SafeClientEvent.getStructurePlaceInfo(
    center: BlockPos,
    structureOffset: Array<BlockPos>,
    toIgnore: Set<BlockPos>,
    attempts: Int,
    range: Float,
    visibleSideCheck: Boolean
): PlaceInfo? {
    for (offset in structureOffset) {
        val pos = center.add(offset)
        if (toIgnore.contains(pos)) continue
        if (!world.isPlaceable(pos)) continue

        return getNeighbour(pos, attempts, range, visibleSideCheck) ?: continue
    }

    if (attempts > 1) return getStructurePlaceInfo(center, structureOffset, toIgnore, attempts - 1, range, visibleSideCheck)

    return null
}

/**
 * Placing function for multithreading only
 */
private suspend fun SafeClientEvent.doPlace(
    placeInfo: PlaceInfo,
    placeSpeed: Float
) {
    val rotation = getRotationTo(placeInfo.hitVec)
    val rotationPacket = CPacketPlayer.PositionRotation(player.posX, player.posY, player.posZ, rotation.x, rotation.y, player.onGround)
    val placePacket = placeInfo.toPlacePacket(EnumHand.MAIN_HAND)

    connection.sendPacket(rotationPacket)
    delay((40.0f / placeSpeed).toLong())

    connection.sendPacket(placePacket)
    player.swingArm(EnumHand.MAIN_HAND)
    delay((10.0f / placeSpeed).toLong())
}

/**
 * Placing block without desync
 */
fun SafeClientEvent.placeBlock(
    placeInfo: PlaceInfo,
    hand: EnumHand = EnumHand.MAIN_HAND
) {
    if (!world.isPlaceable(placeInfo.placedPos)) return

    connection.sendPacket(placeInfo.toPlacePacket(hand))
    player.swingArm(hand)

    val itemStack = player.serverSideItem
    val block = (itemStack.item as? ItemBlock?)?.block ?: return
    val metaData = itemStack.metadata
    val blockState = block.getStateForPlacement(world, placeInfo.pos, placeInfo.side, placeInfo.hitVecOffset.x.toFloat(), placeInfo.hitVecOffset.y.toFloat(), placeInfo.hitVecOffset.z.toFloat(), metaData, player, EnumHand.MAIN_HAND)
    val soundType = blockState.block.getSoundType(blockState, world, placeInfo.pos, player)
    world.playSound(player, placeInfo.pos, soundType.placeSound, SoundCategory.BLOCKS, (soundType.getVolume() + 1.0f) / 2.0f, soundType.getPitch() * 0.8f)
}

private fun PlaceInfo.toPlacePacket(hand: EnumHand) =
    CPacketPlayerTryUseItemOnBlock(this.pos, this.side, hand, hitVecOffset.x.toFloat(), hitVecOffset.y.toFloat(), hitVecOffset.z.toFloat())
