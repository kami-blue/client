package me.zeroeightsix.kami.module.modules.render;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;

/**
 * @author dominikaaaa
 * Updated by dominikaaaa on 27/12/19
 */
@Module.Info(
        name = "CleanGUI",
        category = Module.Category.RENDER,
        showOnArray = Module.ShowOnArray.OFF,
        description = "Modifies parts of the GUI to be transparent"
)
public class CleanGUI extends Module {
    public Setting<Boolean> inventoryGlobal = register(Settings.b("Inventory", true));
    public static Setting<Boolean> chatGlobal = Settings.b("Chat", false);

    private static CleanGUI INSTANCE = new CleanGUI();

    public CleanGUI() {
        INSTANCE = this;
        register(chatGlobal);
    }

    public static boolean enabled() {
        return INSTANCE.isEnabled();
    }
}
