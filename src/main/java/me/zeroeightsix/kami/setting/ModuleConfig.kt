package me.zeroeightsix.kami.setting

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.setting.GenericConfig.setting
import me.zeroeightsix.kami.setting.config.AbstractMultiConfig
import java.io.File

object ModuleConfig : AbstractMultiConfig<Module>(
        "Modules",
        KamiMod.DIRECTORY,
        *Module.Category.values().map { it.categoryName }.toTypedArray()
) {
    val currentPath = setting("CurrentPath", "default")
    override val file: File get() = File("$directoryPath$name/$currentPath")

    override fun <S : Setting<*>> Module.setting(setting: S): S {
        getGroupOrPut(category.categoryName).addSetting(name, setting)
        return setting
    }

    override fun save() {
        super.save()
        for (module in ModuleManager.getModules()) module.destroy()
    }

}