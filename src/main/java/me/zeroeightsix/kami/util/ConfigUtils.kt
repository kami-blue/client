package me.zeroeightsix.kami.util

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.manager.managers.FriendManager
import me.zeroeightsix.kami.manager.managers.MacroManager
import me.zeroeightsix.kami.manager.managers.WaypointManager
import me.zeroeightsix.kami.setting.GenericConfig
import me.zeroeightsix.kami.setting.GuiConfig
import me.zeroeightsix.kami.setting.ModuleConfig
import me.zeroeightsix.kami.setting.config.AbstractConfig
import java.io.File
import java.io.FileWriter
import java.io.IOException

object ConfigUtils {

    fun loadAll(): Boolean {
        var success = loadConfig(GenericConfig) // Generic
        success = MacroManager.loadMacros() && success // Macro
        success = WaypointManager.loadWaypoints() && success // Waypoint
        success = FriendManager.loadFriends() && success // Friends
        success = loadConfig(ModuleConfig) && success // Modules
        success = loadConfig(GuiConfig) && success // GUI

        return success
    }

    fun saveAll(): Boolean {
        if (!KamiMod.isInitialized()) return false

        var success = saveConfig(GenericConfig) // Generic
        success = MacroManager.saveMacros() && success // Macro
        success = WaypointManager.saveWaypoints() && success // Waypoint
        success = FriendManager.saveFriends() && success // Friends
        success = saveConfig(ModuleConfig) && success // Modules
        success = saveConfig(GuiConfig) && success // GUI

        return success
    }

    /**
     * Load configuration with try catch
     *
     * @return false if exception caught
     */
    fun loadConfig(config: AbstractConfig<*>): Boolean {
        return try {
            config.load()
            KamiMod.LOG.info("${config.name} config loaded")
            true
        } catch (e: IOException) {
            KamiMod.LOG.error("Failed to load ${config.name} config", e)
            false
        }
    }

    /**
     * Save configuration with try catch
     *
     * @return false if exception caught
     */
    fun saveConfig(config: AbstractConfig<*>): Boolean {
        return try {
            config.save()
            KamiMod.LOG.info("Config saved")
            true
        } catch (e: Exception) {
            KamiMod.LOG.error("Failed to save config!", e)
            false
        }
    }

    fun isFilenameValid(file: String): Boolean {
        return try {
            File(file).canonicalPath
            true
        } catch (e: Throwable) {
            false
        }
    }

    fun fixEmptyJson(file: File, isArray: Boolean = false) {
        if (!file.exists()) file.createNewFile()
        var notEmpty = false
        file.forEachLine { notEmpty = notEmpty || it.trim().isNotBlank() || it == "[]" || it == "{}" }

        if (!notEmpty) {
            val fileWriter = FileWriter(file)
            try {
                fileWriter.write(if (isArray) "[]" else "{}")
            } catch (exception: IOException) {
                exception.printStackTrace()
            }
            fileWriter.close()
        }
    }
}