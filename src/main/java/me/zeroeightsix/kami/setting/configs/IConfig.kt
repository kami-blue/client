package me.zeroeightsix.kami.setting.configs

import me.zeroeightsix.kami.setting.settings.SettingRegister
import java.io.File

/**
 * Setting group that can be saved to a .json file
 *
 * @param T Type to have extension function for registering setting
 */
interface IConfig<T> : SettingRegister<T> {

    /** Main file of the config */
    val file: File

    /** Backup file of the config */
    val backup: File

    /**
     * Save this group to its .json file
     */
    fun save()

    /**
     * Load all setting values in from its .json file
     */
    fun load()

}