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
import me.zeroeightsix.kami.manager.managers.FriendManager
import me.zeroeightsix.kami.manager.managers.MacroManager
import me.zeroeightsix.kami.manager.managers.WaypointManager
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.setting.config.Configuration
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Paths

object ConfigUtils {

    fun loadAll(): Boolean {
        var success = MacroManager.loadMacros()

        success = WaypointManager.loadWaypoints() && success

        success = FriendManager.loadFriends() && success

        KamiMod.INSTANCE.guiManager = KamiGUI()
        KamiMod.INSTANCE.guiManager.initializeGUI()
        KamiMod.LOG.info("Gui loaded")

        success = loadConfiguration() && success

        return success
    }

    fun saveAll(): Boolean {
        var success = MacroManager.saveMacros()

        success = WaypointManager.saveWaypoints() && success

        success = FriendManager.saveFriends() && success

        success = saveConfiguration() && success

        return success
    }

    /**
     * Load configuration with try catch
     *
     * @return false if exception caught
     */
    fun loadConfiguration(): Boolean {
        return try {
            loadConfigurationUnsafe()
            KamiMod.LOG.info("Config loaded")
            true
        } catch (e: IOException) {
            KamiMod.LOG.error("Failed to load config! ${e.message}")
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
        return try {
            saveConfigurationUnsafe()
            KamiMod.LOG.info("Config saved")
            true
        } catch (e: IOException) {
            KamiMod.LOG.error("Failed to save config! ${e.message}")
            e.printStackTrace()
            false
        }
    }

    fun getConfigName(): String {
        val config = Paths.get("KAMIBlueLastConfig.txt")
        var kamiConfigName = KAMI_CONFIG_NAME_DEFAULT
        try {
            Files.newBufferedReader(config).use { reader ->
                val line = reader.readLine()
                if (isFilenameValid(line)) kamiConfigName = line
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

    @Throws(IOException::class)
    private fun loadConfigurationUnsafe() {
        val kamiConfigName = getConfigName()
        val kamiConfig = Paths.get(kamiConfigName)
        if (!Files.exists(kamiConfig)) return
        Configuration.loadConfiguration(kamiConfig)
        val gui = KamiMod.INSTANCE.guiStateSetting.value
        for ((key, value) in gui.entrySet()) {
            val optional = KamiMod.INSTANCE.guiManager.children.stream()
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
        for (component in KamiMod.INSTANCE.guiManager.children) {
            if (component !is Frame) continue
            if (!component.isPinnable || !component.isVisible) continue
            component.opacity = 0f
        }
    }

    @Throws(IOException::class)
    private fun saveConfigurationUnsafe() {
        val jsonObject = JsonObject()
        KamiMod.INSTANCE.guiManager.children.stream()
                .filter { component: Component? -> component is Frame }
                .map { component: Component? -> component as Frame? }
                .forEach { frame ->
                    val frameObject = JsonObject()
                    frameObject.add("x", JsonPrimitive(frame!!.x))
                    frameObject.add("y", JsonPrimitive(frame.y))
                    frameObject.add("docking", JsonPrimitive(listOf(*Docking.values()).indexOf(frame.docking)))
                    frameObject.add("minimized", JsonPrimitive(frame.isMinimized))
                    frameObject.add("pinned", JsonPrimitive(frame.isPinned))
                    jsonObject.add(frame.title, frameObject)
                }
        KamiMod.INSTANCE.guiStateSetting.value = jsonObject
        val outputFile = Paths.get(getConfigName())
        if (!Files.exists(outputFile)) Files.createFile(outputFile)
        Configuration.saveConfiguration(outputFile)
        for (module in ModuleManager.getModules()) {
            module.destroy()
        }
    }

    private const val KAMI_CONFIG_NAME_DEFAULT = "KAMIBlueConfig.json"
}