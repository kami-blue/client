package me.zeroeightsix.kami.module.modules.client

import baritone.api.BaritoneAPI
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.LocalPlayerUpdateEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Setting.SettingListeners
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.text.MessageSendHelper

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
class Baritone : Module() {
    private var allowBreak = register(Settings.b("AllowBreak", true))
    private var allowSprint = register(Settings.b("AllowSprint", true))
    private var allowPlace = register(Settings.b("AllowPlace", true))
    var allowInventory: Setting<Boolean> = register(Settings.b("AllowInventory", false))
    private var freeLook = register(Settings.b("FreeLook", true))
    private var allowDownwardTunneling = register(Settings.b("DownwardTunneling", true))
    private var allowParkour = register(Settings.b("AllowParkour", true))
    private var allowParkourPlace = register(Settings.b("AllowParkour Place", true))
    private var avoidPortals = register(Settings.b("AvoidPortals", false))
    private var mapArtMode = register(Settings.b("MapArtMode", false))
    private var renderGoal = register(Settings.b("RenderGoals", true))
    private var failureTimeout = register(Settings.integerBuilder("FailTimeout").withRange(1, 20).withValue(2).build())
    private var blockReachDistance = register(Settings.floatBuilder("ReachDistance").withRange(1.0f, 10.0f).withValue(4.5f).build())
    private var hasRun = register(Settings.booleanBuilder("hasRun1").withValue(false).withVisibility { false }.build())

    init {
        allowBreak.settingListener = SettingListeners { mc.player?.let { BaritoneAPI.getSettings().allowBreak.value = allowBreak.value } }
        allowSprint.settingListener = SettingListeners { mc.player?.let { BaritoneAPI.getSettings().allowSprint.value = allowSprint.value } }
        allowPlace.settingListener = SettingListeners { mc.player?.let { BaritoneAPI.getSettings().allowPlace.value = allowPlace.value } }
        allowInventory.settingListener = SettingListeners { mc.player?.let { BaritoneAPI.getSettings().allowInventory.value = allowInventory.value } }
        freeLook.settingListener = SettingListeners { mc.player?.let { BaritoneAPI.getSettings().freeLook.value = freeLook.value } }
        allowDownwardTunneling.settingListener = SettingListeners { mc.player?.let { BaritoneAPI.getSettings().allowDownward.value = allowDownwardTunneling.value } }
        allowParkour.settingListener = SettingListeners { mc.player?.let { BaritoneAPI.getSettings().allowParkour.value = allowParkour.value } }
        allowParkourPlace.settingListener = SettingListeners { mc.player?.let { BaritoneAPI.getSettings().allowParkourPlace.value = allowParkourPlace.value } }
        avoidPortals.settingListener = SettingListeners { mc.player?.let { BaritoneAPI.getSettings().enterPortal.value = !avoidPortals.value } }
        mapArtMode.settingListener = SettingListeners { mc.player?.let { BaritoneAPI.getSettings().mapArtMode.value = mapArtMode.value } }
        renderGoal.settingListener = SettingListeners { mc.player?.let { BaritoneAPI.getSettings().renderGoal.value = renderGoal.value } }
        failureTimeout.settingListener = SettingListeners { mc.player?.let { BaritoneAPI.getSettings().failureTimeoutMS.value = failureTimeout.value * 1000L } }
        blockReachDistance.settingListener = SettingListeners { mc.player?.let { BaritoneAPI.getSettings().blockReachDistance.value = blockReachDistance.value } }
    }

    @EventHandler // this is done because on first init the settings won't change if null
    var localPlayerUpdateEvent = Listener(EventHook { event: LocalPlayerUpdateEvent? ->
        if (!hasRun.value && mc.player != null) { // you can use a setting for this and only run it once because then it'll be updated in game, we're only worried about default settings
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
            hasRun.value = true
        }
    })
}