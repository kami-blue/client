package me.zeroeightsix.kami.module.modules.client;

import baritone.api.BaritoneAPI;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.MessageSendHelper;

/**
 * Created by Dewy on the 21st of April, 2020
 */
@Module.Info(
          name = "Baritone",
          category = Module.Category.CLIENT,
          description = "Configure KAMI Blue's Baritone integration.",
          showOnArray = Module.ShowOnArray.OFF
)
public class Baritone extends Module {
    public Setting<Boolean> allowBreak = register(Settings.b("Allow Break", true));
    public Setting<Boolean> allowSprint = register(Settings.b("Allow Sprint", true));
    public Setting<Boolean> allowPlace = register(Settings.b("Allow Place", true));
    public Setting<Boolean> allowInventory = register(Settings.b("Allow Inventory", true));
    public Setting<Boolean> freeLook = register(Settings.b("Free Look", true));
    public Setting<Boolean> allowDownwardTunneling = register(Settings.b("Downward Tunneling", true));

    public Setting<Boolean> allowParkour = register(Settings.b("Allow Parkour", true));
    public Setting<Boolean> allowParkourPlace = register(Settings.b("Allow Parkour Place", true));

    public Setting<Boolean> avoidPortals = register(Settings.b("Avoid Portals", false));
    public Setting<Boolean> mapArtMode = register(Settings.b("Map Art Mode", false));

    public Setting<Boolean> renderGoal = register(Settings.b("Render Goals", true));

    @Override
    public void onDisable() {
        MessageSendHelper.sendErrorMessage("Error: The Baritone module is for configuring Baritone integration, not toggling it.");
        enable();
    }

    // ._.
    @Override
    public void onUpdate() {
        BaritoneAPI.getSettings().allowBreak.value = allowBreak.getValue();
        BaritoneAPI.getSettings().allowSprint.value = allowSprint.getValue();
        BaritoneAPI.getSettings().allowPlace.value = allowPlace.getValue();
        BaritoneAPI.getSettings().allowInventory.value = allowInventory.getValue();
        BaritoneAPI.getSettings().freeLook.value = freeLook.getValue();
        BaritoneAPI.getSettings().allowDownward.value = allowDownwardTunneling.getValue();

        BaritoneAPI.getSettings().allowParkour.value = allowParkour.getValue();
        BaritoneAPI.getSettings().allowParkourPlace.value = allowParkourPlace.getValue();

        BaritoneAPI.getSettings().enterPortal.value = !avoidPortals.getValue();
        BaritoneAPI.getSettings().mapArtMode.value = mapArtMode.getValue();

        BaritoneAPI.getSettings().renderGoal.value = renderGoal.getValue();
    }
}
