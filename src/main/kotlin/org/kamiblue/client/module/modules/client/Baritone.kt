package org.kamiblue.client.module.modules.client

import net.minecraft.util.math.BlockPos
import org.kamiblue.client.event.events.BaritoneSettingsInitEvent
import org.kamiblue.client.event.events.RenderRadarEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.BaritoneUtils
import org.kamiblue.client.util.color.ColorHolder
import org.kamiblue.client.util.graphics.RenderUtils2D
import org.kamiblue.client.util.math.Vec2d
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.event.listener.listener

internal object Baritone : Module(
    name = "Baritone",
    category = Category.CLIENT,
    description = "Configures Baritone settings",
    showOnArray = false,
    alwaysEnabled = true
) {
    private val showOnRadar by setting("Show Path on Radar", true, description = "Show the current path on radar.")
    private val color by setting("Path Color", ColorHolder(32, 250, 32), visibility = { showOnRadar })
    private val allowBreak = setting("Allow Break", true)
    private val allowSprint = setting("Allow Sprint", true)
    private val allowPlace = setting("Allow Place", true)
    private val allowInventory = setting("Allow Inventory", false)
    private val freeLook = setting("Free Look", true)
    private val allowDownwardTunneling = setting("Downward Tunneling", true)
    private val allowParkour = setting("Allow Parkour", true)
    private val allowParkourPlace = setting("Allow Parkour Place", true)
    private val avoidPortals = setting("Avoid Portals", false)
    private val renderGoal = setting("Render Goals", true)
    private val failureTimeout = setting("Fail Timeout", 2, 1..20, 1)
    private val blockReachDistance = setting("Reach Distance", 4.5f, 1.0f..10.0f, 0.5f)

    init {
        settingList.forEach {
            it.listeners.add { sync() }
        }

        listener<BaritoneSettingsInitEvent> {
            sync()
        }

        safeListener<RenderRadarEvent> {
            if (!showOnRadar || !BaritoneUtils.isPathing) return@safeListener

            val path = BaritoneUtils.primary?.pathingBehavior?.path ?: return@safeListener

            if (!path.isPresent) return@safeListener

            val playerOffset = Vec2d(player.position.x.toDouble(), player.position.z.toDouble())

            for (movement in path.get().movements()) {
                val positionFrom = getPos(movement.src, playerOffset, it.scale)
                val positionTo = getPos(movement.dest, playerOffset, it.scale)

                if (positionFrom.length() < it.radius && positionTo.length() < it.radius) {
                    RenderUtils2D.drawLine(it.vertexHelper, positionFrom, positionTo, color = color, lineWidth = 3f)
                }
            }
        }
    }

    private fun getPos(blockPos: BlockPos, playerOffset: Vec2d, scale: Float): Vec2d {
        return Vec2d(blockPos.x.toDouble(), blockPos.z.toDouble()).minus(playerOffset).div(scale.toDouble())
    }

    private fun sync() {
        BaritoneUtils.settings?.let {
            it.chatControl.value = false // enable chatControlAnyway if you want to use it
            it.allowBreak.value = allowBreak.value
            it.allowSprint.value = allowSprint.value
            it.allowPlace.value = allowPlace.value
            it.allowInventory.value = allowInventory.value
            it.freeLook.value = freeLook.value
            it.allowDownward.value = allowDownwardTunneling.value
            it.allowParkour.value = allowParkour.value
            it.allowParkourPlace.value = allowParkourPlace.value
            it.enterPortal.value = !avoidPortals.value
            it.renderGoal.value = renderGoal.value
            it.failureTimeoutMS.value = failureTimeout.value * 1000L
            it.blockReachDistance.value = blockReachDistance.value
        }
    }
}
