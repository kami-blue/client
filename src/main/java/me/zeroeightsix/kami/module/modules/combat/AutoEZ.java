package me.zeroeightsix.kami.module.modules.combat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

/**
 * @author polymer
 * @author cookiedragon234
 * Updated by polymer 10 March 2020
 * Updated by S-B99 on 10/04/20
 */
@Module.Info(name = "AutoEZ", category = Module.Category.COMBAT, description = "Sends an insult in chat after killing someone")
public class AutoEZ extends Module {
	private Setting<Mode> mode = register(Settings.e("Mode", Mode.ONTOP));

	int hasBeenCombat;
	enum Mode {GG, ONTOP, EZD, EZ_HYPIXEL, NAENAE }

	private String getText(Mode m) {
		switch (m) {
			case GG: return "gg, ";
			case ONTOP: return "KAMI BLUE on top! ez ";
			case EZD: return "You just got ez'd ";
			case EZ_HYPIXEL: return "E Z Win ";
			case NAENAE: return "You just got naenae'd by kami blue plus, ";
			default: return null;
		}
	}
	
	@EventHandler public Listener<AttackEntityEvent> livingDeathEventListener = new Listener<>(event -> {
		if (event.getTarget() instanceof EntityPlayer) {
			EntityPlayer focus = (EntityPlayer) event.getTarget();
			if (event.getEntityPlayer().getUniqueID() == mc.player.getUniqueID()) {
				if (focus.getHealth() <= 0.0 || focus.isDead || !mc.world.playerEntities.contains(focus)) {
					mc.player.sendChatMessage(getText(mode.getValue()) + event.getTarget().getName());
					return;
				}
				hasBeenCombat = 500;
			}
		}
	});
	
	@Override
	public void onUpdate() {
		if (mc.player.isDead) hasBeenCombat = 0;
		--hasBeenCombat;
	}
}

