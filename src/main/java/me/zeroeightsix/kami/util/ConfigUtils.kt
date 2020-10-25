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

    var inProcess = false

    fun loadAll(): Boolean {
        inProcess = true

        // Generic
        var success = loadConfig(GenericConfig)

        // Macro
        success = MacroManager.loadMacros() && success

        // Waypoint
        success = WaypointManager.loadWaypoints() && success

        // Friends
        success = FriendManager.loadFriends() && success

        // Modules
        success = loadConfig(ModuleConfig) && success

        // GUI
        //GuiConfig.clearSettings()
        //KamiMod.getInstance().guiManager = KamiGUI()
        //KamiMod.getInstance().guiManager.initializeGUI()
        //success = loadConfig(GuiConfig) && success

        inProcess = false
        return success
    }

    fun saveAll(): Boolean {
        var success = true
        inProcess = true

        // Generic
        success = saveConfig(GenericConfig) && success

        // Macro
        success = MacroManager.saveMacros() && success

        // Waypoint
        success = WaypointManager.saveWaypoints() && success

        // Friends
        success = FriendManager.saveFriends() && success

        // Modules
        success = saveConfig(ModuleConfig) && success

        // GUI
        //success = saveConfig(GuiConfig) && success

        inProcess = false
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
            KamiMod.log.info("${config.name} config loaded")
            true
        } catch (e: IOException) {
            KamiMod.log.error("Failed to load ${config.name} config", e)
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
            KamiMod.log.info("Config saved")
            true
        } catch (e: Exception) {
            KamiMod.log.error("Failed to save config!", e)
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

    fun fixEmptyJson(file: File) {
        if (!file.exists()) file.createNewFile()
        var notEmpty = false
        file.forEachLine { notEmpty = notEmpty || it.trim().isNotBlank() }

        if (!notEmpty) {
            val fileWriter = FileWriter(file)
            try {
                fileWriter.write("{}")
            } catch (exception: IOException) {
                exception.printStackTrace()
            }
            fileWriter.close()
        }
    }
}