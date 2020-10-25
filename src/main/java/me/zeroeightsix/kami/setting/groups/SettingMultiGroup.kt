package me.zeroeightsix.kami.setting.groups

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import me.zeroeightsix.kami.setting.Setting

open class SettingMultiGroup(
        name: String
) : SettingGroup(name) {

    protected val subGroup = LinkedHashMap<String, SettingMultiGroup>()


    /* Settings */
    inline fun <reified T : Setting<T>> getSettingTyped(settingName: String) = `access$subSetting`[settingName.toLowerCase()] as? T

    fun getSetting(settingName: String) = subSetting[settingName.toLowerCase()]

    fun addSetting(settingGroup: SettingMultiGroup, setting: Setting<*>) {
        subGroup.getOrPut(settingGroup.name.toLowerCase(), { settingGroup }).addSetting(setting)
    }

    fun addSetting(groupName: String, setting: Setting<*>) {
        subGroup.getOrPut(groupName.toLowerCase(), { SettingMultiGroup(groupName) }).addSetting(setting)
    }
    /* End of settings */


    /* Groups */
    fun getGroups() = subGroup.values.toList()

    fun getGroupOrPut(groupName: String) = subGroup.getOrPut(groupName.toLowerCase()) { SettingMultiGroup(groupName) }

    fun getGroup(groupName: String) = subGroup[groupName.toLowerCase()]

    fun addGroup(settingGroup: SettingMultiGroup) {
        subGroup[settingGroup.name.toLowerCase()] = settingGroup
    }
    /* End of groups */


    override fun write(): JsonObject = super.write().apply {
        add("Groups", JsonArray().apply {
            for (group in subGroup.values) {
                add(group.write())
            }
        })
    }

    override fun read(jsonObject: JsonObject?) {
        if (jsonObject == null) return
        super.read(jsonObject)

        jsonObject.getAsJsonArray("Groups").also { jsonArray ->
            val map = jsonArray.toList()
                    .map { it.asJsonObject }
                    .associateBy { it.getAsJsonPrimitive("Name").asString }

            for (group in subGroup.values) {
                group.read(map[group.name])
            }
        }
    }

    @PublishedApi
    internal val `access$subSetting`: LinkedHashMap<String, Setting<*>>
        get() = subSetting
}