package me.zeroeightsix.kami.module.modules.client

import baritone.api.BaritoneAPI
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Setting.SettingListeners
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.event.listener

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
    private var hasRun = false

    init {
        settingList.forEach {
            it.settingListener = SettingListeners { set() }
        }

        // not triggered until in game
        listener<SafeTickEvent> {
            if (!hasRun) {
                set()
                hasRun = true
            }
        }
    }

    private fun set() {
        mc.player?.let {
            BaritoneAPI.getSettings().chatControl.value = false // enable chatControlAnyway if you want to use it
            BaritoneAPI.getSettings().allowBreak.value = allowBreak.value
            BaritoneAPI.getSettings().allowSprint.value = allowSprint.value
            BaritoneAPI.getSettings().allowPlace.value = allowPlace.value
            BaritoneAPI.getSettings().allowInventory.value = allowInventory.value
            BaritoneAPI.getSettings().freeLook.value = freeLook.value
            BaritoneAPI.getSettings().allowDownward.value = allowDownwardTunneling.value
            BaritoneAPI.getSettings().allowParkour.value = allowParkour.value
            BaritoneAPI.getSettings().allowParkourPlace.value = allowParkourPlace.value
            BaritoneAPI.getSettings().enterPortal.value = !avoidPortals.value
            BaritoneAPI.getSettings().mapArtMode.value = mapArtMode.value
            BaritoneAPI.getSettings().renderGoal.value = renderGoal.value
            BaritoneAPI.getSettings().failureTimeoutMS.value = failureTimeout.value * 1000L
            BaritoneAPI.getSettings().blockReachDistance.value = blockReachDistance.value
        }
    }
}