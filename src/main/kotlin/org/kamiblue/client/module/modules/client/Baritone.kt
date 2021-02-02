package org.kamiblue.client.module.modules.client

import org.kamiblue.client.event.events.BaritoneSettingsInitEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.BaritoneUtils
import org.kamiblue.event.listener.listener

/**
 * Created by Dewy on the 21st of April, 2020
 */
internal object Baritone : Module(
    name = "Baritone",
    category = Category.CLIENT,
    description = "Configures Baritone settings",
    showOnArray = false,
    alwaysEnabled = true
) {
    private val allowBreak = setting("AllowBreak", true)
    private val allowSprint = setting("AllowSprint", true)
    private val allowPlace = setting("AllowPlace", true)
    val allowInventory = setting("AllowInventory", false)
    private val freeLook = setting("FreeLook", true)
    private val allowDownwardTunneling = setting("DownwardTunneling", true)
    private val allowParkour = setting("AllowParkour", true)
    private val allowParkourPlace = setting("AllowParkourPlace", true)
    private val avoidPortals = setting("AvoidPortals", false)
    private val mapArtMode = setting("MapArtMode", false)
    private val renderGoal = setting("RenderGoals", true)
    private val failureTimeout = setting("FailTimeout", 2, 1..20, 1)
    private val blockReachDistance = setting("ReachDistance", 4.5f, 1.0f..10.0f, 0.5f)

    init {
        settingList.forEach {
            it.listeners.add { sync() }
        }

        listener<BaritoneSettingsInitEvent> {
            sync()
        }
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
            it.mapArtMode.value = mapArtMode.value
            it.renderGoal.value = renderGoal.value
            it.failureTimeoutMS.value = failureTimeout.value * 1000L
            it.blockReachDistance.value = blockReachDistance.value
        }
    }
}