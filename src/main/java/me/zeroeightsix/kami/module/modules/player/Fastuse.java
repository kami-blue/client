package me.zeroeightsix.kami.module.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
// info goes here
import me.zeroeightsix.kami.module.modules.misc.BookCrash;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.init.Items;

//import me.zeroeightsix.kami.module.Module.Info;

/**
 * Created by S-B99 on 23/10/2019
 * @author S-B99
 * Updated by d1gress/Qther on 2/12/2019
 */
@Module.Info(category = Module.Category.PLAYER, description = "Changes delay when holding right click", name = "FastUse")
public class Fastuse extends Module {

	private Setting<Boolean> exp = register(Settings.b("EXP Bottles", false));
	private Setting<Boolean> crystal = register(Settings.b("Crystals", false));
	private Setting<Boolean> all = register(Settings.b("Everything", true));
	private Setting<Integer> delay = register(Settings.i("Delay", 0));
	private static long time = 0;


	@Override
	public void onUpdate() {
		if (mc.player != null && delay.getValue() <= 0) {
			if (!all.getValue() && (mc.player.getHeldItemMainhand().getItem() == Items.EXPERIENCE_BOTTLE || mc.player.getHeldItemOffhand().getItem() == Items.EXPERIENCE_BOTTLE) && exp.getValue())  mc.rightClickDelayTimer = 0;
			if (!all.getValue() && (mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL || mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL) && crystal.getValue())  mc.rightClickDelayTimer = 0;
			if (all.getValue())  mc.rightClickDelayTimer = 0;
		}
		else if (mc.player != null && time == 0 || time + delay.getValue() * 50 <= System.currentTimeMillis()) { //scales weird
			if (!all.getValue() && (mc.player.getHeldItemMainhand().getItem() == Items.EXPERIENCE_BOTTLE || mc.player.getHeldItemOffhand().getItem() == Items.EXPERIENCE_BOTTLE) && exp.getValue())  mc.rightClickDelayTimer = 0;
			if (!all.getValue() && (mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL || mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL) && crystal.getValue())  mc.rightClickDelayTimer = 0;
			if (all.getValue())  mc.rightClickDelayTimer = 0;
			time = System.currentTimeMillis();
		}
	}
}
