package org.kamiblue.client.module.modules.render

import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module

internal object Zoom : Module(
    name = "Zoom",
    category = Category.RENDER,
    description = "Configures FOV",
    showOnArray = false
) {
    private var fov = 0f
    private var sensi = 0f

    private val fovChange = setting("FOV", 40.0f, 1.0f..180.0f, 0.5f)
    private val modifySensitivity = setting("Modify Sensitivity", true)
    private val sensitivityMultiplier = setting("Sensitivity Multiplier", 1.0f, 0.25f..2.0f, 0.25f, { modifySensitivity.value })
    private val smoothCamera = setting("Cinematic Camera", false)

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