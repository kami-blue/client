package org.kamiblue.client.setting.configs

import org.kamiblue.client.setting.settings.AbstractSetting
import org.kamiblue.commons.interfaces.Nameable

open class NameableConfig<T : Nameable>(
    name: String,
    filePath: String
) : AbstractConfig<T>(name, filePath) {
    override fun <S : AbstractSetting<*>> T.setting(setting: S): S {
        getGroupOrPut(name).addSetting(setting)
        return setting
    }

    fun getSettings(nameable: Nameable) = getGroup(nameable.name)?.getSettings() ?: emptyList()
}
