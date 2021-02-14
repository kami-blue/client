package org.kamiblue.client.setting.configs

import org.kamiblue.client.KamiMod
import org.kamiblue.client.setting.groups.SettingMultiGroup
import org.kamiblue.client.setting.settings.SettingRegister
import java.io.File

abstract class AbstractMultiConfig<T : Any>(
    name: String,
    protected val directoryPath: String,
    vararg groupNames: String
) : AbstractConfig<T>(name, directoryPath), IConfig, SettingRegister<T> {

    override val file: File get() = File("$directoryPath$name")

    init {
        for (groupName in groupNames) addGroup(SettingMultiGroup(groupName))
    }

    override fun save() {
        if (!file.exists()) file.mkdirs()

        for (group in subGroup.values) {
            val file = getFiles(group)
            saveToFile(group, file.first, file.second)
        }
    }

    override fun load() {
        if (!file.exists()) file.mkdirs()

        for (group in subGroup.values) {
            val file = getFiles(group)
            try {
                loadFromFile(group, file.first)
            } catch (e: Exception) {
                KamiMod.LOG.warn("Failed to load latest, loading backup.")
                loadFromFile(group, file.second)
            }
        }
    }

    /**
     * Get the file pair for a group
     *
     * @param group Group to get the file pair
     *
     * @return Pair of this group's main file to its backup file
     */
    private fun getFiles(group: SettingMultiGroup) =
        File("${file.path}/${group.name}.json") to File("${file.path}/${group.name}.bak")

}