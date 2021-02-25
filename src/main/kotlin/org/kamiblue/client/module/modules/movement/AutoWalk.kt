package org.kamiblue.client.module.modules.movement

import baritone.api.pathing.goals.GoalXZ
import net.minecraft.util.MovementInputFromOptions
import net.minecraftforge.client.event.InputUpdateEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.BaritoneCommandEvent
import org.kamiblue.client.event.events.ConnectionEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.module.modules.player.LagNotifier
import org.kamiblue.client.util.BaritoneUtils
import org.kamiblue.client.util.TickTimer
import org.kamiblue.client.util.TimeUnit
import org.kamiblue.client.util.math.Direction
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.threads.runSafe
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.commons.extension.floorToInt
import org.kamiblue.commons.interfaces.DisplayEnum
import org.kamiblue.event.listener.listener

internal object AutoWalk : Module(
    name = "AutoWalk",
    category = Category.MOVEMENT,
    description = "Automatically walks somewhere"
) {
    private val mode = setting("Direction", AutoWalkMode.BARITONE)
    private val disableOnDisconnect by setting("Disable On Disconnect", true)

    private enum class AutoWalkMode(override val displayName: String) : DisplayEnum {
        FORWARD("Forward"),
        BACKWARD("Backward"),
        BARITONE("Baritone")
    }

    val baritoneWalk get() = isEnabled && mode.value == AutoWalkMode.BARITONE

    private const val border = 30000000
    private val messageTimer = TickTimer(TimeUnit.SECONDS)
    var direction = Direction.NORTH; private set

    override fun isActive(): Boolean {
        return isEnabled && (mode.value != AutoWalkMode.BARITONE || BaritoneUtils.isActive || BaritoneUtils.isPathing)
    }

    override fun getHudInfo(): String {
        return if (mode.value == AutoWalkMode.BARITONE && (BaritoneUtils.isActive || BaritoneUtils.isPathing)) {
            direction.displayName
        } else {
            mode.value.displayName
        }
    }

    init {
        onDisable {
            if (mode.value == AutoWalkMode.BARITONE) BaritoneUtils.cancelEverything()
        }

        listener<BaritoneCommandEvent> {
            if (it.command.contains("cancel")) {
                disable()
            }
        }

        listener<ConnectionEvent.Disconnect> {
            if (disableOnDisconnect) disable()
        }

        listener<InputUpdateEvent>(6969) {
            if (LagNotifier.paused && LagNotifier.pauseAutoWalk) return@listener

            if (it.movementInput !is MovementInputFromOptions) return@listener

            when (mode.value) {
                AutoWalkMode.FORWARD -> {
                    it.movementInput.moveForward = 1.0f
                }
                AutoWalkMode.BACKWARD -> {
                    it.movementInput.moveForward = -1.0f
                }
                else -> {
                    // Baritone mode
                }
            }
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (mode.value == AutoWalkMode.BARITONE && !checkBaritoneElytra() && !isActive()) {
                startPathing()
            }
        }
    }

    private fun SafeClientEvent.startPathing() {
        if (!world.isChunkGeneratedAt(player.chunkCoordX, player.chunkCoordZ)) return

        direction = Direction.fromEntity(player)
        val x = player.posX.floorToInt() + direction.directionVec.x * border
        val z = player.posZ.floorToInt() + direction.directionVec.z * border

        BaritoneUtils.cancelEverything()
        BaritoneUtils.primary?.customGoalProcess?.setGoalAndPath(GoalXZ(x, z))
    }

    private fun checkBaritoneElytra() = mc.player?.let {
        if (it.isElytraFlying && messageTimer.tick(10L)) {
            MessageSendHelper.sendErrorMessage("$chatName Baritone mode isn't currently compatible with Elytra flying!" +
                " Choose a different mode if you want to use AutoWalk while Elytra flying")
        }
        it.isElytraFlying
    } ?: true

    init {
        mode.listeners.add {
            if (isDisabled || mc.player == null) return@add
            if (mode.value == AutoWalkMode.BARITONE) {
                if (!checkBaritoneElytra()) {
                    runSafe { startPathing() }
                }
            } else {
                BaritoneUtils.cancelEverything()
            }
        }
    }
}