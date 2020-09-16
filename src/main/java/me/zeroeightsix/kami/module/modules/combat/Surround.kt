package me.zeroeightsix.kami.module.modules.combat

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.manager.mangers.PlayerPacketManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BlockUtils
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.MovementUtils
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.combat.SurroundUtils
import me.zeroeightsix.kami.util.math.MathUtils
import me.zeroeightsix.kami.util.math.RotationUtils
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.math.round

@Module.Info(
        name = "Surround",
        category = Module.Category.COMBAT,
        description = "Surrounds you with obsidian to take less damage",
        modulePriority = 100
)
object Surround : Module() {
    private val autoCenter = register(Settings.e<AutoCenterMode>("AutoCenter", AutoCenterMode.MOTION))
    private val placeSpeed = register(Settings.floatBuilder("PlacesPerTick").withValue(4f).withRange(0f, 10f))
    private val autoDisable = register(Settings.e<AutoDisableMode>("AutoDisable", AutoDisableMode.OUT_OF_HOLE))
    private val outOfHoleTimeout = register(Settings.integerBuilder("OutOfHoleTimeout(t)").withValue(20).withRange(1, 50).withVisibility { autoDisable.value == AutoDisableMode.OUT_OF_HOLE })
    private val enableInHole = register(Settings.b("EnableInHole", true))
    private val inHoleTimeout = register(Settings.integerBuilder("InHoleTimeout(t)").withValue(50).withRange(1, 100).withVisibility { enableInHole.value })

    enum class AutoCenterMode {
        OFF, TP, MOTION
    }

    enum class AutoDisableMode {
        ONE_TIME, OUT_OF_HOLE
    }

    private var holePos: BlockPos? = null
    private var toggleTimer = TimerUtils.StopTimer(TimerUtils.TimeUnit.TICKS)
    private val placeThread = Thread { runSurround() }.apply { name = "Surround" }
    private val threadPool = Executors.newSingleThreadExecutor()
    private var future: Future<*>? = null
    private var prevSlot = -1

    @EventHandler
    private val sendListener = Listener(EventHook { event: PacketEvent.Send ->
        if (future?.isDone != false || event.packet !is CPacketHeldItemChange) return@EventHook
        if (event.packet.slotId != getObby()) { // If player changed slot during placing, cancel it and assign it to prevSlot
            prevSlot = event.packet.slotId
            event.cancel()
        }
    })

    override fun onEnable() {
        toggleTimer.reset()
        mc.player?.setVelocity(0.0, -5.0, 0.0)
    }

    override fun onDisable() {
        syncItem()
        toggleTimer.reset()
        holePos = null
    }

    override fun isActive(): Boolean {
        return isEnabled && future?.isDone == false
    }

    // Runs the codes on rendering for more immediate reaction
    override fun onRender() {
        if (mc.world == null || mc.player == null) return
        if (getObby() == -1) return
        if (isDisabled) {
            enableInHoleCheck()
            return
        }

        // Following codes will not run if disabled
        if (!mc.player.onGround || MathUtils.mcPlayerPosFloored(mc) != holePos) { // Out of hole check
            outOfHoleCheck()
            return
        } else {
            toggleTimer.reset()
        }
        if (!isPlaceable() || !centerPlayer()) { // Placeable & Centered check
            if (!isPlaceable() && autoDisable.value == AutoDisableMode.ONE_TIME) disable()
            return
        }
        if (future?.isDone != false) future = threadPool.submit(placeThread)
    }

    override fun onUpdate() {
        if (isEnabled && holePos == null) holePos = MathUtils.mcPlayerPosFloored(mc)
        if (future?.isDone == false) PlayerPacketManager.addPacket(this, PlayerPacketManager.PlayerPacket(sprinting = false, moving = false, rotating = false))
        if ((future?.isDone == true || future?.isCancelled == true) && prevSlot != -1) syncItem()
    }

    private fun syncItem() {
        mc.playerController?.let {
            if (prevSlot != -1) mc.connection!!.sendPacket(CPacketHeldItemChange(prevSlot))
            prevSlot = -1
        }
    }

    private fun enableInHoleCheck() {
        if (enableInHole.value && mc.player.onGround && MovementUtils.getSpeed() < 0.15 && SurroundUtils.checkHole(mc.player) != SurroundUtils.HoleType.NONE) {
            if (toggleTimer.stop() > inHoleTimeout.value) {
                MessageSendHelper.sendChatMessage("$chatName You are in hole for longer than ${inHoleTimeout.value} ticks, enabling")
                enable()
            }
        } else {
            toggleTimer.reset()
        }
    }

    private fun outOfHoleCheck() {
        if (autoDisable.value == AutoDisableMode.OUT_OF_HOLE) {
            if (toggleTimer.stop() > outOfHoleTimeout.value) {
                MessageSendHelper.sendChatMessage("$chatName You are out of hole for longer than ${outOfHoleTimeout.value} ticks, disabling")
                disable()
            }
        }
    }

    private fun getObby(): Int {
        val slots = InventoryUtils.getSlotsHotbar(49)
        if (slots == null) { // Obsidian check
            if (isEnabled) {
                MessageSendHelper.sendChatMessage("$chatName No obsidian in hotbar, disabling!")
                disable()
            }
            return -1
        }
        return slots[0]
    }

    private fun isPlaceable(): Boolean {
        val playerPos = MathUtils.mcPlayerPosFloored(mc)
        for (offset in SurroundUtils.surroundOffset) {
            val pos = playerPos.add(offset)
            if (!mc.world.checkNoEntityCollision(AxisAlignedBB(pos))) continue
            if (!mc.world.getBlockState(pos).material.isReplaceable) continue
            return true
        }
        return false
    }

    private fun centerPlayer(): Boolean {
        return if (autoCenter.value == AutoCenterMode.OFF) {
            true
        } else {
            val centerDiff = getCenterDiff()
            if (!isCentered()) {
                mc.player.setVelocity(0.0, -5.0, 0.0)
                if (autoCenter.value == AutoCenterMode.TP) {
                    val posX = mc.player.posX + MathHelper.clamp(centerDiff.x, -0.25, 0.25)
                    val posZ = mc.player.posZ + MathHelper.clamp(centerDiff.z, -0.25, 0.25)
                    mc.player.setPosition(posX, mc.player.posY, posZ)
                } else {
                    mc.player.motionX = centerDiff.x / 2.0
                    mc.player.motionZ = centerDiff.z / 2.0
                }
            }
            isCentered()
        }
    }

    private fun isCentered(): Boolean {
        return getCenterDiff().length() < 0.2
    }

    private fun getCenterDiff(): Vec3d {
        return Vec3d(roundToCenter(mc.player.posX), mc.player.posY, roundToCenter(mc.player.posZ)).subtract(mc.player.positionVector)
    }

    private fun roundToCenter(doubleIn: Double): Double {
        return round(doubleIn + 0.5) - 0.5
    }

    private fun runSurround() {
        val slot = getObby()
        if (slot == -1) return
        prevSlot = mc.playerController.currentPlayerItem
        mc.connection!!.sendPacket(CPacketHeldItemChange(slot))
        val placed = ArrayList<BlockPos>()
        while (isEnabled) {
            val pos = getPlacingPos(placed) ?: break
            val neighbor = BlockUtils.getNeighbour(pos, 1) ?: break
            placed.add(pos)
            doPlace(neighbor.second, neighbor.first)
        }
    }

    private fun getPlacingPos(toIgnore: ArrayList<BlockPos>): BlockPos? {
        val playerPos = MathUtils.mcPlayerPosFloored(mc)
        for (offset in SurroundUtils.surroundOffset) {
            val pos = playerPos.add(offset)
            if (toIgnore.contains(pos)) continue
            if (!mc.world.checkNoEntityCollision(AxisAlignedBB(pos))) continue
            if (!mc.world.getBlockState(pos).material.isReplaceable) continue
            if (BlockUtils.hasNeighbour(pos)) return pos
            if (!mc.world.checkNoEntityCollision(AxisAlignedBB(pos.down()))) continue
            if (!toIgnore.contains(pos.down())) return pos.down()
        }
        return null
    }

    private fun doPlace(pos: BlockPos, facing: EnumFacing) {
        val hitVecOffset = BlockUtils.getHitVecOffset(facing)
        val rotation = RotationUtils.getRotationTo(Vec3d(pos).add(hitVecOffset), true)
        val rotationPacket = CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY, mc.player.posZ, rotation.x.toFloat(), rotation.y.toFloat(), mc.player.onGround)
        val placePacket = CPacketPlayerTryUseItemOnBlock(pos, facing, EnumHand.MAIN_HAND, hitVecOffset.x.toFloat(), hitVecOffset.y.toFloat(), hitVecOffset.z.toFloat())
        mc.connection!!.sendPacket(rotationPacket)
        Thread.sleep((25f / placeSpeed.value).toLong())
        mc.connection!!.sendPacket(placePacket)
        mc.player.swingArm(EnumHand.MAIN_HAND)
        Thread.sleep((25f / placeSpeed.value).toLong())
    }

    init {
        alwaysListening = enableInHole.value
        enableInHole.settingListener = Setting.SettingListeners {
            alwaysListening = enableInHole.value
        }
    }
}