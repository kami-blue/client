package me.zeroeightsix.kami.module.modules.experimental;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

/**
 * @author polymer
 * Created by polymer on 21 Feb 2020
 * Updated by S-B99 on 21/02/20'
 * Updated by polymer 10 March 2020
 */

@Module.Info(name = "Auto EZ", category = Module.Category.EXPERIMENTAL, description = "Sends a Kami-themed insult in chat after killing someone")
public class AutoWalter extends Module {
	private Setting<Mode> mode = register(Settings.e("Mode", Mode.ONTOP));
	int hasBeenCombat;
	private EntityPlayer focus;
	enum Mode {
		OVER_EVERYTHING, ONTOP, EZD, EZ_HYPIXEL, NAENAE
	}

	private String getText(Mode m) {
		switch (m) {
			case OVER_EVERYTHING: return "gg, ";
			case ONTOP: return "KAMI BLUE on top! ez ";
			case EZD: return "You just got ez'd ";
			case EZ_HYPIXEL: return "E Z Win ";
			case NAENAE: return "You just got naenae'd by kami blue plus, ";
			default: return null;
		}
	}
	
	@EventHandler public Listener<AttackEntityEvent> livingDeathEventListener = new Listener<>(event -> {
		if (event.getTarget() instanceof EntityPlayer) {
			focus = (EntityPlayer)event.getTarget();
			if (event.getEntityPlayer().getUniqueID() == mc.player.getUniqueID()) {
				if (focus.getHealth() <= 0.0 || focus.isDead || !mc.world.playerEntities.contains(focus)) {
					mc.player.sendChatMessage(getText(mode.getValue()) + event.getTarget().getName());
					return;
				}
				hasBeenCombat = 500;
				this.focus = focus;
			}
		}
	});
	
	@Override
	public void onUpdate() {
		 if (mc.player.isDead) {
	            hasBeenCombat = 0;
		 }
    --hasBeenCombat;
	}
	
}

