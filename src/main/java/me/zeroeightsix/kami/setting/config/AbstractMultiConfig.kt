package me.zeroeightsix.kami.setting.config

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.setting.IFinalGroup
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.groups.SettingMultiGroup
import java.io.File

abstract class AbstractMultiConfig<T>(
        name: String,
        protected val directoryPath: String,
        vararg groupNames: String
) : AbstractConfig<T>(name, directoryPath), IFinalGroup<T> {

    override val file: File get() = File("$directoryPath$name")

    init {
        for (groupName in groupNames) addGroup(SettingMultiGroup(groupName))
    }

    override fun addSetting(setting: Setting<*>) {}

    override fun save() {
        if (!file.exists()) file.mkdir()

        for ((name, group) in subGroup) {
            val file = getFiles(group)
            saveToFile(group, file.first, file.second)
        }
    }

    override fun load() {
        if (!file.exists()) file.mkdir()

        for ((name, group) in subGroup) {
            val file = getFiles(group)
            try {
                loadFromFile(group, file.first)
            } catch (e: Exception) {
                KamiMod.log.warn("Failed to load latest, loading backup.", e)
                loadFromFile(group, file.second)
            }
        }
    }

    private fun getFiles(group: SettingMultiGroup) =
            File("$directoryPath$name/${group.name}.json") to File("$directoryPath$name/${group.name}.bak")

}