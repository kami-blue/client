package me.zeroeightsix.kami.module.modules.combat;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.math.BlockPos;

/**
 * Created by dominikaaaa on 23/10/2019
 * @author dominikaaaa
 * Updated by dominikaaaa on 03/12/19
 * Updated by d1gress/Qther on 4/12/19
 * 
 * Bowspam code from https://github.com/seppukudevelopment/seppuku/blob/5586365/src/main/java/me/rigamortis/seppuku/impl/module/combat/FastBowModule.java
 */
@Module.Info(
        category = Module.Category.COMBAT,
        description = "Use items faster", name = "FastUse"
)
public class Fastuse extends Module {

    private Setting<Integer> delay = register(Settings.integerBuilder("Delay").withMinimum(0).withMaximum(20).withValue(0).build());
    private Setting<Boolean> all = register(Settings.b("All", false));
    private Setting<Boolean> bow = register(Settings.booleanBuilder().withName("Bow").withValue(true).withVisibility(v -> !all.getValue()).build());
    private Setting<Boolean> expBottles = register(Settings.booleanBuilder().withName("Exp Bottles").withValue(true).withVisibility(v -> !all.getValue()).build());
    private Setting<Boolean> endCrystals = register(Settings.booleanBuilder().withName("End Crystals").withValue(true).withVisibility(v -> !all.getValue()).build());
    private Setting<Boolean> fireworks = register(Settings.booleanBuilder().withName("Fireworks").withValue(false).withVisibility(v -> !all.getValue()).build());
    private static long time = 0;

    @Override
    public void onDisable() {
        mc.rightClickDelayTimer = 4;
    }

    @Override
    public void onUpdate() {
        if (mc.player == null) return;

        if (all.getValue() || bow.getValue() && mc.player.getHeldItemMainhand().getItem() instanceof ItemBow && mc.player.isHandActive() && mc.player.getItemInUseMaxCount() >= 3) {
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(mc.player.getActiveHand()));
            mc.player.stopActiveHand();
        }

        if (!(delay.getValue() <= 0)) {
            if (time <= 0) time = Math.round((2 * (Math.round((float) delay.getValue() / 2))));
            else {
                time--;
                mc.rightClickDelayTimer = 1;
                return;
            }
        }

        if (passItemCheck(mc.player.getHeldItemMainhand().getItem()) || passItemCheck(mc.player.getHeldItemOffhand().getItem())) {
            mc.rightClickDelayTimer = 0;
        }
    }

    private boolean passItemCheck(Item item) {
        if (all.getValue()) return true;
        if (expBottles.getValue() && item instanceof ItemExpBottle) return true;
        if (endCrystals.getValue() && item instanceof ItemEndCrystal) return true;
        if (fireworks.getValue() && item instanceof ItemFirework) return true;
        return false;
    }
}
