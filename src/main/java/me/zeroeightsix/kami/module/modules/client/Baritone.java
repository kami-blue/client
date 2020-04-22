package me.zeroeightsix.kami.module.modules.client;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.util.MessageSendHelper;

@Module.Info(
          name = "Baritone",
          category = Module.Category.CLIENT,
          description = "Configure KAMI Blue's Baritone integration.",
          showOnArray = Module.ShowOnArray.OFF
)
public class Baritone extends Module {

    @Override
    protected void onDisable() {
        MessageSendHelper.sendErrorMessage("Error: The Baritone module is for configuring Baritone integration, not toggling it.");
        enable();
    }
}
