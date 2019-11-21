package me.zeroeightsix.kami.module.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.init.Items;

/**
 * Created 17 October 2019 by hub
 * Updated 21 November 2019 by hub
 */
@Module.Info(name = "FastExp", category = Module.Category.PLAYER, description = "Auto Switch to XP and throw fast")
public class FastExp extends Module {

    private Setting<Boolean> autoThrow = register(Settings.b("Auto Throw", true));
    private Setting<Boolean> autoSwitch = register(Settings.b("Auto Switch", true));
    private Setting<Boolean> autoDisable = register(Settings.booleanBuilder("Auto Disable").withValue(false).withVisibility(o -> autoSwitch.getValue()).build());

    private int initHotbarSlot = -1;

    @EventHandler
    private Listener<PacketEvent.Receive> receiveListener = new Listener<>(event ->
    {
        if (mc.player != null && (mc.player.getHeldItemMainhand().getItem() == Items.EXPERIENCE_BOTTLE)) {
            mc.rightClickDelayTimer = 0;
        }
    });

    @Override
    protected void onEnable() {

        if (mc.player == null) {
            return;
        }

        if (autoSwitch.getValue()) {
            initHotbarSlot = mc.player.inventory.currentItem;
        }

    }

    @Override
    protected void onDisable() {

        if (mc.player == null) {
            return;
        }

        if (autoSwitch.getValue()) {
            if (initHotbarSlot != -1 && initHotbarSlot != mc.player.inventory.currentItem) {
                mc.player.inventory.currentItem = initHotbarSlot;
            }
        }

    }

    @Override
    public void onUpdate() {

        if (mc.player == null) {
            return;
        }

        if (autoSwitch.getValue() && (mc.player.getHeldItemMainhand().getItem() != Items.EXPERIENCE_BOTTLE)) {
            int xpSlot = findXpPots();
            if (xpSlot == -1) {
                if (autoDisable.getValue()) {
                    this.disable();
                }
                return;
            }
            mc.player.inventory.currentItem = xpSlot;
        }

        if (autoThrow.getValue() && mc.player.getHeldItemMainhand().getItem() == Items.EXPERIENCE_BOTTLE) {
            mc.rightClickMouse();
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

}
