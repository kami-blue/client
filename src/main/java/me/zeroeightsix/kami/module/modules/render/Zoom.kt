package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.util.KamiLang 
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting

object Zoom : Module(
    name = KamiLang.get("module.modules.render.Zoom.Zoom"),
    category = Category.RENDER,
    description = KamiLang.get("module.modules.render.Zoom.ConfiguresFov"),
    showOnArray = false
) {
    private var fov = 0f
    private var sensi = 0f

    private val fovChange = setting(KamiLang.get("module.modules.render.Zoom.Fov"), 40.0f, 1.0f..180.0f, 0.5f)
    private val modifySensitivity = setting(KamiLang.get("module.modules.render.Zoom.Modifysensitivity"), true)
    private val sensitivityMultiplier = setting(KamiLang.get("module.modules.render.Zoom.Sensitivitymultiplier"), 1.0f, 0.25f..2.0f, 0.25f, { modifySensitivity.value })
    private val smoothCamera = setting(KamiLang.get("module.modules.render.Zoom.Cinematiccamera"), false)

    init {
        onEnable {
            fov = mc.gameSettings.fovSetting
            sensi = mc.gameSettings.mouseSensitivity

            mc.gameSettings.fovSetting = fovChange.value
            if (modifySensitivity.value) mc.gameSettings.mouseSensitivity = sensi * sensitivityMultiplier.value
            mc.gameSettings.smoothCamera = smoothCamera.value
        }

        onDisable {
            mc.gameSettings.fovSetting = fov
            mc.gameSettings.mouseSensitivity = sensi
            mc.gameSettings.smoothCamera = false
        }

        fovChange.listeners.add {
            if (isEnabled) mc.gameSettings.fovSetting = fovChange.value
        }
        modifySensitivity.listeners.add {
            if (isEnabled) if (modifySensitivity.value) mc.gameSettings.mouseSensitivity = sensi * sensitivityMultiplier.value
            else mc.gameSettings.mouseSensitivity = sensi
        }
        sensitivityMultiplier.listeners.add {
            if (isEnabled) mc.gameSettings.mouseSensitivity = sensi * sensitivityMultiplier.value
        }
        smoothCamera.listeners.add {
            if (isEnabled) mc.gameSettings.smoothCamera = smoothCamera.value
        }
    }
}