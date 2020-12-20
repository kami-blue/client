package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.event.events.BaritoneSettingsInitEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Setting.SettingListeners
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BaritoneUtils
import org.kamiblue.event.listener.listener

/**
 * Created by Dewy on the 21st of April, 2020
 */
@Module.Info(
        name = "Baritone",
        category = Module.Category.CLIENT,
        description = "Configures Baritone settings",
        showOnArray = Module.ShowOnArray.OFF,
        alwaysEnabled = true
)
object Baritone : Module() {
    private val allowBreak = register(Settings.b("AllowBreak", true))
    private val allowSprint = register(Settings.b("AllowSprint", true))
    private val allowPlace = register(Settings.b("AllowPlace", true))
    val allowInventory: Setting<Boolean> = register(Settings.b("AllowInventory", false))
    private val freeLook = register(Settings.b("FreeLook", true))
    private val allowDownwardTunneling = register(Settings.b("DownwardTunneling", true))
    private val allowParkour = register(Settings.b("AllowParkour", true))
    private val allowParkourPlace = register(Settings.b("AllowParkour Place", true))
    private val avoidPortals = register(Settings.b("AvoidPortals", false))
    private val mapArtMode = register(Settings.b("MapArtMode", false))
    private val renderGoal = register(Settings.b("RenderGoals", true))
    private val failureTimeout = register(Settings.integerBuilder("FailTimeout").withRange(1, 20).withValue(2))
    private val blockReachDistance = register(Settings.floatBuilder("ReachDistance").withRange(1.0f, 10.0f).withValue(4.5f))

    init {
        val listener = SettingListeners { sync() }
        settingList.forEach {
            it.settingListener = listener
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