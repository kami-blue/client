package me.zeroeightsix.kami.module.modules.hidden

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.module.modules.capes.Capes
import me.zeroeightsix.kami.module.modules.client.*
import me.zeroeightsix.kami.module.modules.misc.DiscordRPC
import me.zeroeightsix.kami.setting.Settings

/**
 * @author dominikaaaa
 * Horribly designed class for uh, running things only once.
 */
@Module.Info(name = "RunConfig",
        category = Module.Category.HIDDEN,
        showOnArray = Module.ShowOnArray.OFF,
        description = "Default manager for first runs"
)
class RunConfig : Module() {
    private val hasRunCapes = register(Settings.b("Capes", false))
    private val hasRunDiscordSettings = register(Settings.b("DiscordRPC", false))
    private val hasRunFixGui = register(Settings.b("FixGui", false))
    private val hasRunTooltips = register(Settings.b("Tooltips", false))

    public override fun onEnable() {
        ModuleManager.getModule(ActiveModules::class.java).enable()
        ModuleManager.getModule(CommandConfig::class.java).enable()
        ModuleManager.getModule(InfoOverlay::class.java).enable()
        ModuleManager.getModule(InventoryViewer::class.java).enable()
        ModuleManager.getModule(Baritone::class.java).enable()

        if (!hasRunCapes.value) {
            ModuleManager.getModule(Capes::class.java).enable()
            hasRunCapes.value = true
        }
        if (!hasRunDiscordSettings.value) {
            ModuleManager.getModule(DiscordRPC::class.java).enable()
            hasRunDiscordSettings.value = true
        }
        if (!hasRunFixGui.value) {
            ModuleManager.getModule(FixGui::class.java).enable()
            hasRunFixGui.value = true
        }
        if (!hasRunTooltips.value) {
            ModuleManager.getModule(Tooltips::class.java).enable()
            hasRunTooltips.value = true
        }
        disable()
    }
}