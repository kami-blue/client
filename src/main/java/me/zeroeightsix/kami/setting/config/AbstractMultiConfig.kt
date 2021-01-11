package me.zeroeightsix.kami.setting.config

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.setting.IFinalGroup
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