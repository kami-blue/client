package me.zeroeightsix.kami.setting

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.setting.GenericConfig.setting
import me.zeroeightsix.kami.setting.config.AbstractConfig
import me.zeroeightsix.kami.setting.settings.AbstractSetting
import java.io.File

internal object ModuleConfig : AbstractConfig<Module>(
        "modules",
        "${KamiMod.DIRECTORY}modules",
) {
    var currentPath by setting("CurrentPath", "default")

    override val file: File get() = File("$filePath/$currentPath.json")
    override val backup get() =  File("$filePath/$currentPath.bak")

    override fun <S : AbstractSetting<*>> Module.setting(setting: S): S {
        getGroupOrPut(name).addSetting(setting)
        return setting
    }

    override fun save() {
        super.save()
        for (module in ModuleManager.getModules()) module.destroy()
    }

}