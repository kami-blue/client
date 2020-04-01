package me.zeroeightsix.kami.module.modules.movement;

import me.zeroeightsix.kami.module.Module;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;

/**
 * Created by 086 on 23/08/2017.
 * Updated by S-B99 on 06/03/20
 */
@Module.Info(name = "Sprint", description = "Automatically makes the player sprint", category = Module.Category.MOVEMENT, showOnArray = Module.ShowOnArray.OFF)
public class Sprint extends Module {

    @Override
    public void onUpdate() {
        if (mc.player == null) return;
        if (MODULE_MANAGER.getModule(ElytraFlight.class).isEnabled() && (mc.player.isElytraFlying() || mc.player.capabilities.isFlying)) return;
        try {
            if (!mc.player.collidedHorizontally && mc.player.moveForward > 0)
                mc.player.setSprinting(true);
            else
                mc.player.setSprinting(false);
        } catch (Exception ignored) { }
    }
}
