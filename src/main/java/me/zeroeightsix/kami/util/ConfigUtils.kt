package me.zeroeightsix.kami.util

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.manager.managers.FriendManager
import me.zeroeightsix.kami.manager.managers.MacroManager
import me.zeroeightsix.kami.manager.managers.UUIDManager
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
        success = UUIDManager.load() && success // UUID Cache
        success = loadConfig(ModuleConfig) && success // Modules
        success = loadConfig(GuiConfig) && success // GUI

        return success
    }

    fun saveAll(): Boolean {
        var success = saveConfig(GenericConfig) // Generic
        success = MacroManager.saveMacros() && success // Macro
        success = WaypointManager.saveWaypoints() && success // Waypoint
        success = FriendManager.saveFriends() && success // Friends
        success = UUIDManager.save() && success // UUID Cache
        success = saveConfig(ModuleConfig) && success // Modules
        success = saveConfig(GuiConfig) && success // GUI

        return success
    }

    /**
     * Load configuration with try catch
     *
     * @return false if exception caught
     */
    private fun loadConfig(config: AbstractConfig<*>): Boolean {
        return try {
            config.load()
            KamiMod.LOG.info("${config.name} config loaded")
            true
        } catch (e: Exception) {
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

    fun isPathValid(path: String): Boolean {
        return try {
            File(path).canonicalPath
            true
        } catch (e: Throwable) {
            false
        }
    }

    fun fixEmptyJson(file: File, isArray: Boolean = false) {
        var empty = false

        if (!file.exists()) {
            file.createNewFile()
            empty = true
        } else if (file.length() <= 8) {
            val string = file.readText()
            empty = string.isBlank() || string.all {
                it == '[' || it == ']' || it == '{' || it == '}' || it == ' ' || it == '\n' || it == '\r'
            }
        }

        if (empty) {
            try {
                FileWriter(file, false).use {
                    it.write(if (isArray) "[]" else "{}")
                }
            } catch (exception: IOException) {
                KamiMod.LOG.warn("Failed fixing empty json", exception)
            }
        }
    }
}