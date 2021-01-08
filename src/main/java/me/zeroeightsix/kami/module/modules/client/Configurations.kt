package me.zeroeightsix.kami.module.modules.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.module.AbstractModule
import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.setting.ConfigManager
import me.zeroeightsix.kami.setting.GenericConfig
import me.zeroeightsix.kami.setting.GuiConfig
import me.zeroeightsix.kami.setting.ModuleConfig
import me.zeroeightsix.kami.setting.configs.AbstractConfig
import me.zeroeightsix.kami.setting.configs.IConfig
import me.zeroeightsix.kami.setting.settings.impl.primitive.StringSetting
import me.zeroeightsix.kami.util.AsyncCachedValue
import me.zeroeightsix.kami.util.ConfigUtils
import me.zeroeightsix.kami.util.TimeUnit
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.text.formatValue
import me.zeroeightsix.kami.util.threads.defaultScope
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.commons.interfaces.DisplayEnum
import org.kamiblue.event.listener.listener
import java.io.File
import java.io.IOException
import java.nio.file.Paths

internal object Configurations : AbstractModule(
    name = "Configurations",
    description = "Setting up configurations of the client",
    category = Category.CLIENT,
    alwaysEnabled = true,
    config = GenericConfig
) {
    private const val defaultPreset = "default"

    val serverPreset by setting("ServerPreset", false)

    private val guiPresetSetting = setting("GuiPreset", defaultPreset)
    val guiPreset by guiPresetSetting

    private val modulePresetSetting = setting("ModulePreset", defaultPreset)
    val modulePreset by modulePresetSetting

    private var connected = false

    init {
        listener<ConnectionEvent.Connect> {
            connected = true
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (!connected) return@safeListener

            val ip = mc.currentServerData?.serverIP ?: return@safeListener
            connected = false

            if (mc.isIntegratedServerRunning) return@safeListener

            if (serverPreset) {
                ConfigType.GUI.serverPreset(ip)
                ConfigType.MODULES.serverPreset(ip)
            }
        }
    }

    private fun verifyPresetName(input: String): Boolean {
        val nameWithoutExtension = input.removeSuffix(".json")
        val nameWithExtension = "$nameWithoutExtension.json"

        return if (!ConfigUtils.isPathValid(nameWithExtension)) {
            MessageSendHelper.sendChatMessage("${formatValue(nameWithoutExtension)} is not a valid preset name")
            false
        } else {
            true
        }
    }

    private fun updatePreset(setting: StringSetting, input: String, config: IConfig) {
        if (!verifyPresetName(input)) return

        val nameWithoutExtension = input.removeSuffix(".json")
        val prev = setting.value

        try {
            ConfigManager.save(config)
            setting.value = nameWithoutExtension
            ConfigManager.save(GenericConfig)
            ConfigManager.load(config)

            MessageSendHelper.sendChatMessage("Preset set to ${formatValue(nameWithoutExtension)}!")
        } catch (e: IOException) {
            MessageSendHelper.sendChatMessage("Couldn't set preset: ${e.message}")
            KamiMod.LOG.warn("Couldn't set path!", e)

            setting.value = prev
            ConfigManager.save(GenericConfig)
        }
    }

    init {
        with({ prev : String, input: String ->
            if (verifyPresetName(input)) {
                input
            } else {
                if (verifyPresetName(prev)) {
                    prev
                } else {
                    defaultPreset
                }
            }
        }) {
            guiPresetSetting.consumers.add(this)
            modulePresetSetting.consumers.add(this)
        }
    }

    @Suppress("UNUSED")
    enum class ConfigType(
        override val displayName: String,
        override val config: AbstractConfig<out Any>,
        override val setting: StringSetting
    ) : DisplayEnum, IConfigType {
        GUI("GUI", GuiConfig, guiPresetSetting),
        MODULES("Modules", ModuleConfig, modulePresetSetting);

        override val serverPresets by AsyncCachedValue(5L, TimeUnit.SECONDS, Dispatchers.IO) {
            getJsons(config.filePath)
            { it.nameWithoutExtension.startsWith("server-") }
        }

        override val allPresets by AsyncCachedValue(5L, TimeUnit.SECONDS, Dispatchers.IO) {
            getJsons(config.filePath)
            { true }
        }

        private companion object {
            fun getJsons(path: String, filter: (File) -> Boolean): Set<String> {
                val dir = File(path)
                if (!dir.exists() || !dir.isDirectory) return emptySet()

                val files = dir.listFiles() ?: return emptySet()
                val jsonFiles = files.filter {
                    it.isFile && it.extension == ".json" && it.length() > 8L && filter(it)
                }

                return LinkedHashSet<String>().apply {
                    jsonFiles.forEach {
                        add(it.nameWithoutExtension)
                    }
                }
            }
        }
    }

    interface IConfigType : DisplayEnum {
        val config: IConfig
        val setting: StringSetting
        val serverPresets: Set<String>
        val allPresets: Set<String>

        fun reload() {
            defaultScope.launch(Dispatchers.IO) {
                var loaded = ConfigManager.load(GenericConfig)
                loaded = ConfigManager.load(config) || loaded

                if (loaded) MessageSendHelper.sendChatMessage("${formatValue(config.name)} config reloaded!")
                else MessageSendHelper.sendErrorMessage("Failed to load ${formatValue(config.name)} config!")
            }
        }

        fun save() {
            defaultScope.launch(Dispatchers.IO) {
                var saved = ConfigManager.save(GenericConfig)
                saved = ConfigManager.save(config) || saved

                if (saved) MessageSendHelper.sendChatMessage("${formatValue(config.name)} config saved!")
                else MessageSendHelper.sendErrorMessage("Failed to load ${formatValue(config.name)} config!")
            }
        }

        fun preset(name: String) {
            defaultScope.launch(Dispatchers.IO) {
                updatePreset(setting, name, config)
            }
        }

        fun printCurrentPreset() {
            val path = Paths.get("${setting.value}.json").toAbsolutePath()
            MessageSendHelper.sendChatMessage("Path to config: ${formatValue(path)}")
        }

        fun printAllPresets() {
            if (allPresets.isEmpty()) {
                MessageSendHelper.sendChatMessage("No preset for ${formatValue(displayName)} config!")
            } else {
                MessageSendHelper.sendChatMessage("List of presets: ${formatValue(serverPresets.size)}")

                allPresets.forEach {
                    val path = Paths.get("${it}.json").toAbsolutePath()
                    MessageSendHelper.sendRawChatMessage(formatValue(path))
                }
            }
        }

        fun newServerPreset(ip: String) {
            if (!serverPresetDisabledMessage()) return

            preset(convertIpToPresetName(ip))
        }

        fun serverPreset(ip: String) {
            if (!serverPresetDisabledMessage()) return

            val presetName = convertIpToPresetName(ip)

            if (serverPresets.contains(presetName)) {
                MessageSendHelper.sendChatMessage("Changing preset to ${formatValue(presetName)} for ${formatValue(displayName)} config")
                preset(presetName)
            } else {
                MessageSendHelper.sendChatMessage("No server preset found for ${formatValue(displayName)} config, using ${formatValue(defaultPreset)} preset...")
                preset(defaultPreset)
            }
        }

        fun printAllServerPreset() {
            if (!serverPresetDisabledMessage()) return

            if (serverPresets.isEmpty()) {
                MessageSendHelper.sendChatMessage(" No server preset for ${formatValue(displayName)} config!")
            } else {
                MessageSendHelper.sendChatMessage("List of server presets for ${formatValue(displayName)} config: ${formatValue(serverPresets.size)}")

                serverPresets.forEach {
                    val path = Paths.get("${it}.json").toAbsolutePath()
                    MessageSendHelper.sendRawChatMessage(formatValue(path))
                }
            }
        }

        private fun convertIpToPresetName(ip: String) = "server-" +
            ip.replace('.', '_').replace(':', '_')


        private fun serverPresetDisabledMessage() = if (!serverPreset) {
            MessageSendHelper.sendChatMessage("Server preset is not enabled, enable it in Configurations in ClickGUI")
            false
        } else {
            true
        }
    }
}