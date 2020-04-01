package me.zeroeightsix.kami.module.modules.hidden;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.modules.capes.Capes;
import me.zeroeightsix.kami.module.modules.chat.CustomChat;
import me.zeroeightsix.kami.module.modules.gui.ActiveModules;
import me.zeroeightsix.kami.module.modules.gui.CommandConfig;
import me.zeroeightsix.kami.module.modules.gui.InfoOverlay;
import me.zeroeightsix.kami.module.modules.gui.InventoryViewer;
import me.zeroeightsix.kami.module.modules.misc.DiscordSettings;
import me.zeroeightsix.kami.module.modules.render.TabFriends;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;

/**
 * @author S-B99
 * Horribly designed class for uh, running things only once.
 */
@Module.Info(name = "Hidden:RunConfig", category = Module.Category.HIDDEN, showOnArray = Module.ShowOnArray.OFF, description = "Default manager for first runs")
public class RunConfig extends Module {
    private Setting<Boolean> hasRunCapes = register(Settings.b("Capes", false));
    private Setting<Boolean> hasRunDiscordSettings = register(Settings.b("DiscordSettings", false));
    private Setting<Boolean> hasRunFixGui = register(Settings.b("FixGui", false));
    private Setting<Boolean> hasRunTabFriends = register(Settings.b("TabFriends", false));
    private Setting<Boolean> hasRunCustomChat = register(Settings.b("CustomChat", false));
    private Setting<Boolean> hasRun420 = register(Settings.b("420", false));
    private Setting<Boolean> shouldInfoMsg = register(Settings.b("420e", false));

    public void onEnable() {
        MODULE_MANAGER.getModule(ActiveModules.class).enable();
        MODULE_MANAGER.getModule(CommandConfig.class).enable();
        MODULE_MANAGER.getModule(InfoOverlay.class).enable();
        MODULE_MANAGER.getModule(InventoryViewer.class).enable();

        if (!hasRunCapes.getValue()) {
            MODULE_MANAGER.getModule(Capes.class).enable();
            hasRunCapes.setValue(true);
        }
        if (!hasRunDiscordSettings.getValue()) {
            MODULE_MANAGER.getModule(DiscordSettings.class).enable();
            hasRunDiscordSettings.setValue(true);
        }
        if (!hasRunFixGui.getValue()) {
            MODULE_MANAGER.getModule(FixGui.class).enable();
            hasRunFixGui.setValue(true);
        }
        if (!hasRunTabFriends.getValue()) {
            MODULE_MANAGER.getModule(TabFriends.class).enable();
            hasRunTabFriends.setValue(true);
        }
        if (!hasRunCustomChat.getValue()) {
            MODULE_MANAGER.getModule(CustomChat.class).enable();
            hasRunCustomChat.setValue(true);
        }
        if (!hasRun420.getValue()) {
            try {
                Desktop.getDesktop().browse(new URI("https://youtu.be/dQw4w9WgXcQ"));
                shouldInfoMsg.setValue(true);
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
            hasRun420.setValue(true);
        }
//        disable();
    }

    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;
        if (!shouldInfoMsg.getValue()) disable();
        else {
            Command.sendChatMessage("Happy April fools!");
            shouldInfoMsg.setValue(false);
            disable();
        }
    }
}
