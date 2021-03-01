package org.kamiblue.client.module.modules.movement

import net.minecraft.util.math.BlockPos
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.PlayerTravelEvent
import org.kamiblue.client.manager.managers.CombatManager
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.EntityUtils.flooredPosition
import org.kamiblue.client.util.combat.SurroundUtils
import org.kamiblue.client.util.combat.SurroundUtils.checkHole
import org.kamiblue.client.util.math.VectorUtils.toBlockPos
import org.kamiblue.client.util.threads.safeListener

@CombatManager.CombatModule
internal object Anchor : Module(
    name = "Anchor",
    description = "Automatically stops when you are above hole",
    category = Category.MOVEMENT,
    modulePriority = 10
) {
    private val vRange by setting("Range", 5, 1..10, 1)
    private val mode by setting("Mode", AnchorMode.BOTH)
    private val turnOffAfter by setting("Turn Off After", false)
    private val pitchTrigger by setting("Pitch Trigger", false)
    private val pitch by setting("Pitch", 80, -90..90, 1, { pitchTrigger })
    private val strict by setting("Strict", true)
    private var prevInHole = false

    private enum class AnchorMode {
        BOTH, BEDROCK
    }

    /**
     * Checks whether the specified block position is a hole or not
     */
    private fun SafeClientEvent.isHole(pos: BlockPos): Boolean {
        val type = checkHole(pos)
        return mode == AnchorMode.BOTH && type != SurroundUtils.HoleType.NONE ||
            mode == AnchorMode.BEDROCK && type == SurroundUtils.HoleType.BEDROCK
    }

    /**
     * Checks whether the player should stop movement or not
     */
    private fun SafeClientEvent.shouldStop(): Boolean {
        if (pitchTrigger && mc.player.rotationPitch < pitch) return false
        for (dy in 1..vRange) {
            val pos = player.flooredPosition.down(dy)
            if (!world.isAirBlock(pos))
                return false
            if (isHole(pos))
                return true
        }
        return false
    }

    init {
        onDisable {
            prevInHole = false
        }

        safeListener<PlayerTravelEvent> {
            if (isHole(player.flooredPosition) &&
                !world.isAirBlock(player.positionVector.add(0.0, -0.1, 0.0).toBlockPos())) {
                prevInHole = true
                if (turnOffAfter)
                    disable()
                return@safeListener
            }
            if (!shouldStop()) {
                prevInHole = false
                return@safeListener
            }
            if (prevInHole) return@safeListener

            SurroundUtils.centerPlayer(!strict)
            if (!strict) {
                player.motionX = 0.0
                player.motionZ = 0.0
            }
        }
    }
}