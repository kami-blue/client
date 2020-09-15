package me.zeroeightsix.kami.module.modules.combat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.GuiScreenEvent;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.InventoryUtils;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;

import java.util.Comparator;
import java.util.Objects;

@Module.Info(
        name = "OffhandGap",
        category = Module.Category.COMBAT,
        description = "Holds a God apple when right clicking your sword!"
)
public class OffhandGap extends Module {
    private final Setting<Double> disableHealth = register(Settings.doubleBuilder("DisableHealth").withMinimum(0.0).withValue(4.0).withMaximum(20.0).build());
    private final Setting<Boolean> eatWhileAttacking = register(Settings.b("EatWhileAttacking", false));
    private final Setting<Boolean> swordOrAxeOnly = register(Settings.b("SwordAndAxeOnly", true));
    private final Setting<Boolean> preferBlocks = register(Settings.booleanBuilder("PreferPlacingBlocks").withValue(false).withVisibility(v -> !swordOrAxeOnly.getValue()).build());
    private final Setting<Boolean> crystalCheck = register(Settings.b("CrystalCheck", false));

    private int gaps = -1;
    private boolean autoTotemWasEnabled = false;
    private boolean cancelled = false;
    private boolean isGuiOpened = false;
    private Item usedItem;

    public static OffhandGap INSTANCE;

    public OffhandGap() {
        super();
        INSTANCE = this;
    }

    @EventHandler
    public Listener<GuiScreenEvent.Displayed> listener = new Listener<>(event ->
            isGuiOpened = event.getScreen() != null
    );

    @EventHandler
    private final Listener<PacketEvent.Send> sendListener = new Listener<>(e -> {
        if (e.getPacket() instanceof CPacketPlayerTryUseItem) {
            if (cancelled) {
                disableGaps();
                return;
            }
            if (mc.player.getHeldItemMainhand().getItem() instanceof ItemSword || mc.player.getHeldItemMainhand().getItem() instanceof ItemAxe || passItemCheck()) {
                if (AutoTotem.INSTANCE.isEnabled()) {
                    autoTotemWasEnabled = true;
                    AutoTotem.INSTANCE.disable();
                }
                if (!eatWhileAttacking.getValue()) { /* Save item for later when using preventDesync */
                    usedItem = mc.player.getHeldItemMainhand().getItem();
                }
                enableGaps(gaps);
            }
        }
        try {
            /* If you stop holding right click move totem back */
            if (!mc.gameSettings.keyBindUseItem.isKeyDown() && mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE)
                disableGaps();
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
            if (crystalCheck.getValue() && CrystalAura.INSTANCE.isEnabled()) {
                EntityEnderCrystal crystal = mc.world.loadedEntityList.stream()
                        .filter(entity -> entity instanceof EntityEnderCrystal)
                        .map(entity -> (EntityEnderCrystal) entity)
                        .min(Comparator.comparing(c -> mc.player.getDistance(c)))
                        .orElse(null);
                if (Objects.requireNonNull(crystal).getPosition().distanceSq(mc.player.getPosition().x, mc.player.getPosition().y, mc.player.getPosition().z) <= CrystalAura.INSTANCE.range.getValue()) {
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
        if (cancelled) {
            disableGaps();
            return;
        }

        if (mc.player.getHeldItemOffhand().getItem() != Items.GOLDEN_APPLE) {
            for (int i = 0; i < 45; i++) {
                if (mc.player.inventory.getStackInSlot(i).getItem() == Items.GOLDEN_APPLE) {
                    gaps = i;
                    break;
                }
            }
        }
    }

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
            return !preferBlocks.getValue() || !(item instanceof ItemBlock);
        }
    }

    private void disableGaps() {
        if (autoTotemWasEnabled != AutoTotem.INSTANCE.isEnabled()) {
            moveGapsWaitForNoGui();
            AutoTotem.INSTANCE.enable();
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

    private void moveGapsWaitForNoGui() {
        if (isGuiOpened) return;
        moveGapsToInventory(gaps);
    }

    @Override
    public String getHudInfo() {
        return "" + InventoryUtils.countItemAll(322);
    }
}
