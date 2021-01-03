package me.zeroeightsix.kami.module.modules.combat

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.manager.managers.CombatManager
import me.zeroeightsix.kami.manager.managers.PlayerPacketManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.movement.Strafe
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.*
import me.zeroeightsix.kami.util.MovementUtils.speed
import me.zeroeightsix.kami.util.combat.SurroundUtils
import me.zeroeightsix.kami.util.math.VectorUtils.toBlockPos
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.threads.defaultScope
import me.zeroeightsix.kami.util.threads.isActiveOrFalse
import net.minecraft.util.math.BlockPos
import org.kamiblue.event.listener.listener

@CombatManager.CombatModule
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
    private val outOfHoleTimeout = register(Settings.integerBuilder("OutOfHoleTimeout(t)").withValue(10).withRange(1, 50).withVisibility { autoDisable.value == AutoDisableMode.OUT_OF_HOLE })
    private val enableInHole = register(Settings.b("EnableInHole", false))
    private val inHoleTimeout = register(Settings.integerBuilder("InHoleTimeout(t)").withValue(50).withRange(1, 100).withVisibility { enableInHole.value })
    private val disableStrafe = register(Settings.b("DisableStrafe", true))

    enum class AutoCenterMode {
        OFF, TP, MOTION
    }

    enum class AutoDisableMode {
        ONE_TIME, OUT_OF_HOLE
    }

    private var holePos: BlockPos? = null
    private var toggleTimer = StopTimer(TimeUnit.TICKS)
    private var job: Job? = null

    override fun onEnable() {
        toggleTimer.reset()
    }

    override fun onDisable() {
        PlayerPacketManager.resetHotbar()
        toggleTimer.reset()
        holePos = null
    }

    override fun isActive(): Boolean {
        return isEnabled && job.isActiveOrFalse
    }

    init {
        listener<SafeTickEvent> {
            if (getObby() == -1) return@listener
            if (isDisabled) {
                enableInHoleCheck()
                return@listener
            }

            // Following codes will not run if disabled

            // Update hole pos
            if (holePos == null || inHoleCheck()) {
                holePos = mc.player.positionVector.toBlockPos()
            }

            // Out of hole check
            if (mc.player.positionVector.toBlockPos() != holePos) {
                outOfHoleCheck()
                return@listener
            } else {
                toggleTimer.reset()
            }

            // Placeable & Centered check
            if (!isPlaceable() || !centerPlayer()) {
                if (autoDisable.value == AutoDisableMode.ONE_TIME) disable()
                return@listener
            }

            // The actual job
            if (!job.isActiveOrFalse) {
                job = runSurround()
            } else if (job.isActiveOrFalse) {
                spoofHotbar()
                PlayerPacketManager.addPacket(this, PlayerPacketManager.PlayerPacket(rotating = false))
            } else if (isEnabled && CombatManager.isOnTopPriority(this)) {
                PlayerPacketManager.resetHotbar()
            }
        }
    }

    private fun enableInHoleCheck() {
        if (enableInHole.value && inHoleCheck()) {
            if (toggleTimer.stop() > inHoleTimeout.value) {
                MessageSendHelper.sendChatMessage("$chatName You are in hole for longer than ${inHoleTimeout.value} ticks, enabling")
                enable()
            }
        } else {
            toggleTimer.reset()
        }
    }

    private fun inHoleCheck() = mc.player.onGround && mc.player.speed < 0.15 && SurroundUtils.checkHole(mc.player) == SurroundUtils.HoleType.OBBY

    private fun outOfHoleCheck() {
        if (autoDisable.value == AutoDisableMode.OUT_OF_HOLE) {
            if (toggleTimer.stop() > outOfHoleTimeout.value) {
                MessageSendHelper.sendChatMessage("$chatName You are out of hole for longer than ${outOfHoleTimeout.value} ticks, disabling")
                disable()
            }
        }
    }

    private fun spoofHotbar() {
        val slot = getObby()
        if (slot != -1) PlayerPacketManager.spoofHotbar(getObby())
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
            if (WorldUtils.isPlaceable(pos, true)) return true
        }
        return false
    }

    private fun centerPlayer(): Boolean {
        return if (autoCenter.value == AutoCenterMode.OFF) {
            true
        } else {
            if (disableStrafe.value) Strafe.disable()
            SurroundUtils.centerPlayer(autoCenter.value == AutoCenterMode.TP)
        }
    }

    private fun runSurround() = defaultScope.launch {
        spoofHotbar()
        WorldUtils.buildStructure(placeSpeed.value) {
            if (isEnabled && CombatManager.isOnTopPriority(this@Surround)) {
                WorldUtils.getPlaceInfo(mc.player.positionVector.toBlockPos(), SurroundUtils.surroundOffset, it, 2)
            } else {
                null
            }
        }
    }

    init {
        alwaysListening = enableInHole.value
        enableInHole.settingListener = Setting.SettingListeners {
            alwaysListening = enableInHole.value
        }
    }
}
