package me.zeroeightsix.kami.util

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.gui.kami.KamiGUI
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
        var success = loadConfig(GenericConfig)

        val loadingThreads = arrayOf(
                Thread {
                    Thread.currentThread().name = "Macro Loading Thread"
                    success = MacroManager.loadMacros() && success
                },
                Thread {
                    Thread.currentThread().name = "Waypoint Loading Thread"
                    success = WaypointManager.loadWaypoints() && success
                },
                Thread {
                    Thread.currentThread().name = "Friend Loading Thread"
                    success = FriendManager.loadFriends() && success
                },
                Thread {
                    Thread.currentThread().name = "Module Config Loading Thread"
                    success = loadConfig(ModuleConfig) && success
                },
                Thread {
                    Thread.currentThread().name = "Gui Config Loading Thread"
                    GuiConfig.clearSettings()
                    KamiMod.getInstance().guiManager = KamiGUI()
                    KamiMod.getInstance().guiManager.initializeGUI()
                    success = loadConfig(GuiConfig) && success
                }
        )

        for (thread in loadingThreads) {
            thread.start()
        }

        for (thread in loadingThreads) {
            thread.join()
        }

        inProcess = false
        return success
    }

    fun saveAll(): Boolean {
        var success = true
        inProcess = true
        val savingThreads = arrayOf(
                Thread {
                    Thread.currentThread().name = "Macro Saving Thread"
                    success = MacroManager.saveMacros() && success
                },
                Thread {
                    Thread.currentThread().name = "Waypoint Saving Thread"
                    success = WaypointManager.saveWaypoints() && success
                },
                Thread {
                    Thread.currentThread().name = "Friend Saving Thread"
                    success = FriendManager.saveFriends() && success
                },
                Thread {
                    Thread.currentThread().name = "Generic Config Loading Thread"
                    success = saveConfig(GenericConfig) && success
                },
                Thread {
                    Thread.currentThread().name = "Module Config Saving Thread"
                    success = saveConfig(ModuleConfig) && success
                },
                Thread {
                    Thread.currentThread().name = "Gui Config Saving Thread"
                    success = saveConfig(GuiConfig) && success
                }
        )

        for (thread in savingThreads) {
            thread.start()
        }

        for (thread in savingThreads) {
            thread.join()
        }

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
        } catch (e: IOException) {
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