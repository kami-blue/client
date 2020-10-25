package me.zeroeightsix.kami.setting

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.setting.config.AbstractConfig

object GenericConfig : AbstractConfig<Any>(
        "Generic",
        KamiMod.DIRECTORY
) {

    override fun <S : Setting<*>> Any.setting(setting: S): S {
        getGroupOrPut(this::class.simpleName!!).addSetting(setting)
        return setting
    }

}