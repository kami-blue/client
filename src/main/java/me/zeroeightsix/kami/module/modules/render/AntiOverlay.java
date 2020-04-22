package me.zeroeightsix.kami.module.modules.render;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.potion.Potion;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;

/**
 * Created by Dewy on the 20th April, 2020
 */
@Module.Info(
          name = "AntiOverlay",
          description = "Prevents rendering of fire, water and block texture overlays.",
          category = Module.Category.RENDER
)
public class AntiOverlay extends Module {
    private Setting<Boolean> fire = register(Settings.booleanBuilder("Fire").withValue(true).build());
    private Setting<Boolean> water = register(Settings.booleanBuilder("Water").withValue(true).build());
    private Setting<Boolean> blocks = register(Settings.booleanBuilder("Blocks").withValue(true).build());
    public Setting<Boolean> portals = register(Settings.booleanBuilder("Portals").withValue(true).build());
    public Setting<Boolean> blindness = register(Settings.booleanBuilder("Blindness").withValue(true).build());
    public Setting<Boolean> nausea = register(Settings.booleanBuilder("Nausea").withValue(true).build());
    public Setting<Boolean> totems = register(Settings.booleanBuilder("Totems").withValue(true).build());

    @EventHandler
    public Listener<RenderBlockOverlayEvent> renderBlockOverlayEventListener = new Listener<>(event -> {
        boolean shouldCancel = false;

        if (!isEnabled()) {
            return;
        }

        switch (event.getOverlayType()) {
            case FIRE:
                if (fire.getValue()) {
                    shouldCancel = true;
                }

                break;
            case WATER:
                if (water.getValue()) {
                    shouldCancel = true;
                }

                break;
            case BLOCK:
                if (blocks.getValue()) {
                    shouldCancel = true;
                }

                break;
        }

        event.setCanceled(shouldCancel);
    });

    @Override
    public void onUpdate() {
        if (blindness.getValue()) {
            mc.player.removeActivePotionEffect(Potion.getPotionFromResourceLocation("blindness"));
        }

        if (nausea.getValue()) {
            mc.player.removeActivePotionEffect(Potion.getPotionFromResourceLocation("nausea"));
        }
    }
}
