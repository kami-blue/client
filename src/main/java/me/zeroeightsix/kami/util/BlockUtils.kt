package me.zeroeightsix.kami.util

import net.minecraft.block.Block
import net.minecraft.block.BlockEnderChest
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import kotlin.math.floor

/**
 * Created by hub on 15 June 2019
 * Last Updated 12 January 2019 by hub
 */
object BlockUtils {
    @JvmField
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

    @JvmField
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

    fun placeBlockScaffold(pos: BlockPos) {
        val eyesPos = Vec3d(Wrapper.getPlayer().posX,
                Wrapper.getPlayer().posY + Wrapper.getPlayer().getEyeHeight(),
                Wrapper.getPlayer().posZ)
        for (side in EnumFacing.values()) {
            val neighbor = pos.offset(side)
            val side2 = side.opposite

            // check if neighbor can be right clicked
            if (!canBeClicked(neighbor)) {
                continue
            }
            val hitVec = Vec3d(neighbor).add(0.5, 0.5, 0.5)
                    .add(Vec3d(side2.directionVec).scale(0.5))

            // check if hitVec is within range (4.25 blocks)
            if (eyesPos.squareDistanceTo(hitVec) > 18.0625) {
                continue
            }

            // place block
            faceVectorPacketInstant(hitVec)
            processRightClickBlock(neighbor, side2, hitVec)
            Wrapper.getPlayer().swingArm(EnumHand.MAIN_HAND)
            mc.rightClickDelayTimer = 4
            return
        }
    }

    private fun getLegitRotations(vec: Vec3d): FloatArray {
        val eyesPos = eyesPos
        val diffX = vec.x - eyesPos.x
        val diffY = vec.y - eyesPos.y
        val diffZ = vec.z - eyesPos.z
        val diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ)
        val yaw = Math.toDegrees(Math.atan2(diffZ, diffX)).toFloat() - 90f
        val pitch = (-Math.toDegrees(Math.atan2(diffY, diffXZ))).toFloat()
        return floatArrayOf(Wrapper.getPlayer().rotationYaw
                + MathHelper.wrapDegrees(yaw - Wrapper.getPlayer().rotationYaw),
                Wrapper.getPlayer().rotationPitch + MathHelper
                        .wrapDegrees(pitch - Wrapper.getPlayer().rotationPitch))
    }

    private val eyesPos = Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ)

    @JvmStatic
    fun faceVectorPacketInstant(vec: Vec3d) {
        val rotations = getLegitRotations(vec)
        Wrapper.getPlayer().connection.sendPacket(CPacketPlayer.Rotation(rotations[0],
                rotations[1], Wrapper.getPlayer().onGround))
    }

    private fun processRightClickBlock(pos: BlockPos, side: EnumFacing, hitVec: Vec3d) {
        mc.playerController.processRightClickBlock(Wrapper.getPlayer(),
                mc.world, pos, side, hitVec, EnumHand.MAIN_HAND)
    }

    @JvmStatic
    fun canBeClicked(pos: BlockPos): Boolean {
        return getBlock(pos).canCollideCheck(getState(pos), false)
    }

    private fun getBlock(pos: BlockPos): Block {
        return getState(pos).block
    }

    private fun getState(pos: BlockPos): IBlockState {
        return Wrapper.getWorld().getBlockState(pos)
    }

    fun checkForNeighbours(blockPos: BlockPos): Boolean {
        // check if we don't have a block adjacent to blockpos
        if (!hasNeighbour(blockPos)) {
            // find air adjacent to blockpos that does have a block adjacent to it, let's fill this first as to form a bridge between the player and the original blockpos. necessary if the player is going diagonal.
            for (side in EnumFacing.values()) {
                val neighbour = blockPos.offset(side)
                if (hasNeighbour(neighbour)) {
                    return true
                }
            }
            return false
        }
        return true
    }

    fun hasNeighbour(blockPos: BlockPos): Boolean {
        for (side in EnumFacing.values()) {
            val neighbour = blockPos.offset(side)
            if (!Wrapper.getWorld().getBlockState(neighbour).material.isReplaceable) {
                return true
            }
        }
        return false
    }


    /**
     * @return true if there is liquid below
     */
    fun checkForLiquid(): Boolean {
        return getGroundPosY(true) == -1.0f
    }

    /**
     * Get the height of the ground surface below, and check for liquid if [checkLiquid] is true
     *
     * @return The y position of the ground surface, -1.0f if found liquid below and [checkLiquid] is true
     */
    fun getGroundPosY(checkLiquid: Boolean): Float {
        val boundingBox = mc.player.boundingBox
        var yOffset = mc.player.posY - boundingBox.minY
        val xArray = arrayOf(floor(boundingBox.minX).toInt(), floor(boundingBox.maxX).toInt())
        val zArray = arrayOf(floor(boundingBox.minZ).toInt(), floor(boundingBox.maxZ).toInt())
        while (!mc.world.collidesWithAnyBlock(boundingBox.offset(0.0, yOffset, 0.0))) {
            if (checkLiquid) {
                for (x in 0..1) for (z in 0..1) {
                    val blockPos = BlockPos(xArray[x], (mc.player.posY + yOffset).toInt(), zArray[z])
                    if (mc.world.getBlockState(blockPos).block.material.isLiquid) return -1.0f
                }
            }
            yOffset -= 0.05
        }
        return boundingBox.offset(0.0, yOffset + 0.05, 0.0).minY.toFloat()
    }

    /**
     * Checks if given [pos] is able to place block in it
     *
     * @return true playing is not colliding with [pos] and there is block below it
     */
    fun isPlaceable(pos: BlockPos): Boolean {
        val bBox = mc.player.boundingBox
        val xArray = arrayOf(floor(bBox.minX).toInt(), floor(bBox.maxX).toInt())
        val yArray = arrayOf(floor(bBox.minY).toInt(), floor(bBox.maxY).toInt())
        val zArray = arrayOf(floor(bBox.minZ).toInt(), floor(bBox.maxZ).toInt())
        for (x in 0..1) for (y in 0..1) for (z in 0..1) {
            if (pos == BlockPos(xArray[x], yArray[y], zArray[z])) return false
        }
        return mc.world.isAirBlock(pos) && !mc.world.isAirBlock(pos.down())
    }

    /**
     * Checks if given [pos] is able to chest (air above) block in it
     *
     * @return true playing is not colliding with [pos] and there is block below it
     */
    fun isPlaceableForChest(pos: BlockPos): Boolean {
        return isPlaceable(pos) && mc.world.isAirBlock(pos.up())
    }
}