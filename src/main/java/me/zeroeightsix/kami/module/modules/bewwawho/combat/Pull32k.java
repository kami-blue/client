package me.zeroeightsix.kami.module.modules.bewwawho.combat;

import me.zeroeightsix.kami.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.item.ItemAir;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;

@Module.Info(name = "Pull32k", category = Module.Category.COMBAT)
public class Pull32k extends Module{
	boolean foundsword = false;
    @Override
    public void onUpdate() {
    	boolean foundair = false;
        int enchantedSwordIndex = -1;
	    for (int i = 0; i < 9; i++) { 
			ItemStack itemStack = mc.player.inventory.mainInventory.get(i);
			if (EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS, itemStack) >= Short.MAX_VALUE) {
				enchantedSwordIndex = i;
				foundsword = true;
			}
			if(!foundsword) {
				enchantedSwordIndex = -1;
				foundsword = false;
			}
		}
	    if (enchantedSwordIndex != -1) {
			if (mc.player.inventory.currentItem != enchantedSwordIndex) {
				mc.player.connection.sendPacket(new CPacketHeldItemChange(enchantedSwordIndex));
				mc.player.inventory.currentItem = enchantedSwordIndex;
				mc.playerController.updateController();
			}
		}
	    if (enchantedSwordIndex == -1 && mc.player.openContainer != null && mc.player.openContainer instanceof ContainerHopper && mc.player.openContainer.inventorySlots != null && !mc.player.openContainer.inventorySlots.isEmpty()) {
			for (int i = 0; i < 5; i++) {
				if (EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS, mc.player.openContainer.inventorySlots.get(0).inventory.getStackInSlot(i)) >= Short.MAX_VALUE) {
					enchantedSwordIndex = i;
					break;
				}
			}

			if (enchantedSwordIndex == -1) {
				return;
			}
			if(enchantedSwordIndex != -1) {
				for (int i = 0; i < 9; i++) {
					ItemStack itemStack = mc.player.inventory.mainInventory.get(i);
					if (itemStack.getItem() instanceof ItemAir) {
						if (mc.player.inventory.currentItem != i) {
							mc.player.connection.sendPacket(new CPacketHeldItemChange(i));
							mc.player.inventory.currentItem = i;
							mc.playerController.updateController();
						}
						foundair = true;
						break;
					}
				}
			}
			if (foundair || checkStuff()) {
				mc.playerController.windowClick(mc.player.openContainer.windowId, enchantedSwordIndex, mc.player.inventory.currentItem, ClickType.SWAP, mc.player);
			}
		}
    }
    public boolean checkStuff() {
    	if(EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS, mc.player.inventory.getCurrentItem()) == Short.valueOf((short)5)) {
    		return true;
    	} else {
    		return false;
    	}
    }
}
