package me.zeroeightsix.kami.setting.groups

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import me.zeroeightsix.kami.setting.Setting

open class SettingMultiGroup(
        name: String
) : SettingGroup(name) {

    protected val subGroup = LinkedHashMap<String, SettingMultiGroup>()


    /* Settings */
    /**
     * Get a setting by name and type
     *
     * @param S type of the setting
     * @param settingName Name of the setting
     *
     * @return Setting that matches [settingName] and [S] or null if none
     */
    inline fun <reified S : Setting<S>> getSettingTyped(settingName: String) = `access$subSetting`[settingName.toLowerCase()] as? S

    /**
     * Get a setting by name
     *
     * @param settingName Name of the setting
     *
     * @return Setting that matches [settingName] or null if none
     */
    fun getSetting(settingName: String) = subSetting[settingName.toLowerCase()]

    /**
     * Add a setting to a group under
     *
     * @param settingGroup Group to add to
     * @param setting Setting to add
     *
     * @return [setting]
     */
    fun <S : Setting<*>> addSetting(settingGroup: SettingMultiGroup, setting: S): S {
        subGroup.getOrPut(settingGroup.name.toLowerCase(), { settingGroup }).addSetting(setting)
        return setting
    }

    /**
     * Add a setting to a group under
     *
     * @param groupName Name of the group to add to
     * @param setting Setting to add
     *
     * @return [setting]
     */
    fun <S : Setting<*>> addSetting(groupName: String, setting: S): S {
        subGroup.getOrPut(groupName.toLowerCase(), { SettingMultiGroup(groupName) }).addSetting(setting)
        return setting
    }
    /* End of settings */


    /* Groups */
    /**
     * Get a copy of the list of group in this group
     *
     * @return A copy of [subGroup]
     */
    fun getGroups() = subGroup.values.toList()

    /**
     * Get a group by name or add a group if not found
     *
     * @return The group that matches [groupName]
     */
    fun getGroupOrPut(groupName: String) = subGroup.getOrPut(groupName.toLowerCase()) { SettingMultiGroup(groupName) }


    /**
     * Get a group by name
     *
     * @return The group that matches [groupName] or null if none
     */
    fun getGroup(groupName: String) = subGroup[groupName.toLowerCase()]

    /**
     * Adds a group to this group
     *
     * @param settingGroup Group to add
     */
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