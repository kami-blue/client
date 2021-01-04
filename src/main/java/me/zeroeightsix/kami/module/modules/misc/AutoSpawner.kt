package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.*
import me.zeroeightsix.kami.util.math.VectorUtils
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.block.BlockDeadBush
import net.minecraft.block.BlockSoulSand
import net.minecraft.block.BlockTallGrass
import net.minecraft.init.Blocks
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemSkull
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.TickEvent

/**
 * TODO: Rewrite
 */
@Module.Info(
    name = "AutoSpawner",
    category = Module.Category.MISC,
    description = "Automatically spawns Withers, Iron Golems and Snowmen"
)
object AutoSpawner : Module() {
    private val useMode = register(Settings.e<UseMode>("UseMode", UseMode.SPAM))
    private val party = register(Settings.b("Party", false))
    private val partyWithers = register(Settings.booleanBuilder("Withers").withValue(false).withVisibility { party.value })
    private val entityMode = register(Settings.enumBuilder(EntityMode::class.java).withName("EntityMode").withValue(EntityMode.SNOW).withVisibility { !party.value })
    private val placeRange = register(Settings.floatBuilder("PlaceRange").withValue(3.5f).withRange(2f, 10f))
    private val delay = register(Settings.integerBuilder("Delay").withValue(20).withRange(10, 100).withVisibility { useMode.value == UseMode.SPAM })
    private val debug = register(Settings.b("Info", true))

    private enum class UseMode {
        SINGLE, SPAM
    }

    private enum class EntityMode {
        SNOW, IRON, WITHER
    }

    private var timer = TickTimer(TimeUnit.TICKS)
    private var placeTarget: BlockPos? = null
    private var rotationPlaceableX = false
    private var rotationPlaceableZ = false
    private var bodySlot = -1
    private var headSlot = -1
    private var isSneaking = false
    private var buildStage = Stage.PRE

    private enum class Stage {
        PRE, BODY, HEAD, DELAY
    }

    override fun getHudInfo(): String {
        return if (party.value) {
            if (partyWithers.value) "PARTY WITHER"
            else "PARTY"
        } else entityMode.value.toString()
    }

    override fun onEnable() {
        if (mc.player == null) disable()
    }

    override fun onDisable() {
        placeTarget = null
        rotationPlaceableX = false
        rotationPlaceableZ = false
        bodySlot = -1
        isSneaking = false
        buildStage = Stage.PRE
    }

    init {
        safeListener<TickEvent.ClientTickEvent> {
            when (buildStage) {
                Stage.PRE -> {
                    isSneaking = false
                    rotationPlaceableX = false
                    rotationPlaceableZ = false

                    if (party.value) randomizeEntity()

                    if (!checkBlocksInHotbar()) {
                        if (!party.value) {
                            if (debug.value) sendChatMessage("$chatName &c Blocks missing for: &c${entityMode.value}, disabling.")
                            disable()
                        }
                        return@safeListener
                    }
                    val blockPosList = VectorUtils.getBlockPosInSphere(player.positionVector, placeRange.value)
                    var noPositionInArea = true

                    for (pos in blockPosList) {
                        placeTarget = pos
                        if (testStructure()) {
                            noPositionInArea = false
                            break
                        }
                    }

                    if (noPositionInArea) {
                        if (useMode.value == UseMode.SINGLE) {
                            if (debug.value) sendChatMessage("$chatName No valid position, disabling.")
                            disable()
                            return@safeListener
                        }
                    }

                    buildStage = Stage.BODY
                }
                Stage.BODY -> {
                    InventoryUtils.swapSlot(bodySlot)
                    for (pos in BodyParts.bodyBase) placeBlock(placeTarget!!.add(pos))
                    if (entityMode.value == EntityMode.WITHER || entityMode.value == EntityMode.IRON) {
                        if (rotationPlaceableX) {
                            for (pos in BodyParts.ArmsX) {
                                placeBlock(placeTarget!!.add(pos))
                            }
                        } else if (rotationPlaceableZ) {
                            for (pos in BodyParts.ArmsZ) {
                                placeBlock(placeTarget!!.add(pos))
                            }
                        }
                    }

                    buildStage = Stage.HEAD
                }
                Stage.HEAD -> {
                    InventoryUtils.swapSlot(headSlot)

                    if (entityMode.value == EntityMode.IRON || entityMode.value == EntityMode.SNOW) {
                        for (pos in BodyParts.head) placeBlock(placeTarget!!.add(pos))
                    }

                    if (entityMode.value == EntityMode.WITHER) {
                        if (rotationPlaceableX) {
                            for (pos in BodyParts.headsX) {
                                placeBlock(placeTarget!!.add(pos))
                            }
                        } else if (rotationPlaceableZ) {
                            for (pos in BodyParts.headsZ) {
                                placeBlock(placeTarget!!.add(pos))
                            }
                        }
                    }

                    if (isSneaking) {
                        player.connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.STOP_SNEAKING))
                        isSneaking = false
                    }

                    if (useMode.value == UseMode.SINGLE) disable()

                    buildStage = Stage.DELAY
                    timer.reset()
                }
                Stage.DELAY -> {
                    if (timer.tick(delay.value.toLong())) buildStage = Stage.PRE
                }
            }
        }
    }

    private fun randomizeEntity() {
        entityMode.value = EntityMode.values().random()
        if (!partyWithers.value && entityMode.value == EntityMode.WITHER) randomizeEntity()
    }

    private fun checkBlocksInHotbar(): Boolean {
        headSlot = -1
        bodySlot = -1
        for (slotIndex in 0..8) {
            val stack = mc.player.inventory.getStackInSlot(slotIndex) ?: continue
            if (stack.isEmpty) continue

            when (entityMode.value as EntityMode) {
                EntityMode.SNOW -> {
                    if (stack.item is ItemBlock) {
                        val block = (stack.item as ItemBlock).block
                        if (block == Blocks.LIT_PUMPKIN || block == Blocks.PUMPKIN) {
                            if (checkItemStackSize(stack, 1)) headSlot = slotIndex
                        }
                        if (block == Blocks.SNOW) {
                            if (checkItemStackSize(stack, 2)) bodySlot = slotIndex
                        }
                    }
                }
                EntityMode.IRON -> {
                    if (stack.item is ItemBlock) {
                        val block = (stack.item as ItemBlock).block
                        if (block == Blocks.LIT_PUMPKIN || block == Blocks.PUMPKIN) {
                            if (checkItemStackSize(stack, 1)) headSlot = slotIndex
                        }
                        if (block == Blocks.IRON_BLOCK) {
                            if (checkItemStackSize(stack, 4)) bodySlot = slotIndex
                        }
                    }
                }
                EntityMode.WITHER -> {
                    if (stack.item is ItemSkull && stack.itemDamage == 1) {
                        if (checkItemStackSize(stack, 3)) headSlot = slotIndex
                    } else if (stack.item is ItemBlock) {
                        val block = (stack.item as ItemBlock).block
                        if (block is BlockSoulSand) {
                            if (checkItemStackSize(stack, 4)) bodySlot = slotIndex
                        }
                    }
                }
            }
        }
        return bodySlot != -1 && headSlot != -1
    }

    private fun checkItemStackSize(stack: ItemStack, target: Int): Boolean {
        return mc.player.isCreative && stack.count >= 1 || stack.count >= target
    }

    private fun testStructure(): Boolean {
        placeTarget?.let {
            rotationPlaceableX = true
            rotationPlaceableZ = true

            // dont place on grass
            val block = mc.world.getBlockState(it).block
            if (block is BlockTallGrass || block is BlockDeadBush) return false
            if (getPlaceableSide(it) == null) return false

            for (pos in BodyParts.bodyBase) {
                if (placingIsBlocked(it.add(pos))) return false
            }

            if (entityMode.value == EntityMode.SNOW || entityMode.value == EntityMode.IRON) {
                for (pos in BodyParts.head) {
                    if (placingIsBlocked(it.add(pos))) return false
                }
            }

            if (entityMode.value == EntityMode.IRON || entityMode.value == EntityMode.WITHER) {
                for (pos in BodyParts.ArmsX) {
                    if (placingIsBlocked(it.add(pos))) rotationPlaceableX = false
                }
                for (pos in BodyParts.ArmsZ) {
                    if (placingIsBlocked(it.add(pos))) rotationPlaceableZ = false
                }
            }

            if (entityMode.value == EntityMode.WITHER) {
                for (pos in BodyParts.headsX) {
                    if (placingIsBlocked(it.add(pos))) rotationPlaceableX = false
                }
                for (pos in BodyParts.headsZ) {
                    if (placingIsBlocked(it.add(pos))) rotationPlaceableZ = false
                }
            }

            return rotationPlaceableX || rotationPlaceableZ
        } ?: return false
    }

    private object BodyParts {
        val bodyBase = arrayOf(
            BlockPos(0, 1, 0),
            BlockPos(0, 2, 0))
        val ArmsX = arrayOf(
            BlockPos(-1, 2, 0),
            BlockPos(1, 2, 0)
        )
        val ArmsZ = arrayOf(
            BlockPos(0, 2, -1),
            BlockPos(0, 2, 1)
        )
        val headsX = arrayOf(
            BlockPos(0, 3, 0),
            BlockPos(-1, 3, 0),
            BlockPos(1, 3, 0)
        )
        val headsZ = arrayOf(
            BlockPos(0, 3, 0),
            BlockPos(0, 3, -1),
            BlockPos(0, 3, 1)
        )
        val head = arrayOf(
            BlockPos(0, 3, 0)
        )
    }

    private fun placingIsBlocked(pos: BlockPos): Boolean {
        return !mc.world.isAirBlock(pos) || !mc.world.checkNoEntityCollision(AxisAlignedBB(pos))
    }

    private fun placeBlock(pos: BlockPos) {
        val side = getPlaceableSide(pos) ?: return
        val neighbour = pos.offset(side)
        val opposite = side.opposite
        val hitVec = Vec3d(neighbour).add(0.5, 0.5, 0.5).add(Vec3d(opposite.directionVec).scale(0.5))
        val neighbourBlock = mc.world.getBlockState(neighbour).block
        if (!isSneaking && (WorldUtils.blackList.contains(neighbourBlock) || WorldUtils.shulkerList.contains(neighbourBlock))) {
            mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING))
            isSneaking = true
        }
        mc.playerController.processRightClickBlock(mc.player, mc.world, neighbour, opposite, hitVec, EnumHand.MAIN_HAND)
        mc.player.swingArm(EnumHand.MAIN_HAND)
    }

    private fun getPlaceableSide(pos: BlockPos): EnumFacing? {
        for (side in EnumFacing.values()) {
            val neighbour = pos.offset(side)
            if (!mc.world.getBlockState(neighbour).block.canCollideCheck(mc.world.getBlockState(neighbour), false)) {
                continue
            }
            val blockState = mc.world.getBlockState(neighbour)
            if (!blockState.material.isReplaceable && blockState.block !is BlockTallGrass && blockState.block !is BlockDeadBush) {
                return side
            }
        }
        return null
    }
}