package me.zeroeightsix.kami.module.modules.combat

import me.zeroeightsix.kami.manager.mangers.CombatManager
import me.zeroeightsix.kami.manager.mangers.PlayerPacketManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.movement.Strafe
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BlockUtils
import me.zeroeightsix.kami.util.InventoryUtils
import me.zeroeightsix.kami.util.MovementUtils
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.combat.SurroundUtils
import me.zeroeightsix.kami.util.math.VectorUtils.toBlockPos
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.util.EnumFacing
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
        modulePriority = 200
)
object Surround : Module() {
    private val autoCenter = register(Settings.e<AutoCenterMode>("AutoCenter", AutoCenterMode.MOTION))
    private val placeSpeed = register(Settings.floatBuilder("PlacesPerTick").withValue(4f).withRange(0.25f, 5f).withStep(0.25f))
    private val autoDisable = register(Settings.e<AutoDisableMode>("AutoDisable", AutoDisableMode.OUT_OF_HOLE))
    private val outOfHoleTimeout = register(Settings.integerBuilder("OutOfHoleTimeout(t)").withValue(20).withRange(1, 50).withVisibility { autoDisable.value == AutoDisableMode.OUT_OF_HOLE })
    private val enableInHole = register(Settings.b("EnableInHole", true))
    private val inHoleTimeout = register(Settings.integerBuilder("InHoleTimeout(t)").withValue(50).withRange(1, 100).withVisibility { enableInHole.value })
    private val disableStrafe = register(Settings.b("DisableStrafe", true))

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
    private var strafeEnabled = false

    override fun onEnable() {
        toggleTimer.reset()
        mc.player?.setVelocity(0.0, -5.0, 0.0)
    }

    override fun onDisable() {
        PlayerPacketManager.resetHotbar()
        toggleTimer.reset()
        holePos = null
        if (strafeEnabled && disableStrafe.value) {
            Strafe.enable()
            strafeEnabled = false
        }
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
        if (!mc.player.onGround || mc.player.positionVector.toBlockPos() != holePos) { // Out of hole check
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
        if (isEnabled && holePos == null && centerPlayer()) holePos = mc.player.positionVector.toBlockPos()
        if (future?.isDone == false && future?.isCancelled == false) {
            val slot = getObby()
            if (slot != -1) PlayerPacketManager.spoofHotbar(getObby())
            PlayerPacketManager.addPacket(this, PlayerPacketManager.PlayerPacket(rotating = false))
        } else if (isEnabled && CombatManager.isOnTopPriority(this)) {
            PlayerPacketManager.resetHotbar()
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
        val playerPos = mc.player.positionVector.toBlockPos()
        for (offset in SurroundUtils.surroundOffset) {
            val pos = playerPos.add(offset)
            if (BlockUtils.isPlaceable(pos)) return true
        }
        return false
    }

    private fun centerPlayer(): Boolean {
        return if (autoCenter.value == AutoCenterMode.OFF) {
            true
        } else {
            if (disableStrafe.value) {
                strafeEnabled = Strafe.isEnabled
                Strafe.disable()
            }
            SurroundUtils.centerPlayer(autoCenter.value == AutoCenterMode.TP)
        }
    }

    private fun runSurround() {
        BlockUtils.buildStructure(placeSpeed.value) {
            if (isEnabled && CombatManager.isOnTopPriority(this)) {
                BlockUtils.getPlaceInfo(mc.player.positionVector.toBlockPos(), SurroundUtils.surroundOffset, it, 2)
            } else {
                null
            }
        }
    }

    private fun getPlaceInfo(toIgnore: List<BlockPos>, attempts: Int = 1): Pair<EnumFacing, BlockPos>? {
        val playerPos = mc.player.positionVector.toBlockPos()
        for (offset in SurroundUtils.surroundOffset) {
            val pos = playerPos.add(offset)
            if (toIgnore.contains(pos)) continue
            if (!BlockUtils.isPlaceable(pos)) continue
            return BlockUtils.getNeighbour(pos, attempts) ?: continue
        }
        if (attempts <= 2) {
            return getPlaceInfo(toIgnore, attempts + 1)
        }
        return null
    }

    init {
        alwaysListening = enableInHole.value
        enableInHole.settingListener = Setting.SettingListeners {
            alwaysListening = enableInHole.value
        }
    }
}