package me.zeroeightsix.kami.module.modules.combat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.GuiScreenEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.gui.GuiGameOver;
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
	EntityPlayer focus;
	int hasBeenCombat;

	public enum Mode {
		GG("gg, "),
		ONTOP("KAMI BLUE on top! ez "),
		EZD("You just got ez'd "),
		EZ_HYPIXEL("E Z Win "),
		NAENAE("You just got naenae'd by kami blue plus, ");

		private String text;

		Mode(String text) {
			this.text = text;
		}
	}

	private String getText(Mode m) {
		return m.text;
	}
	
	@EventHandler
	public Listener<AttackEntityEvent> livingDeathEventListener = new Listener<>(event -> {
		if (event.getTarget() instanceof EntityPlayer) {
			focus = (EntityPlayer) event.getTarget();
			if (event.getEntityPlayer().getUniqueID() == mc.player.getUniqueID()) {
				if (focus.getHealth() <= 0.0 || focus.isDead || !mc.world.playerEntities.contains(focus)) {
					mc.player.sendChatMessage(getText(mode.getValue()) + event.getTarget().getName());
					return;
				}
				hasBeenCombat = 1000;
			}
		}
	});

	@EventHandler
	public Listener<GuiScreenEvent.Displayed> listener = new Listener<>(event -> {
		if (!(event.getScreen() instanceof GuiGameOver)) return;
		if (mc.player.getHealth() > 0) {
			hasBeenCombat = 0;
		}
	});
	
	@Override
	public void onUpdate() {
		if (mc.player == null) return;
		if (hasBeenCombat > 0 && (focus.getHealth() <= 0.0f || focus.isDead || !mc.world.playerEntities.contains(focus))) {
			mc.player.sendChatMessage(getText(mode.getValue())+focus.getName());
			hasBeenCombat = 0;
		}
		--hasBeenCombat;
	}
}

