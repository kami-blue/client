package me.zeroeightsix.kami.module.modules.experimental;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;

/**
 * @author polymer
 * Created by polymer on 21/02/20
 * Update by S-B99 on 21/02/20
 */
@Module.Info(name = "OffhandGap", category = Module.Category.EXPERIMENTAL, description = "Holds a God apple when right clicking your sword!")
public class OffhandGap extends Module {
	private Setting<Double> minHealth = register(Settings.doubleBuilder("Disable health").withMinimum(0.0).withValue(6.0).withMaximum(20.0).build());
	
	int gaps = -1;
	boolean wasEnabled = false;
	
	void moveToOffhand(int slot) {
		if (mc.player.getHeldItemOffhand().getItem() != Items.GOLDEN_APPLE) {
			mc.playerController.windowClick(0, slot < 9 ? slot + 36 : slot, 0, ClickType.PICKUP, mc.player);
			mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
		}
	}
	void moveFromOffhand(int slot) {
		if (mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE) {
			mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
			mc.playerController.windowClick(0, slot, 0, ClickType.PICKUP, mc.player);
		}
	}
	
	@EventHandler
	private Listener<PacketEvent.Send> sendListener = new Listener<>(e ->{
		if (e.getPacket() instanceof CPacketPlayerTryUseItem) {
			if (mc.player.getHeldItemMainhand().getItem() == Items.DIAMOND_SWORD) {
				if (ModuleManager.isModuleEnabled("AutoTotem") && mc.player.getHealth() >= minHealth.getValue()) {
					wasEnabled = true;
					ModuleManager.getModuleByName("AutoTotem").disable();
				}
				moveToOffhand(gaps);
			} 
		}
		try {
			if (wasEnabled = !ModuleManager.isModuleEnabled("AutoTotem") && mc.player.getHeldItemMainhand().getItem() != Items.DIAMOND_SWORD && mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE) {
				moveFromOffhand(gaps);
				ModuleManager.getModuleByName("AutoTotem").enable();
			}
		}
		catch (NullPointerException npe) {
			npe.printStackTrace();
		}
	});
	
	@Override
	public void onUpdate() {
		if (mc.player == null) return;
		if (mc.player.getHeldItemOffhand().getItem() != Items.GOLDEN_APPLE) {	
			for (int i = 0; i < 45; i++)
		 		if (mc.player.inventory.getStackInSlot(i).getItem() == Items.GOLDEN_APPLE) {
	                gaps = i;
	                break;
	            }
		}	
	}	
}
