package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings

/**
 * @author dominikaaaa
 * Created by dominikaaaa on 20/12/19
 * Updated by dominikaaaa on 22/12/19
 */
@Module.Info(
        name = "Zoom",
        category = Module.Category.RENDER,
        description = "Configures FOV",
        showOnArray = Module.ShowOnArray.OFF
)
class Zoom : Module() {
    private var fov = 0f
    private var sensi = 0f
    private val fovChange = register(Settings.integerBuilder("FOV").withMinimum(30).withValue(30).withMaximum(150).build())
    private val sensChange = register(Settings.floatBuilder("Sensitivity").withMinimum(0.25f).withValue(1.3f).withMaximum(2f).build())
    private val smoothCamera = register(Settings.b("Cinematic Camera", true))
    private val sens = register(Settings.b("Sensitivity", true))
    public override fun onEnable() {
        if (mc.player == null) return
        fov = mc.gameSettings.fovSetting
        sensi = mc.gameSettings.mouseSensitivity
        if (smoothCamera.value) mc.gameSettings.smoothCamera = true
    }

    public override fun onDisable() {
        mc.gameSettings.fovSetting = fov
        mc.gameSettings.mouseSensitivity = sensi
        if (smoothCamera.value) mc.gameSettings.smoothCamera = false
    }

    override fun onUpdate() {
        if (mc.player == null) return
        mc.gameSettings.fovSetting = fovChange.value.toFloat()
        mc.gameSettings.smoothCamera = smoothCamera.value
        if (sens.value) mc.gameSettings.mouseSensitivity = sensi * sensChange.value
    }
}