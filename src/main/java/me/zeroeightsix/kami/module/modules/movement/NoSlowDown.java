package me.zeroeightsix.kami.module.modules.movement;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraftforge.client.event.InputUpdateEvent;

/**
 * Created by 086 on 15/12/2017.
 * Updated by dominikaaaa on 31/03/20
 * @see me.zeroeightsix.kami.mixin.client.MixinBlockSoulSand
 * @see net.minecraft.client.entity.EntityPlayerSP#onLivingUpdate()
 */
@Module.Info(
        name = "NoSlowDown",
        category = Module.Category.MOVEMENT,
        description = "Prevents being slowed down when using an item or going through cobwebs"
)
public class NoSlowDown extends Module {
    public Setting<Boolean> soulSand = register(Settings.b("Soul Sand", true));
    public Setting<Boolean> cobweb = register(Settings.b("Cobweb", true));
    private Setting<Boolean> slime = register(Settings.b("Slime", true));
    private Setting<Boolean> allItems = register(Settings.b("All Items", false));
    private Setting<Boolean> food = register(Settings.booleanBuilder().withName("Food").withValue(true).withVisibility(v -> !allItems.getValue()).build());
    private Setting<Boolean> bow = register(Settings.booleanBuilder().withName("Bows").withValue(true).withVisibility(v -> !allItems.getValue()).build());
    private Setting<Boolean> potion = register(Settings.booleanBuilder().withName("Potions").withValue(true).withVisibility(v -> !allItems.getValue()).build());
    private Setting<Boolean> shield = register(Settings.booleanBuilder().withName("Shield").withValue(true).withVisibility(v -> !allItems.getValue()).build());

    /*
     * InputUpdateEvent is called just before the player is slowed down @see EntityPlayerSP.onLivingUpdate)
     * We'll abuse this fact, and multiply moveStrafe and moveForward by 5 to nullify the *0.2f hardcoded by Mojang.
     */
    @EventHandler
    private Listener<InputUpdateEvent> eventListener = new Listener<>(event -> {
        if (passItemCheck(mc.player.getActiveItemStack().getItem()) && !mc.player.isRiding()) {
            event.getMovementInput().moveStrafe *= 5;
            event.getMovementInput().moveForward *= 5;
        }
    });

    @Override
    public void onUpdate() {
        if (slime.getValue()) Blocks.SLIME_BLOCK.slipperiness = 0.4945f; // normal block speed 0.4945
        else Blocks.SLIME_BLOCK.slipperiness = 0.8f;
    }

    @Override
    public void onDisable() { Blocks.SLIME_BLOCK.slipperiness = 0.8f; }

    private boolean passItemCheck(Item item) {
        if (!mc.player.isHandActive()) return false;
        return allItems.getValue()
                || (food.getValue() && item instanceof ItemFood)
                || (bow.getValue() && item instanceof ItemBow)
                || (potion.getValue() && item instanceof ItemPotion)
                || (shield.getValue() && item instanceof ItemShield);
    }
}
