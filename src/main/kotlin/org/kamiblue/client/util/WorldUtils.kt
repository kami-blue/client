package org.kamiblue.client.util

import kotlinx.coroutines.delay
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.manager.managers.PlayerPacketManager
import org.kamiblue.client.util.math.RotationUtils.getRotationTo
import org.kamiblue.client.util.math.corners
import org.kamiblue.client.util.threads.runSafeSuspend
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.item.ItemBlock
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import org.kamiblue.commons.extension.add
import kotlin.math.floor

object WorldUtils {
    val blackList = linkedSetOf(
        Blocks.ENDER_CHEST,
        Blocks.CHEST,
        Blocks.TRAPPED_CHEST,
        Blocks.CRAFTING_TABLE,
        Blocks.ANVIL,
        Blocks.BREWING_STAND,
        Blocks.HOPPER,
        Blocks.DROPPER,
        Blocks.DISPENSER,
        Blocks.TRAPDOOR,
        Blocks.ENCHANTING_TABLE
    )

    val shulkerList = linkedSetOf(
        Blocks.WHITE_SHULKER_BOX,
        Blocks.ORANGE_SHULKER_BOX,
        Blocks.MAGENTA_SHULKER_BOX,
        Blocks.LIGHT_BLUE_SHULKER_BOX,
        Blocks.YELLOW_SHULKER_BOX,
        Blocks.LIME_SHULKER_BOX,
        Blocks.PINK_SHULKER_BOX,
        Blocks.GRAY_SHULKER_BOX,
        Blocks.SILVER_SHULKER_BOX,
        Blocks.CYAN_SHULKER_BOX,
        Blocks.PURPLE_SHULKER_BOX,
        Blocks.BLUE_SHULKER_BOX,
        Blocks.BROWN_SHULKER_BOX,
        Blocks.GREEN_SHULKER_BOX,
        Blocks.RED_SHULKER_BOX,
        Blocks.BLACK_SHULKER_BOX
    )

    fun SafeClientEvent.isLiquidBelow(entity: Entity = player): Boolean {
        val results = rayTraceBoundingBoxToGround(entity, true)
        if (results.all { it.typeOfHit == RayTraceResult.Type.MISS || it.hitVec?.y ?: 911.0 < 0.0 }) {
            return true
        }

        val pos = results.maxByOrNull { it.hitVec?.y ?: -69420.0 }?.blockPos ?: return false
        return isLiquid(pos)
    }

    fun SafeClientEvent.getGroundPos(entity: Entity = player): Vec3d {
        val results = rayTraceBoundingBoxToGround(entity, false)
        if (results.all { it.typeOfHit == RayTraceResult.Type.MISS || it.hitVec?.y ?: 911.0 < 0.0 }) {
            return Vec3d(0.0, -999.0, 0.0)
        }

        return results.maxByOrNull { it.hitVec?.y ?: -69420.0 }?.hitVec ?: Vec3d(0.0, -69420.0, 0.0)
    }

    private fun SafeClientEvent.rayTraceBoundingBoxToGround(entity: Entity, stopOnLiquid: Boolean): List<RayTraceResult> {
        val boundingBox = entity.entityBoundingBox
        val xArray = arrayOf(floor(boundingBox.minX), floor(boundingBox.maxX))
        val zArray = arrayOf(floor(boundingBox.minZ), floor(boundingBox.maxZ))

        val results = ArrayList<RayTraceResult>(4)

        for (x in xArray) {
            for (z in zArray) {
                val result = rayTraceToGround(Vec3d(x, boundingBox.minY, z), stopOnLiquid)
                results.add(result)
            }
        }

        return results
    }

    private fun SafeClientEvent.rayTraceToGround(vec3d: Vec3d, stopOnLiquid: Boolean): RayTraceResult? {
        return world.rayTraceBlocks(vec3d, Vec3d(vec3d.x, -1.0, vec3d.z), stopOnLiquid, true, false)
    }

    fun SafeClientEvent.isLiquid(pos: BlockPos): Boolean {
        return world.getBlockState(pos).material.isLiquid
    }

    fun SafeClientEvent.isWater(pos: BlockPos): Boolean {
        return world.getBlockState(pos).block == Blocks.WATER
    }

    /**
     * Checks if given [pos] is able to place block in it
     *
     * @return true playing is not colliding with [pos] and there is block below it
     */
    fun SafeClientEvent.isPlaceable(pos: BlockPos, ignoreSelfCollide: Boolean = false) = world.getBlockState(pos).material.isReplaceable
        && world.checkNoEntityCollision(AxisAlignedBB(pos), if (ignoreSelfCollide) player else null)

    fun SafeClientEvent.getHitSide(blockPos: BlockPos): EnumFacing {
        return rayTraceTo(blockPos)?.sideHit ?: EnumFacing.UP
    }

    fun SafeClientEvent.rayTraceTo(blockPos: BlockPos): RayTraceResult? {
        return world.rayTraceBlocks(player.getPositionEyes(1f), Vec3d(blockPos).add(0.5, 0.5, 0.5))
    }

    fun getHitVec(pos: BlockPos, facing: EnumFacing): Vec3d {
        val vec = facing.directionVec
        return Vec3d(vec.x * 0.5 + 0.5 + pos.x, vec.y * 0.5 + 0.5 + pos.y, vec.z * 0.5 + 0.5 + pos.z)
    }

    fun getHitVecOffset(facing: EnumFacing): Vec3d {
        val vec = facing.directionVec
        return Vec3d(vec.x * 0.5 + 0.5, vec.y * 0.5 + 0.5, vec.z * 0.5 + 0.5)
    }

    fun SafeClientEvent.rayTraceHitVec(pos: BlockPos): RayTraceResult? {
        val eyePos = player.getPositionEyes(1f)
        val bb = world.getBlockState(pos).getSelectedBoundingBox(world, pos)

        return world.rayTraceBlocks(eyePos, bb.center, false, false, true)?.takeIf {
            it.isEqualTo(pos)
        } ?: bb.corners(0.95).mapNotNull { corner ->
            world.rayTraceBlocks(eyePos, corner, false, false, true)?.takeIf { it.isEqualTo(pos) }
        }.minByOrNull {
            it.hitVec?.distanceTo(eyePos) ?: 69420.0
        }
    }

    private fun RayTraceResult.isEqualTo(pos: BlockPos) = typeOfHit == RayTraceResult.Type.BLOCK && blockPos == pos

    suspend fun SafeClientEvent.buildStructure(
        placeSpeed: Float,
        getPlaceInfo: SafeClientEvent.(HashSet<BlockPos>) -> Pair<EnumFacing, BlockPos>?
    ) {
        val emptyHashSet = HashSet<BlockPos>()
        val placed = HashSet<BlockPos>()
        var placeCount = 0
        while (getPlaceInfo(emptyHashSet) != null) {
            val placingInfo = getPlaceInfo(placed) ?: getPlaceInfo(emptyHashSet) ?: break
            placeCount++
            placed.add(placingInfo.second.offset(placingInfo.first))
            runSafeSuspend {
                doPlace(placingInfo.second, placingInfo.first, placeSpeed)
            }
            if (placeCount >= 4) {
                delay(100L)
                placeCount = 0
                placed.clear()
            }
        }
    }

    fun SafeClientEvent.hasNeighbour(blockPos: BlockPos): Boolean {
        return EnumFacing.values().any {
            !world.getBlockState(blockPos.offset(it)).material.isReplaceable
        }
    }

    fun SafeClientEvent.getPlaceInfo(
        center: BlockPos?,
        structureOffset: Array<BlockPos>,
        toIgnore: HashSet<BlockPos>,
        maxAttempts: Int,
        attempts: Int = 1
    ): Pair<EnumFacing, BlockPos>? {
        center?.let {
            for (offset in structureOffset) {
                val pos = it.add(offset)
                if (toIgnore.contains(pos)) continue
                if (!isPlaceable(pos)) continue
                return getNeighbour(pos, attempts) ?: continue
            }
            if (attempts <= maxAttempts) return getPlaceInfo(it, structureOffset, toIgnore, maxAttempts, attempts + 1)
        }
        return null
    }

    fun SafeClientEvent.getNeighbour(
        blockPos: BlockPos,
        attempts: Int = 3,
        range: Float = 4.25f,
        sides: Array<EnumFacing> = EnumFacing.values(),
        toIgnore: HashSet<BlockPos> = HashSet()
    ): Pair<EnumFacing, BlockPos>? {
        for (side in sides) {
            val pos = blockPos.offset(side)
            if (!toIgnore.add(pos)) continue
            if (world.getBlockState(pos).material.isReplaceable) continue
            if (player.getPositionEyes(1f).distanceTo(Vec3d(pos).add(getHitVecOffset(side))) > range) continue
            return Pair(side.opposite, pos)
        }
        if (attempts > 1) {
            toIgnore.add(blockPos)
            for (side in sides) {
                val pos = blockPos.offset(side)
                if (!isPlaceable(pos)) continue
                return getNeighbour(pos, attempts - 1, range, sides, toIgnore) ?: continue
            }
        }
        return null
    }

    /**
     * Placing function for multithreading only
     */
    suspend fun SafeClientEvent.doPlace(
        pos: BlockPos,
        facing: EnumFacing,
        placeSpeed: Float
    ) {
        val hitVecOffset = getHitVecOffset(facing)
        val rotation = getRotationTo(Vec3d(pos).add(hitVecOffset))
        val rotationPacket = CPacketPlayer.PositionRotation(player.posX, player.posY, player.posZ, rotation.x, rotation.y, player.onGround)
        val placePacket = CPacketPlayerTryUseItemOnBlock(pos, facing, EnumHand.MAIN_HAND, hitVecOffset.x.toFloat(), hitVecOffset.y.toFloat(), hitVecOffset.z.toFloat())

        connection.sendPacket(rotationPacket)
        delay((40f / placeSpeed).toLong())

        connection.sendPacket(placePacket)
        player.swingArm(EnumHand.MAIN_HAND)
        delay((10f / placeSpeed).toLong())
    }

    /**
     * Placing block without desync
     */
    fun SafeClientEvent.placeBlock(
        pos: BlockPos,
        side: EnumFacing
    ) {
        if (!isPlaceable(pos.offset(side))) return

        val hitVecOffset = getHitVecOffset(side)
        val placePacket = CPacketPlayerTryUseItemOnBlock(pos, side, EnumHand.MAIN_HAND, hitVecOffset.x.toFloat(), hitVecOffset.y.toFloat(), hitVecOffset.z.toFloat())
        connection.sendPacket(placePacket)
        player.swingArm(EnumHand.MAIN_HAND)

        val itemStack = PlayerPacketManager.getHoldingItemStack()
        val block = (itemStack.item as? ItemBlock?)?.block ?: return
        val metaData = itemStack.metadata
        val blockState = block.getStateForPlacement(world, pos, side, hitVecOffset.x.toFloat(), hitVecOffset.y.toFloat(), hitVecOffset.z.toFloat(), metaData, player, EnumHand.MAIN_HAND)
        val soundType = blockState.block.getSoundType(blockState, world, pos, player)
        world.playSound(player, pos, soundType.placeSound, SoundCategory.BLOCKS, (soundType.getVolume() + 1.0f) / 2.0f, soundType.getPitch() * 0.8f)
    }

}