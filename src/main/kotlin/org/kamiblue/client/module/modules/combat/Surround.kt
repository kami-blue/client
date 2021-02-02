package org.kamiblue.client.module.modules.combat

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.manager.managers.CombatManager
import org.kamiblue.client.manager.managers.PlayerPacketManager
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.module.modules.movement.Strafe
import org.kamiblue.client.util.*
import org.kamiblue.client.util.MovementUtils.speed
import org.kamiblue.client.util.WorldUtils.buildStructure
import org.kamiblue.client.util.WorldUtils.getPlaceInfo
import org.kamiblue.client.util.WorldUtils.isPlaceable
import org.kamiblue.client.util.combat.SurroundUtils
import org.kamiblue.client.util.items.firstBlock
import org.kamiblue.client.util.items.hotbarSlots
import org.kamiblue.client.util.math.VectorUtils.toBlockPos
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.threads.defaultScope
import org.kamiblue.client.util.threads.isActiveOrFalse
import org.kamiblue.client.util.threads.safeListener
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.gameevent.TickEvent

@CombatManager.CombatModule
internal object Surround : Module(
    name = "Surround",
    category = Category.COMBAT,
    description = "Surrounds you with obsidian to take less damage",
    modulePriority = 200
) {
    private val autoCenter = setting("AutoCenter", AutoCenterMode.MOTION)
    private val placeSpeed = setting("PlacesPerTick", 4f, 0.25f..5f, 0.25f)
    private val autoDisable = setting("AutoDisable", AutoDisableMode.OUT_OF_HOLE)
    private val outOfHoleTimeout = setting("OutOfHoleTimeout(t)", 10, 1..50, 5, { autoDisable.value == AutoDisableMode.OUT_OF_HOLE })
    private val enableInHole = setting("EnableInHole", false)
    private val inHoleTimeout = setting("InHoleTimeout(t)", 50, 1..100, 5, { enableInHole.value })
    private val disableStrafe = setting("DisableStrafe", true)

    enum class AutoCenterMode {
        OFF, TP, MOTION
    }

    enum class AutoDisableMode {
        ONE_TIME, OUT_OF_HOLE
    }

    private var holePos: BlockPos? = null
    private var toggleTimer = StopTimer(TimeUnit.TICKS)
    private var job: Job? = null

    override fun isActive(): Boolean {
        return isEnabled && job.isActiveOrFalse
    }

    init {
        onEnable {
            toggleTimer.reset()
        }

        onDisable {
            PlayerPacketManager.resetHotbar()
            toggleTimer.reset()
            holePos = null
        }


        safeListener<TickEvent.ClientTickEvent> {
            if (getObby() == null) return@safeListener
            if (isDisabled) {
                enableInHoleCheck()
                return@safeListener
            }

            // Following codes will not run if disabled

            // Update hole pos
            if (holePos == null || inHoleCheck()) {
                holePos = player.positionVector.toBlockPos()
            }

            // Out of hole check
            if (player.positionVector.toBlockPos() != holePos) {
                outOfHoleCheck()
                return@safeListener
            } else {
                toggleTimer.reset()
            }

            // Placeable & Centered check
            if (!canRun() || !centerPlayer()) {
                if (autoDisable.value == AutoDisableMode.ONE_TIME) disable()
                return@safeListener
            }

            // The actual job
            if (!job.isActiveOrFalse) {
                job = runSurround()
            } else if (job.isActiveOrFalse) {
                spoofHotbar()
                PlayerPacketManager.addPacket(Surround, PlayerPacketManager.PlayerPacket(rotating = false))
            } else if (isEnabled && CombatManager.isOnTopPriority(Surround)) {
                PlayerPacketManager.resetHotbar()
            }
        }
    }

    private fun SafeClientEvent.enableInHoleCheck() {
        if (enableInHole.value && inHoleCheck()) {
            if (toggleTimer.stop() > inHoleTimeout.value) {
                MessageSendHelper.sendChatMessage("$chatName You are in hole for longer than ${inHoleTimeout.value} ticks, enabling")
                enable()
            }
        } else {
            toggleTimer.reset()
        }
    }

    private fun SafeClientEvent.inHoleCheck() = player.onGround && player.speed < 0.15 && SurroundUtils.checkHole(player) == SurroundUtils.HoleType.OBBY

    private fun outOfHoleCheck() {
        if (autoDisable.value == AutoDisableMode.OUT_OF_HOLE) {
            if (toggleTimer.stop() > outOfHoleTimeout.value) {
                MessageSendHelper.sendChatMessage("$chatName You are out of hole for longer than ${outOfHoleTimeout.value} ticks, disabling")
                disable()
            }
        }
    }

    private fun SafeClientEvent.spoofHotbar() {
        getObby()?.let { PlayerPacketManager.spoofHotbar(it) }
    }

    private fun SafeClientEvent.getObby(): Int? {
        val slots = player.hotbarSlots.firstBlock(Blocks.OBSIDIAN)

        if (slots == null) { // Obsidian check
            if (isEnabled) {
                MessageSendHelper.sendChatMessage("$chatName No obsidian in hotbar, disabling!")
                disable()
            }
            return null
        }

        return slots.hotbarSlot
    }

    private fun SafeClientEvent.canRun(): Boolean {
        val playerPos = player.positionVector.toBlockPos()
        for (offset in SurroundUtils.surroundOffset) {
            val pos = playerPos.add(offset)
            if (isPlaceable(pos, true)) return true
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

    private fun SafeClientEvent.runSurround() = defaultScope.launch {
        spoofHotbar()

        buildStructure(placeSpeed.value) {
            if (isEnabled && CombatManager.isOnTopPriority(this@Surround)) {
                getPlaceInfo(player.positionVector.toBlockPos(), SurroundUtils.surroundOffset, it, 2)
            } else {
                null
            }
        }
    }

    init {
        alwaysListening = enableInHole.value
        enableInHole.listeners.add {
            alwaysListening = enableInHole.value
        }
    }
}
