package me.zeroeightsix.kami.module.modules.combat

import com.mojang.realmsclient.gui.ChatFormatting
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.player.Freecam
import me.zeroeightsix.kami.module.modules.player.NoBreakAnimation
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BlockUtils
import me.zeroeightsix.kami.util.MessageSendHelper
import net.minecraft.block.BlockAir
import net.minecraft.block.BlockLiquid
import net.minecraft.block.BlockObsidian
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

/**
 * @author hub
 * @since 2019-8-13
 */
@Module.Info(
        name = "AutoFeetPlace",
        category = Module.Category.COMBAT,
        description = "Continually places obsidian around your feet"
)
class AutoFeetPlace : Module() {
    private val mode = register(Settings.e<Mode>("Mode", Mode.FULL))
    private val triggerable = register(Settings.b("Triggerable", true))
    private val disableNone = register(Settings.b("DisableNoObby", true))
    private val timeoutTicks = register(Settings.integerBuilder("TimeoutTicks").withMinimum(1).withValue(40).withMaximum(100).withVisibility { b: Int? -> triggerable.value }.build())
    private val blocksPerTick = register(Settings.integerBuilder("BlocksPerTick").withMinimum(1).withValue(4).withMaximum(9).build())
    private val tickDelay = register(Settings.integerBuilder("TickDelay").withMinimum(0).withValue(0).withMaximum(10).build())
    private val rotate = register(Settings.b("Rotate", true))
    private val infoMessage = register(Settings.b("InfoMessage", false))

    private var offsetStep = 0
    private var delayStep = 0
    private var playerHotbarSlot = -1
    private var lastHotbarSlot = -1
    private var isSneaking = false
    private var totalTicksRunning = 0
    private var firstRun = false
    private var missingObiDisable = false

    override fun onEnable() {
        if (mc.player == null) {
            disable()
            return
        }
        firstRun = true

        // save initial player hand
        playerHotbarSlot = mc.player.inventory.currentItem
        lastHotbarSlot = -1
    }

    override fun onDisable() {
        if (mc.player == null) return

        // load initial player hand
        if (lastHotbarSlot != playerHotbarSlot && playerHotbarSlot != -1) {
            mc.player.inventory.currentItem = playerHotbarSlot
        }
        if (isSneaking) {
            mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING))
            isSneaking = false
        }
        playerHotbarSlot = -1
        lastHotbarSlot = -1
        missingObiDisable = false
    }

    override fun onUpdate() {
        if (mc.player == null || KamiMod.MODULE_MANAGER.isModuleEnabled(Freecam::class.java)) {
            return
        }

        if (triggerable.value && totalTicksRunning >= timeoutTicks.value) {
            totalTicksRunning = 0
            disable()
            return
        }

        if (!firstRun) {
            delayStep = if (delayStep < tickDelay.value) {
                delayStep++
                return
            } else {
                0
            }
        }

        if (firstRun) {
            firstRun = false
            if (findObiInHotbar() == -1) {
                missingObiDisable = true
            }
        }

        var offsetPattern = arrayOfNulls<Vec3d>(0)
        var maxSteps = 0

        if (mode.value == Mode.FULL) {
            offsetPattern = Offsets.FULL
            maxSteps = Offsets.FULL.size
        }
        if (mode.value == Mode.SURROUND) {
            offsetPattern = Offsets.SURROUND
            maxSteps = Offsets.SURROUND.size
        }

        var blocksPlaced = 0

        while (blocksPlaced < blocksPerTick.value) {
            if (offsetStep >= maxSteps) {
                offsetStep = 0
                break
            }
            val offsetPos = BlockPos(offsetPattern[offsetStep])
            val targetPos = BlockPos(mc.player.positionVector).add(offsetPos.x, offsetPos.y, offsetPos.z)
            if (placeBlock(targetPos)) {
                blocksPlaced++
            }
            offsetStep++
        }

        if (blocksPlaced > 0) {
            if (lastHotbarSlot != playerHotbarSlot && playerHotbarSlot != -1) {
                mc.player.inventory.currentItem = playerHotbarSlot
                lastHotbarSlot = playerHotbarSlot
            }
            if (isSneaking) {
                mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING))
                isSneaking = false
            }
        }

        totalTicksRunning++

        if (missingObiDisable && disableNone.value) {
            missingObiDisable = false
            if (infoMessage.value) {
                MessageSendHelper.sendChatMessage("$chatName " + ChatFormatting.RED + "Disabled" + ChatFormatting.RESET + ", Obsidian missing!")
            }
            disable()
        }
    }

    private fun placeBlock(pos: BlockPos): Boolean {
        // check if block is already placed
        val block = mc.world.getBlockState(pos).block
        if (block !is BlockAir && block !is BlockLiquid) {
            return false
        }

        // check if entity blocks placing
        for (entity in mc.world.getEntitiesWithinAABBExcludingEntity(null, AxisAlignedBB(pos))) {
            if (entity !is EntityItem && entity !is EntityXPOrb) {
                return false
            }
        }
        val side = getPlaceableSide(pos) ?: return false

        // check if we have a block adjacent to blockpos to click at
        val neighbour = pos.offset(side)
        val opposite = side.opposite

        // check if neighbor can be right clicked
        if (!BlockUtils.canBeClicked(neighbour)) {
            return false
        }

        val hitVec = Vec3d(neighbour).add(0.5, 0.5, 0.5).add(Vec3d(opposite.directionVec).scale(0.5))
        val neighbourBlock = mc.world.getBlockState(neighbour).block
        val obiSlot = findObiInHotbar()

        if (obiSlot == -1) {
            missingObiDisable = true
            return false
        }
        if (lastHotbarSlot != obiSlot) {
            mc.player.inventory.currentItem = obiSlot
            lastHotbarSlot = obiSlot
        }
        if (!isSneaking && BlockUtils.blackList.contains(neighbourBlock) || BlockUtils.shulkerList.contains(neighbourBlock)) {
            mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING))
            isSneaking = true
        }
        if (rotate.value) {
            BlockUtils.faceVectorPacketInstant(hitVec)
        }

        mc.playerController.processRightClickBlock(mc.player, mc.world, neighbour, opposite, hitVec, EnumHand.MAIN_HAND)
        mc.player.swingArm(EnumHand.MAIN_HAND)
        mc.rightClickDelayTimer = 4

        if (KamiMod.MODULE_MANAGER.isModuleEnabled(NoBreakAnimation::class.java)) {
            KamiMod.MODULE_MANAGER.getModuleT(NoBreakAnimation::class.java).resetMining()
        }
        return true
    }

    private fun findObiInHotbar(): Int {
        // search blocks in hotbar
        var slot = -1
        for (i in 0..8) {
            // filter out non-block items
            val stack = mc.player.inventory.getStackInSlot(i)
            if (stack == ItemStack.EMPTY || stack.getItem() !is ItemBlock) {
                continue
            }
            val block = (stack.getItem() as ItemBlock).block
            if (block is BlockObsidian) {
                slot = i
                break
            }
        }
        return slot
    }

    private enum class Mode {
        SURROUND, FULL
    }

    private object Offsets {
        val SURROUND = arrayOf<Vec3d?>(
                Vec3d(1.0, 0.0, 0.0),
                Vec3d(0.0, 0.0, 1.0),
                Vec3d(-1.0, 0.0, 0.0),
                Vec3d(0.0, 0.0, -1.0),
                Vec3d(1.0, -1.0, 0.0),
                Vec3d(0.0, -1.0, 1.0),
                Vec3d(-1.0, -1.0, 0.0),
                Vec3d(0.0, -1.0, -1.0)
        )
        val FULL = arrayOf<Vec3d?>(
                Vec3d(1.0, 0.0, 0.0),
                Vec3d(0.0, 0.0, 1.0),
                Vec3d(-1.0, 0.0, 0.0),
                Vec3d(0.0, 0.0, -1.0),
                Vec3d(1.0, -1.0, 0.0),
                Vec3d(0.0, -1.0, 1.0),
                Vec3d(-1.0, -1.0, 0.0),
                Vec3d(0.0, -1.0, -1.0),
                Vec3d(0.0, -1.0, 0.0)
        )
    }

    companion object {
        private fun getPlaceableSide(pos: BlockPos): EnumFacing? {
            for (side in EnumFacing.values()) {
                val neighbour = pos.offset(side)
                if (!mc.world.getBlockState(neighbour).block.canCollideCheck(mc.world.getBlockState(neighbour), false)) {
                    continue
                }
                val blockState = mc.world.getBlockState(neighbour)
                if (!blockState.material.isReplaceable) {
                    return side
                }
            }
            return null
        }
    }
}