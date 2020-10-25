package me.zeroeightsix.kami.setting

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.setting.config.AbstractMultiConfig

object ModuleConfig : AbstractMultiConfig<Module>(
        "Modules",
        KamiMod.DIRECTORY,
        *Module.Category.values().map { it.categoryName }.toTypedArray()
) {

    override fun <S : Setting<*>> Module.setting(setting: S): S {
        getGroupOrPut(category.categoryName).addSetting(originalName, setting)
        return setting
    }

    override fun save() {
        super.save()
        for (module in ModuleManager.getModules()) module.destroy()
    }

}