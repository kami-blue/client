package me.zeroeightsix.kami.module.modules.combat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.init.Items;

import static me.zeroeightsix.kami.util.MathsUtils.reverseNumber;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendWarningMessage;

/**
 * Created 17 October 2019 by hub
 * Updated 21 November 2019 by hub
 * Updated by dominikaaaa on 07/04/20
 */
@Module.Info(
        name = "AutoMend",
        category = Module.Category.COMBAT,
        description = "Automatically mends armour"
)
public class AutoMend extends Module {

    private Setting<Boolean> autoThrow = register(Settings.b("Auto Throw", true));
    private Setting<Boolean> autoSwitch = register(Settings.b("Auto Switch", true));
    private Setting<Boolean> autoDisable = register(Settings.booleanBuilder("Auto Disable").withValue(false).withVisibility(o -> autoSwitch.getValue()).build());
    private Setting<Integer> threshold = register(Settings.integerBuilder("Repair %").withMinimum(1).withMaximum(100).withValue(75));

    private int initHotbarSlot = -1;

    @EventHandler
    private Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {
        if (mc.player != null && (mc.player.getHeldItemMainhand().getItem() == Items.EXPERIENCE_BOTTLE)) {
            mc.rightClickDelayTimer = 0;
        }
    });

    @Override
    protected void onEnable() {
        if (mc.player == null) return;

        if (autoSwitch.getValue()) {
            initHotbarSlot = mc.player.inventory.currentItem;
        }

    }

    @Override
    protected void onDisable() {
        if (mc.player == null) return;

        if (autoSwitch.getValue()) {
            if (initHotbarSlot != -1 && initHotbarSlot != mc.player.inventory.currentItem) {
                mc.player.inventory.currentItem = initHotbarSlot;
            }
        }

    }

    @Override
    public void onUpdate() {
        if (mc.player == null) return;

        if (shouldMend(0) || shouldMend(1) || shouldMend(2) || shouldMend(3)) {

            if (autoSwitch.getValue() && (mc.player.getHeldItemMainhand().getItem() != Items.EXPERIENCE_BOTTLE)) {
                int xpSlot = findXpPots();
                if (xpSlot == -1) {
                    if (autoDisable.getValue()) {
                        sendWarningMessage(getChatName() + " No XP in hotbar, disabling");
                        disable();
                    }
                    return;
                }
                mc.player.inventory.currentItem = xpSlot;
            }

            if (autoThrow.getValue() && mc.player.getHeldItemMainhand().getItem() == Items.EXPERIENCE_BOTTLE) {
                mc.rightClickMouse();
            }
        }

    }

    private int findXpPots() {
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == Items.EXPERIENCE_BOTTLE) {
                slot = i;
                break;
            }
        }
        return slot;
    }

    private boolean shouldMend(int i) { // (100 * damage / max damage) >= (100 - 70)
        if (mc.player.inventory.armorInventory.get(i).getMaxDamage() == 0) return false;
        return (100 * mc.player.inventory.armorInventory.get(i).getItemDamage())
                / mc.player.inventory.armorInventory.get(i).getMaxDamage()
                > reverseNumber(threshold.getValue(), 1, 100);
    }

}
