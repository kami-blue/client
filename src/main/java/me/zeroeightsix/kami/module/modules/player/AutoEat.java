package me.zeroeightsix.kami.module.modules.player;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.FoodStats;

/**
 * Created by 086 on 8/04/2018.
 * Updated by polymer on 09/04/20
 * Updated by S-B99 on 09/04/20
 */
@Module.Info(name = "AutoEat", description = "Automatically eat when hungry", category = Module.Category.PLAYER)
public class AutoEat extends Module {
    private Setting<Integer> foodLevel = register(Settings.integerBuilder("Eat level").withValue(15).withMinimum(1).withMaximum(20).build());
	
    private int lastSlot = -1;
    private boolean eating = false;

    private boolean isValid(ItemStack stack, int food) {
        return (stack.getItem() instanceof ItemFood && (foodLevel.getValue()- food) >= ((ItemFood) stack.getItem()).getHealAmount(stack)) && passItemCheck(stack.getItem());
    }

    private boolean passItemCheck(Item item) {
			if (item == Items.ROTTEN_FLESH) return false;
			if (item == Items.SPIDER_EYE) return false;
		return true;
	}
    
    @Override
    public void onUpdate() {
        if (eating && !mc.player.isHandActive()) {
            if (lastSlot != -1) {
                mc.player.inventory.currentItem = lastSlot;
                lastSlot = -1;
            }
            eating = false;
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
            return;
        }
        if (eating) return;

        FoodStats stats = mc.player.getFoodStats();
        if (isValid(mc.player.getHeldItemOffhand(), stats.getFoodLevel())) {
            mc.player.setActiveHand(EnumHand.OFF_HAND);
            eating = true;
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), true);
            mc.rightClickMouse();
        } else {
            for (int i = 0; i < 9; i++) {
                if (isValid(mc.player.inventory.getStackInSlot(i), stats.getFoodLevel())) {
                    lastSlot = mc.player.inventory.currentItem;
                    mc.player.inventory.currentItem = i;
                    eating = true;
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), true);
                    mc.rightClickMouse();
                    return;
                }
            }
        }
    }
}
