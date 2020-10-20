package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings

@Module.Info(
        name = "Zoom",
        category = Module.Category.RENDER,
        description = "Configures FOV",
        showOnArray = Module.ShowOnArray.OFF
)
object Zoom : Module() {
    private var fov = 0f
    private var sensi = 0f

    private val fovChange = register(Settings.floatBuilder("FOV").withRange(1.0f, 180.0f).withValue(40.0f))
    private val sens = register(Settings.b("ChangeSensitivity", true))
    private val sensChange = register(Settings.floatBuilder("SensitivityMultiplier").withValue(1.0f).withRange(0.25f, 2.0f).withStep(0.25f).withVisibility { sens.value })
    private val smoothCamera = register(Settings.b("CinematicCamera", false))

    public override fun onEnable() {
        if (mc.player == null) return
        fov = mc.gameSettings.fovSetting
        sensi = mc.gameSettings.mouseSensitivity

        mc.gameSettings.fovSetting = fovChange.value.toFloat()
        if (sens.value) mc.gameSettings.mouseSensitivity = sensi * sensChange.value
        mc.gameSettings.smoothCamera = smoothCamera.value
    }

    public override fun onDisable() {
        mc.gameSettings.fovSetting = fov
        mc.gameSettings.mouseSensitivity = sensi
        mc.gameSettings.smoothCamera = false
    }

    init {
        fovChange.settingListener = Setting.SettingListeners { if (isEnabled && mc.player != null) mc.gameSettings.fovSetting = fovChange.value.toFloat() }
        sens.settingListener = Setting.SettingListeners { if (isEnabled && mc.player != null) if (sens.value) mc.gameSettings.mouseSensitivity = sensi * sensChange.value else mc.gameSettings.mouseSensitivity = sensi }
        sensChange.settingListener = Setting.SettingListeners { if (isEnabled && mc.player != null) mc.gameSettings.mouseSensitivity = sensi * sensChange.value }
        smoothCamera.settingListener = Setting.SettingListeners { if (isEnabled && mc.player != null) mc.gameSettings.smoothCamera = smoothCamera.value }
    }
}