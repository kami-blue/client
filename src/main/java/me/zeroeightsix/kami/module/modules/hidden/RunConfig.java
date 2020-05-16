package me.zeroeightsix.kami.module.modules.hidden;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.modules.capes.Capes;
import me.zeroeightsix.kami.module.modules.client.*;
import me.zeroeightsix.kami.module.modules.misc.DiscordRPC;
import me.zeroeightsix.kami.module.modules.render.TabFriends;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;

/**
 * @author dominikaaaa
 * Horribly designed class for uh, running things only once.
 */
@Module.Info(
        name = "RunConfig",
        category = Module.Category.HIDDEN,
        showOnArray = Module.ShowOnArray.OFF,
        description = "Default manager for first runs"
)
public class RunConfig extends Module {
    private Setting<Boolean> hasRunCapes = register(Settings.b("Capes", false));
    private Setting<Boolean> hasRunDiscordSettings = register(Settings.b("DiscordRPC", false));
    private Setting<Boolean> hasRunFixGui = register(Settings.b("FixGui", false));
    private Setting<Boolean> hasRunTooltips = register(Settings.b("Tooltips", false));

    public void onEnable() {
        MODULE_MANAGER.getModule(ActiveModules.class).enable();
        MODULE_MANAGER.getModule(CommandConfig.class).enable();
        MODULE_MANAGER.getModule(InfoOverlay.class).enable();
        MODULE_MANAGER.getModule(InventoryViewer.class).enable();
        MODULE_MANAGER.getModule(Baritone.class).enable();

        if (!hasRunCapes.getValue()) {
            MODULE_MANAGER.getModule(Capes.class).enable();
            hasRunCapes.setValue(true);
        }
        if (!hasRunDiscordSettings.getValue()) {
            MODULE_MANAGER.getModule(DiscordRPC.class).enable();
            hasRunDiscordSettings.setValue(true);
        }
        if (!hasRunFixGui.getValue()) {
            MODULE_MANAGER.getModule(FixGui.class).enable();
            hasRunFixGui.setValue(true);
        }
        if (!hasRunTooltips.getValue()) {
            MODULE_MANAGER.getModule(Tooltips.class).enable();
            hasRunTooltips.setValue(true);
        }
        disable();
    }
}
