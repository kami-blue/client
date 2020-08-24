package me.zeroeightsix.kami.util

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.gui.kami.KamiGUI
import me.zeroeightsix.kami.gui.rgui.component.AlignedComponent
import me.zeroeightsix.kami.gui.rgui.component.Component
import me.zeroeightsix.kami.gui.rgui.component.container.use.Frame
import me.zeroeightsix.kami.gui.rgui.util.ContainerHelper
import me.zeroeightsix.kami.gui.rgui.util.Docking
import me.zeroeightsix.kami.module.MacroManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.WaypointManager
import me.zeroeightsix.kami.setting.config.Configuration
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Paths
import java.util.concurrent.Executors
import java.util.function.Consumer

object ConfigUtils {

    fun loadAll(): Boolean {
        var success = true
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
                    Thread.currentThread().name = "Config Loading Thread"
                    success = loadConfiguration() && success
                },
                Thread {
                    Thread.currentThread().name = "Gui Loading Thread"
                    KamiMod.getInstance().guiManager = KamiGUI()
                    KamiMod.getInstance().guiManager.initializeGUI()
                    KamiMod.log.info("Gui loaded")
                }
        )

        for (thread in loadingThreads) {
            thread.start()
        }

        for (thread in loadingThreads) {
            thread.join()
        }

        return success
    }

    fun saveAll(): Boolean {
        var success = true
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
                    Thread.currentThread().name = "Config Saving Thread"
                    success = saveConfiguration() && success
                }
        )

        for (thread in savingThreads) {
            thread.start()
        }

        for (thread in savingThreads) {
            thread.join()
        }

        return success
    }

    /**
     * Load configuration with try catch
     *
     * @return false if exception caught
     */
    fun loadConfiguration(): Boolean {
        KamiMod.log.info("Loading config...")
        return try {
            loadConfigurationUnsafe()
            KamiMod.log.info("Config loaded")
            true
        } catch (e: IOException) {
            KamiMod.log.error("Failed to load config! ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Save configuration with try catch
     *
     * @return false if exception caught
     */
    fun saveConfiguration(): Boolean {
        KamiMod.log.info("Saving config...")
        return try {
            saveConfigurationUnsafe()
            KamiMod.log.info("Config saved")
            true
        } catch (e: IOException) {
            KamiMod.log.error("Failed to save config! ${e.message}")
            e.printStackTrace()
            false
        }
    }

    fun getConfigName(): String? {
        val config = Paths.get("KAMIBlueLastConfig.txt")
        var kamiConfigName = KAMI_CONFIG_NAME_DEFAULT
        try {
            Files.newBufferedReader(config).use { reader ->
                if (isFilenameValid(reader.readLine())) kamiConfigName = KAMI_CONFIG_NAME_DEFAULT
            }
        } catch (e: NoSuchFileException) {
            try {
                Files.newBufferedWriter(config).use { writer ->
                    writer.write(KAMI_CONFIG_NAME_DEFAULT)
                }
            } catch (e1: IOException) {
                e1.printStackTrace()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return kamiConfigName
    }

    fun isFilenameValid(file: String?): Boolean {
        return try {
            File(file!!).canonicalPath
            true
        } catch (e: Throwable) {
            false
        }
    }

    @Throws(IOException::class)
    private fun loadConfigurationUnsafe() {
        val kamiConfigName = getConfigName()!!
        val kamiConfig = Paths.get(kamiConfigName)
        if (!Files.exists(kamiConfig)) return
        Configuration.loadConfiguration(kamiConfig)
        val gui = KamiMod.getInstance().guiStateSetting.value
        for ((key, value) in gui.entrySet()) {
            val optional = KamiMod.getInstance().guiManager.children.stream()
                    .filter { component: Component? -> component is Frame }
                    .filter { component: Component -> (component as Frame).title == key }
                    .findFirst()
            if (optional.isPresent) {
                val `object` = value.asJsonObject
                val frame = optional.get() as Frame
                frame.x = `object`["x"].asInt
                frame.y = `object`["y"].asInt
                val docking = Docking.values()[`object`["docking"].asInt]
                if (docking.isLeft) ContainerHelper.setAlignment(frame, AlignedComponent.Alignment.LEFT) else if (docking.isRight) ContainerHelper.setAlignment(frame, AlignedComponent.Alignment.RIGHT) else if (docking.isCenterVertical) ContainerHelper.setAlignment(frame, AlignedComponent.Alignment.CENTER)
                frame.docking = docking
                frame.isMinimized = `object`["minimized"].asBoolean
                frame.isPinned = `object`["pinned"].asBoolean
            } else {
                System.err.println("Found GUI config entry for $key, but found no frame with that name")
            }
        }
        KamiMod.getInstance().getGuiManager().children.stream()
                .filter { component: Component -> component is Frame && component.isPinnable && component.isVisible() }
                .forEach { component: Component -> component.opacity = 0f }
    }

    @Throws(IOException::class)
    private fun saveConfigurationUnsafe() {
        val `object` = JsonObject()
        KamiMod.getInstance().guiManager.children.stream()
                .filter { component: Component? -> component is Frame }
                .map { component: Component? -> component as Frame? }
                .forEach { frame ->
                    val frameObject = JsonObject()
                    frameObject.add("x", JsonPrimitive(frame!!.x))
                    frameObject.add("y", JsonPrimitive(frame.y))
                    frameObject.add("docking", JsonPrimitive(listOf(*Docking.values()).indexOf(frame.docking)))
                    frameObject.add("minimized", JsonPrimitive(frame.isMinimized))
                    frameObject.add("pinned", JsonPrimitive(frame.isPinned))
                    `object`.add(frame.title, frameObject)
                }
        KamiMod.getInstance().guiStateSetting.value = `object`
        val outputFile = Paths.get(getConfigName()!!)
        if (!Files.exists(outputFile)) Files.createFile(outputFile)
        Configuration.saveConfiguration(outputFile)
        KamiMod.MODULE_MANAGER.modules.forEach(Consumer { obj: Module ->
            obj.destroy()
        })
    }

    private const val KAMI_CONFIG_NAME_DEFAULT = "KAMIBlueConfig.json"
}