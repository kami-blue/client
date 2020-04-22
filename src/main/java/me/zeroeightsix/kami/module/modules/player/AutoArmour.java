package me.zeroeightsix.kami.module.modules.player;

import me.zeroeightsix.kami.module.Module;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

/**
 * Created by 086 on 24/01/2018.
 */
@Module.Info(
        name = "AutoArmour",
        category = Module.Category.PLAYER,
        description = "Automatically equips armour"
)
public class AutoArmour extends Module {

    @Override
    public void onUpdate() {
        if (mc.player.ticksExisted % 2 == 0) return;
        // check screen
        if (mc.currentScreen instanceof GuiContainer
                && !(mc.currentScreen instanceof InventoryEffectRenderer))
            return;

        // store slots and values of best armor pieces
        int[] bestArmorSlots = new int[4];
        int[] bestArmorValues = new int[4];

        // initialize with currently equipped armor
        for (int armorType = 0; armorType < 4; armorType++) {
            ItemStack oldArmor = mc.player.inventory.armorItemInSlot(armorType);

            if (oldArmor != null && oldArmor.getItem() instanceof ItemArmor)
                bestArmorValues[armorType] =
                        ((ItemArmor) oldArmor.getItem()).damageReduceAmount;

            bestArmorSlots[armorType] = -1;
        }

        // search inventory for better armor
        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(slot);

            if (stack.getCount() > 1)
                continue;

            if (stack == null || !(stack.getItem() instanceof ItemArmor))
                continue;

            ItemArmor armor = (ItemArmor) stack.getItem();
            int armorType = armor.armorType.ordinal() - 2;

            if (armorType == 2 && mc.player.inventory.armorItemInSlot(armorType).getItem().equals(Items.ELYTRA))
                continue;

            int armorValue = armor.damageReduceAmount;

            if (armorValue > bestArmorValues[armorType]) {
                bestArmorSlots[armorType] = slot;
                bestArmorValues[armorType] = armorValue;
            }
        }

        // equip better armor
        for (int armorType = 0; armorType < 4; armorType++) {
            // check if better armor was found
            int slot = bestArmorSlots[armorType];
            if (slot == -1)
                continue;

            // check if armor can be swapped
            // needs 1 free slot where it can put the old armor
            ItemStack oldArmor = mc.player.inventory.armorItemInSlot(armorType);
            if (oldArmor == null || oldArmor != ItemStack.EMPTY
                    || mc.player.inventory.getFirstEmptyStack() != -1) {
                // hotbar fix
                if (slot < 9)
                    slot += 36;

                // swap armor
                mc.playerController.windowClick(0, 8 - armorType, 0,
                        ClickType.QUICK_MOVE, mc.player);
                mc.playerController.windowClick(0, slot, 0,
                        ClickType.QUICK_MOVE, mc.player);

                break;
            }
        }

    }
}
