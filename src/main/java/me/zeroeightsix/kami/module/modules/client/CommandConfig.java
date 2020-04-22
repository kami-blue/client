package me.zeroeightsix.kami.module.modules.client;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendErrorMessage;

/**
 * @author dominikaaaa
 */
@Module.Info(
        name = "CommandConfig",
        category = Module.Category.CLIENT,
        description = "Configures PrefixChat and Alias options",
        showOnArray = Module.ShowOnArray.OFF
)
public class CommandConfig extends Module {
    public Setting<Boolean> aliasInfo = register(Settings.b("Alias Info", true));
    public Setting<Boolean> prefixChat = register(Settings.b("PrefixChat", true));
    public void onDisable() { sendDisableMessage(this.getClass()); }

    private void sendDisableMessage(Class clazz) {
        sendErrorMessage("Error: The " + MODULE_MANAGER.getModule(clazz).getName() + " module is only for configuring command options, disabling it doesn't do anything.");
        MODULE_MANAGER.getModule(clazz).enable();
    }
}
