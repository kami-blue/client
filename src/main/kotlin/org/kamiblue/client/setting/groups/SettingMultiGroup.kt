package org.kamiblue.client.setting.groups

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

open class SettingMultiGroup(
    name: String
) : SettingGroup(name) {

    protected val subGroup = LinkedHashMap<String, SettingMultiGroup>()


    /* Settings */
    /**
     * Get a setting by name
     *
     * @param settingName Name of the setting
     *
     * @return Setting that matches [settingName] or null if none
     */
    fun getSetting(settingName: String) = subSetting[settingName.toLowerCase()]
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
        if (subGroup.isNotEmpty()) add("groups", JsonArray().apply {
            for (group in subGroup.values) {
                add(group.write())
            }
        })
    }


    override fun read(jsonObject: JsonObject?) {
        super.read(jsonObject)

        if (subGroup.isNotEmpty()) {
            (jsonObject?.get("groups") as? JsonArray)?.also { jsonArray ->
                for (element in jsonArray) {
                    (element as? JsonObject)?.let {
                        val name = (it.get("name") as? JsonPrimitive)?.asString ?: return@let
                        getGroup(name)?.read(it)
                    }
                }
            }
        }
    }
}