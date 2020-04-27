package me.zeroeightsix.kami.module.modules.combat;


import me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.module.Module;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;

// NorkenBot
@Module.Info(name = "Offhandcrystal", description = "Puts a fucking crystal in your hand dumbass", category = Module.Category.COMBAT)
public class offhandcrystal extends Module {
	private Setting<Boolean> totem_disable = register(Settings.b("Totem Disable", true));
	private Setting<Boolean> totem_safe    = register(Settings.b("Auto Safe", true));

	int count;
	boolean item = false;
	boolean move = false;

	@Override
	public void onEnable() {
		if (totem_disable.getValue()) {
			ModuleManager.getModuleByName("AutoTotem").disable();
		}
	}

	@Override
	public void onDisable() {
		if (totem_disable.getValue()) {
			ModuleManager.getModuleByName("AutoTotem").enable();
		}
	}

	@Override
	public void onUpdate() {
		if (totem_safe.getValue()) {
			if (mc.player.getHealth() < 4.0f) {
				disable();
			}
		}

		if (mc.currentScreen instanceof GuiContainer) return;

		if (item) {
			int _item = -1;
			for (int item_ = 0; item_ < 45; item_++) {
				if (mc.player.inventory.getStackInSlot(item_).isEmpty) {
					_item = item_;
					break;
				}
			}

			if (_item == -1) {
				return;
			}

			mc.playerController.windowClick(0, _item < 9 ? _item + 36 : _item, 0, ClickType.PICKUP, mc.player);
			item = false;
		}

		count = mc.player.inventory.mainInventory.stream().filter(itemStack -> itemStack.getItem() == Items.GOLDEN_APPLE).mapToInt(ItemStack::getCount).sum();

		if (mc.player.getHeldItemOffhand().getItem() != Items.end_crystal) {
			if (move) {
				mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
				move = false;
				if (!mc.player.inventory.itemStack.isEmpty()) {
					item = true;
				}

				return;
			}

			if (mc.player.inventory.itemStack.isEmpty()) {
				if (count == 0) return;
				int _item = -1;

				for (int item_ = 0; item_ < 45; item_++) {
					if (mc.player.inventory.getStackInSlot(item_).getItem() == Items.END_CRYSTAL) {
						_item = item_;
						break;
					}
				}

				if (_item == -1) {
					return;
				}

				mc.playerController.windowClick(0, _item < 9 ? _item + 36 : _item, 0, ClickType.PICKUP, mc.player);
				move = true;
			}
		}
	}
}
