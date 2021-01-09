package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.event.events.BaritoneSettingsInitEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.BaritoneUtils
import org.kamiblue.event.listener.listener

/**
 * Created by Dewy on the 21st of April, 2020
 */
object Baritone : Module(
    category = Category.CLIENT,
    showOnArray = false,
    alwaysEnabled = true
) {
    private val allowBreak = setting(getTranslationKey("AllowBreak"), true)
    private val allowSprint = setting(getTranslationKey("AllowSprint"), true)
    private val allowPlace = setting(getTranslationKey("AllowPlace"), true)
    val allowInventory = setting(getTranslationKey("AllowInventory"), false)
    private val freeLook = setting(getTranslationKey("FreeLook"), true)
    private val allowDownwardTunneling = setting(getTranslationKey("DownwardTunneling"), true)
    private val allowParkour = setting(getTranslationKey("AllowParkour"), true)
    private val allowParkourPlace = setting(getTranslationKey("AllowParkourPlace"), true)
    private val avoidPortals = setting(getTranslationKey("AvoidPortals"), false)
    private val mapArtMode = setting(getTranslationKey("MapArtMode"), false)
    private val renderGoal = setting(getTranslationKey("RenderGoals"), true)
    private val failureTimeout = setting(getTranslationKey("FailTimeout"), 2, 1..20, 1)
    private val blockReachDistance = setting(getTranslationKey("ReachDistance"), 4.5f, 1.0f..10.0f, 0.5f)

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