package me.zeroeightsix.kami.setting

import me.zeroeightsix.kami.setting.settings.AbstractSetting
import java.io.File

/**
 * Setting group that can be saved to a .json file
 *
 * @param T Type to have extension function for registering setting
 */
interface IFinalGroup<T> {

    /** Main file of the config */
    val file: File

    /** Backup file of the config */
    val backup: File

    /**
     * Register a setting to this group
     *
     * @param S Type of the setting
     * @param setting Setting to register
     *
     * @return [setting]
     */
    fun <S : AbstractSetting<*>> T.setting(setting: S): S

    /**
     * Save this group to its .json file
     */
    fun save()

    /**
     * Load all setting values in from its .json file
     */
    fun load()

}