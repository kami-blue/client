package me.zeroeightsix.kami.module.modules.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.GuiScreenEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.gui.GuiGameOver;

import static me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage;

/**
 * Created by 086 on 9/04/2018.
 * Updated 16 November 2019 by hub
 */
@Module.Info(
        name = "AutoRespawn",
        description = "Automatically respawn after dying",
        category = Module.Category.MISC
)
public class AutoRespawn extends Module {

    private Setting<Boolean> respawn = register(Settings.b("Respawn", true));
    private Setting<Boolean> deathCoords = register(Settings.b("DeathCoords", true));
    private Setting<Boolean> antiGlitchScreen = register(Settings.b("Anti Glitch Screen", true));

    @EventHandler
    public Listener<GuiScreenEvent.Displayed> listener = new Listener<>(event -> {

        if (!(event.getScreen() instanceof GuiGameOver)) {
            return;
        }

        if (deathCoords.getValue() && mc.player.getHealth() <= 0) {
            sendChatMessage(String.format("You died at x %d y %d z %d", (int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ));
        }

        if (respawn.getValue() || (antiGlitchScreen.getValue() && mc.player.getHealth() > 0)) {
            mc.player.respawnPlayer();
            mc.displayGuiScreen(null);
        }

    });

}
