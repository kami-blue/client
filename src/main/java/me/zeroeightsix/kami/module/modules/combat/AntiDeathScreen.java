package me.zeroeightsix.kami.module.modules.combat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.GuiScreenEvent;
import me.zeroeightsix.kami.module.Module;
import net.minecraft.client.gui.GuiGameOver;

/**
 * Created by dominikaaaa on 30/11/19
 */
@Module.Info(
        name = "AntiDeathScreen",
        description = "Fixes random death screen glitches",
        category = Module.Category.COMBAT
)
public class AntiDeathScreen extends Module {

    @EventHandler
    public Listener<GuiScreenEvent.Displayed> listener = new Listener<>(event -> {

        if (!(event.getScreen() instanceof GuiGameOver)) {
            return;
        }

        if (mc.player.getHealth() > 0) {
            mc.player.respawnPlayer();
            mc.displayGuiScreen(null);
        }

    });

}
