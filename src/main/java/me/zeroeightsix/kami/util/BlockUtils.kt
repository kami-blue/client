package me.zeroeightsix.kami.util

import me.zeroeightsix.kami.command.SafeClientEvent
import me.zeroeightsix.kami.manager.managers.PlayerPacketManager
import me.zeroeightsix.kami.util.math.RotationUtils
import net.minecraft.client.Minecraft
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
import kotlin.math.floor

/**
 * Created by hub on 15/06/19
 * Updated by hub on 12/01/19
 * Updated by Xiaro on 22/08/20
 */
object BlockUtils {
    val blackList = listOf(
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

    val shulkerList = listOf(
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

    private val mc = Minecraft.getMinecraft()

    @JvmStatic
    fun faceVectorPacketInstant(vec: Vec3d) {
        val rotation = RotationUtils.getRotationTo(vec, true)
        mc.player.connection.sendPacket(CPacketPlayer.Rotation(rotation.x.toFloat(), rotation.y.toFloat(), mc.player.onGround))
    }

    fun hasNeighbour(blockPos: BlockPos): Boolean {
        for (side in EnumFacing.values()) {
            val neighbour = blockPos.offset(side)
            if (!mc.world.getBlockState(neighbour).material.isReplaceable) {
                return true
            }
        }
        return false
    }

    fun getHitSide(blockPos: BlockPos): EnumFacing {
        return rayTraceTo(blockPos)?.sideHit ?: EnumFacing.UP
    }

    fun getHitVec(pos: BlockPos, facing: EnumFacing): Vec3d {
        val vec = facing.directionVec
        return Vec3d(vec.x * 0.5 + 0.5 + pos.x, vec.y * 0.5 + 0.5 + pos.y, vec.z * 0.5 + 0.5 + pos.z)
    }

    fun getHitVecOffset(facing: EnumFacing): Vec3d {
        val vec = facing.directionVec
        return Vec3d(vec.x * 0.5 + 0.5, vec.y * 0.5 + 0.5, vec.z * 0.5 + 0.5)
    }

    /**
     * @return true if there is liquid below
     */
    fun checkForLiquid(): Boolean {
        return getGroundPosY(true) == -999.0
    }

    /**
     * Get the height of the ground surface below, and check for liquid if [checkLiquid] is true
     *
     * @return The y position of the ground surface, -999.0 if found liquid below and [checkLiquid]
     * is true or player is above the void
     */
    fun getGroundPosY(checkLiquid: Boolean): Double {
        val boundingBox = mc.player.entityBoundingBox
        var yOffset = mc.player.posY - boundingBox.minY
        val xArray = arrayOf(floor(boundingBox.minX).toInt(), floor(boundingBox.maxX).toInt())
        val zArray = arrayOf(floor(boundingBox.minZ).toInt(), floor(boundingBox.maxZ).toInt())
        while (!mc.world.collidesWithAnyBlock(boundingBox.offset(0.0, yOffset, 0.0))) {
            if (checkLiquid) {
                for (x in 0..1) for (z in 0..1) {
                    val blockPos = BlockPos(xArray[x], (mc.player.posY + yOffset).toInt(), zArray[z])
                    if (isLiquid(blockPos)) return -999.0
                }
            }
            yOffset -= 0.05
            if (mc.player.posY + yOffset < 0.0f) return -999.0
        }
        return boundingBox.offset(0.0, yOffset + 0.05, 0.0).minY
    }

    fun isLiquid(pos: BlockPos): Boolean {
        return mc.world.getBlockState(pos).material.isLiquid
    }

    fun isWater(pos: BlockPos): Boolean {
        return mc.world.getBlockState(pos).block == Blocks.WATER
    }

    fun rayTraceTo(blockPos: BlockPos): RayTraceResult? {
        return mc.world.rayTraceBlocks(mc.player.getPositionEyes(1f), Vec3d(blockPos).add(0.5, 0.5, 0.5))
    }

    /**
     * Checks if given [pos] is able to place block in it
     *
     * @return true playing is not colliding with [pos] and there is block below it
     */
    fun isPlaceable(pos: BlockPos, ignoreSelfCollide: Boolean = false) = mc.world.getBlockState(pos).material.isReplaceable
        && mc.world.checkNoEntityCollision(AxisAlignedBB(pos), if (ignoreSelfCollide) mc.player else null)

    /**
     * Checks if given [pos] is able to chest (air above) block in it
     *
     * @return true playing is not colliding with [pos] and there is block below it
     */
    fun isPlaceableForChest(pos: BlockPos): Boolean {
        return isPlaceable(pos) && !mc.world.getBlockState(pos.down()).material.isReplaceable && mc.world.isAirBlock(pos.up())
    }

    fun buildStructure(placeSpeed: Float, getPlaceInfo: (HashSet<BlockPos>) -> Pair<EnumFacing, BlockPos>?) {
        val emptyHashSet = HashSet<BlockPos>()
        val placed = HashSet<BlockPos>()
        var placeCount = 0
        while (getPlaceInfo(emptyHashSet) != null) {
            val placingInfo = getPlaceInfo(placed) ?: getPlaceInfo(emptyHashSet) ?: break
            placeCount++
            placed.add(placingInfo.second.offset(placingInfo.first))
            doPlace(placingInfo.second, placingInfo.first, placeSpeed)
            if (placeCount >= 4) {
                Thread.sleep(100L)
                placeCount = 0
                placed.clear()
            }
        }
    }

    fun getPlaceInfo(center: BlockPos?, structureOffset: Array<BlockPos>, toIgnore: HashSet<BlockPos>, maxAttempts: Int, attempts: Int = 1): Pair<EnumFacing, BlockPos>? {
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

    fun getNeighbour(
        blockPos: BlockPos,
        attempts: Int = 3,
        range: Float = 4.25f,
        sides: Array<EnumFacing> = EnumFacing.values(),
        toIgnore: HashSet<BlockPos> = HashSet()
    ): Pair<EnumFacing, BlockPos>? {
        for (side in sides) {
            val pos = blockPos.offset(side)
            if (!toIgnore.add(pos)) continue
            if (mc.world.getBlockState(pos).material.isReplaceable) continue
            if (mc.player.getPositionEyes(1f).distanceTo(Vec3d(pos).add(getHitVecOffset(side))) > range) continue
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
    fun doPlace(pos: BlockPos, facing: EnumFacing, placeSpeed: Float) {
        val hitVecOffset = getHitVecOffset(facing)
        val rotation = RotationUtils.getRotationTo(Vec3d(pos).add(hitVecOffset), true)
        val rotationPacket = CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY, mc.player.posZ, rotation.x.toFloat(), rotation.y.toFloat(), mc.player.onGround)
        val placePacket = CPacketPlayerTryUseItemOnBlock(pos, facing, EnumHand.MAIN_HAND, hitVecOffset.x.toFloat(), hitVecOffset.y.toFloat(), hitVecOffset.z.toFloat())
        mc.connection!!.sendPacket(rotationPacket)
        Thread.sleep((40f / placeSpeed).toLong())
        mc.connection!!.sendPacket(placePacket)
        mc.player.swingArm(EnumHand.MAIN_HAND)
        Thread.sleep((10f / placeSpeed).toLong())
    }

    /**
     * Placing block without desync
     */
    fun SafeClientEvent.placeBlock(pos: BlockPos, side: EnumFacing) {
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