package me.zeroeightsix.kami.module.modules.combat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;

import java.util.Comparator;
import java.util.Objects;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;
import static me.zeroeightsix.kami.module.modules.gui.InfoOverlay.getItems;

/**
 * @author polymer (main listener switch function xd)
 * @author S-B99 (made epic and smooth and cleaned up code <3) (why did i rewrite this 4 times)
 * Created by polymer on 21/02/20
 * Updated by S-B99 on 07/03/20
 */
@Module.Info(name = "OffhandGap", category = Module.Category.COMBAT, description = "Holds a God apple when right clicking your sword!")
public class OffhandGap extends Module {
	private Setting<Double> disableHealth = register(Settings.doubleBuilder("Disable Health").withMinimum(0.0).withValue(4.0).withMaximum(20.0).build());
	private Setting<Boolean> eatWhileAttacking = register(Settings.b("Eat While Attacking", false));
	private Setting<Boolean> swordOrAxeOnly = register(Settings.b("Sword or Axe Only", true));
	private Setting<Boolean> preferBlocks = register(Settings.booleanBuilder("Prefer Placing Blocks").withValue(false).withVisibility(v -> !swordOrAxeOnly.getValue()).build());
	private Setting<Boolean> crystalCheck = register(Settings.b("Crystal Check", false));
//	private Setting<Mode> modeSetting = register(Settings.e("Use Mode", Mode.GAPPLE));

//	private enum Mode {
//		GAPPLE, FOOD, CUSTOM
//	}

	int gaps = -1;
	boolean autoTotemWasEnabled = false;
	boolean cancelled = false;
	Item usedItem;
	Item toUseItem;
	CrystalAura crystalAura;

	@EventHandler
	private Listener<PacketEvent.Send> sendListener = new Listener<>(e ->{
		if (e.getPacket() instanceof CPacketPlayerTryUseItem) {
			if (cancelled) {
				disableGaps();
				return;
			}
			if (mc.player.getHeldItemMainhand().getItem() instanceof ItemSword || mc.player.getHeldItemMainhand().getItem() instanceof ItemAxe || passItemCheck()) {
				if (MODULE_MANAGER.isModuleEnabled(AutoTotem.class)) {
					autoTotemWasEnabled = true;
					MODULE_MANAGER.getModule(AutoTotem.class).disable();
				}
				if (!eatWhileAttacking.getValue()) { /* Save item for later when using preventDesync */
					usedItem = mc.player.getHeldItemMainhand().getItem();
				}
				enableGaps(gaps);
			}
		}
		try {
			/* If you stop holding right click move totem back */
			if (!mc.gameSettings.keyBindUseItem.isKeyDown() && mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE) disableGaps();
				/* In case you didn't stop right clicking but you switched items by scrolling or something */
			else if ((usedItem != mc.player.getHeldItemMainhand().getItem()) && mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE) {
				if (!eatWhileAttacking.getValue()) {
					usedItem = mc.player.getHeldItemMainhand().getItem();
					disableGaps();
				}
			}
			/* Force disable if under health limit */
			else if (mc.player.getHealth() + mc.player.getAbsorptionAmount() <= disableHealth.getValue()) {
				disableGaps();
			}
			/* Disable if there are crystals in the range of CrystalAura */
			crystalAura = (CrystalAura) MODULE_MANAGER.getModule(CrystalAura.class);
			if (crystalCheck.getValue() && crystalAura.isEnabled()) {
				EntityEnderCrystal crystal = mc.world.loadedEntityList.stream()
		                .filter(entity -> entity instanceof EntityEnderCrystal)
		                .map(entity -> (EntityEnderCrystal) entity)
		                .min(Comparator.comparing(c -> mc.player.getDistance(c)))
		                .orElse(null);
				if (Objects.requireNonNull(crystal).getPosition().distanceSq(mc.player.getPosition().x, mc.player.getPosition().y, mc.player.getPosition().z) <= crystalAura.range.getValue()) {
					disableGaps();
				}
			}
		} catch (NullPointerException ignored) { }
	});

	@Override
	public void onUpdate() {
		if (mc.player == null) return;

		/* If your health doesn't meet the cutoff then set it to true */
		cancelled = mc.player.getHealth() + mc.player.getAbsorptionAmount() <= disableHealth.getValue();
		if (cancelled) { disableGaps(); return; }

		toUseItem = Items.GOLDEN_APPLE;
		if (mc.player.getHeldItemOffhand().getItem() != Items.GOLDEN_APPLE) {
			for (int i = 0; i < 45; i++) {
				if (mc.player.inventory.getStackInSlot(i).getItem() == Items.GOLDEN_APPLE) {
					gaps = i;
					break;
				}
			}
		}
	}

//	private static Map<Integer, ItemStack> getFullInventory() { return getInventorySlots(0, 45); }

	/* If weaponCheck is disabled, check if they're not holding an item you'd want to use normally */
	private boolean passItemCheck() {
		if (swordOrAxeOnly.getValue()) return false;
		else {
			Item item = mc.player.getHeldItemMainhand().getItem();
			if (item instanceof ItemBow) return false;
			if (item instanceof ItemSnowball) return false;
			if (item instanceof ItemEgg) return false;
			if (item instanceof ItemPotion) return false;
			if (item instanceof ItemEnderEye) return false;
			if (item instanceof ItemEnderPearl) return false;
			if (item instanceof ItemFood) return false;
			if (item instanceof ItemShield) return false;
			if (item instanceof ItemFlintAndSteel) return false;
			if (item instanceof ItemFishingRod) return false;
			if (item instanceof ItemArmor) return false;
			if (item instanceof ItemExpBottle) return false;
			if (preferBlocks.getValue() && item instanceof ItemBlock) return false;
		}
		return true;
	}

	private void disableGaps() {
		if (autoTotemWasEnabled != MODULE_MANAGER.isModuleEnabled(AutoTotem.class)) {
			moveGapsToInventory(gaps);
			MODULE_MANAGER.getModule(AutoTotem.class).enable();
			autoTotemWasEnabled = false;
		}
	}

	private void enableGaps(int slot) {
		if (mc.player.getHeldItemOffhand().getItem() != Items.GOLDEN_APPLE) {
			mc.playerController.windowClick(0, slot < 9 ? slot + 36 : slot, 0, ClickType.PICKUP, mc.player);
			mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
		}
	}

	private void moveGapsToInventory(int slot) {
		if (mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE) {
			mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
			mc.playerController.windowClick(0, slot < 9 ? slot + 36 : slot, 0, ClickType.PICKUP, mc.player);
		}
	}

	@Override
	public String getHudInfo() {
		return String.valueOf(getItems(Items.GOLDEN_APPLE));
	}
}
