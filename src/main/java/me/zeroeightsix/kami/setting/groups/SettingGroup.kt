package me.zeroeightsix.kami.setting.groups

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import me.zeroeightsix.kami.setting.Setting

open class SettingGroup(
        val name: String
) {

    /** Settings in this group */
    protected val subSetting = LinkedHashMap<String, Setting<*>>()


    /**
     * Get a copy of the list of settings in this group
     *
     * @return A copy of [subSetting]
     */
    fun getSettings() = subSetting.values.toList()

    /**
     * Adds a setting to this group
     *
     * @param S type of the setting
     * @param setting Setting to add
     *
     * @return [setting]
     */
    open fun <S: Setting<*>> addSetting(setting: S): S {
        subSetting[setting.name.toLowerCase()] = setting
        return setting
    }


    /**
     * Writes setting values to a [JsonObject]
     *
     * @return [JsonObject] contains all the setting values
     */
    open fun write(): JsonObject = JsonObject().apply {
        add("Name", JsonPrimitive(name))

        add("Settings", JsonObject().apply {
            for (setting in subSetting.values) {
                add(setting.name, setting.write())
            }
        })
    }

    /**
     * Read setting values from a [JsonObject]
     *
     * @param jsonObject [JsonObject] to read from
     */
    open fun read(jsonObject: JsonObject?) {
        if (jsonObject == null) return

        jsonObject.getAsJsonObject("Settings").also {
            for (setting in subSetting.values) {
                setting.read(it.get(setting.name))
            }
        }
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SettingGroup

        if (name != other.name) return false
        if (subSetting != other.subSetting) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + subSetting.hashCode()
        return result
    }

}