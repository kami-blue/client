package me.zeroeightsix.kami.module.modules.combat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.event.events.GuiScreenEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

import static me.zeroeightsix.kami.util.MessageSendHelper.sendWarningMessage;

/**
 * @author polymer
 * @author cookiedragon234
 * Updated by polymer 10 March 2020
 * Updated by dominikaaaa on 12/04/20
 */
@Module.Info(
        name = "AutoEZ",
        category = Module.Category.COMBAT,
        description = "Sends an insult in chat after killing someone"
)
public class AutoEZ extends Module {
	public Setting<Mode> mode = register(Settings.e("Mode", Mode.ONTOP));
	public Setting<String> customText = register(Settings.stringBuilder("Custom Text").withValue("unchanged").withConsumer((old, value) -> {}).build());

	EntityPlayer focus;
	int hasBeenCombat;

	public enum Mode {
		GG("gg, $NAME"),
		ONTOP("KAMI BLUE on top! ez $NAME"),
		EZD("You just got ez'd $NAME"),
		EZ_HYPIXEL("E Z Win $NAME"),
		NAENAE("You just got naenae'd by kami blue plus, $NAME"),
		CUSTOM();

		private String text;

		Mode(String text) {
			this.text = text;
		}
		Mode() { } // yes
	}

	private String getText(Mode m, String playerName) {
		if (m.equals(Mode.CUSTOM)) {
			return customText.getValue().replace("$NAME", playerName);
		}
		return m.text.replace("$NAME", playerName);
	}

	@EventHandler
	public Listener<AttackEntityEvent> livingDeathEventListener = new Listener<>(event -> {
		if (event.getTarget() instanceof EntityPlayer) {
			focus = (EntityPlayer) event.getTarget();
			if (event.getEntityPlayer().getUniqueID() == mc.player.getUniqueID()) {
				if (focus.getHealth() <= 0.0 || focus.isDead || !mc.world.playerEntities.contains(focus)) {
					mc.player.sendChatMessage(getText(mode.getValue(), event.getTarget().getName()));
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

	private static long startTime = 0;
	@Override
	public void onUpdate() {
		if (mc.player == null) return;
		if (hasBeenCombat > 0 && (focus.getHealth() <= 0.0f || focus.isDead || !mc.world.playerEntities.contains(focus))) {
			mc.player.sendChatMessage(getText(mode.getValue(), focus.getName()));
			hasBeenCombat = 0;
		}
		--hasBeenCombat;
		if (startTime == 0) startTime = System.currentTimeMillis();
		if (startTime + 5000 <= System.currentTimeMillis()) { // 5 seconds in milliseconds
			if (mode.getValue().equals(Mode.CUSTOM) && customText.getValue().equalsIgnoreCase("unchanged") && mc.player != null) {
				sendWarningMessage(getChatName() + " Warning: In order to use the custom " + getName() + ", please run the &7" + Command.getCommandPrefix() + "autoez&r command to change it, with '&7$NAME&f' being the username of the killed player");
			}
			startTime = System.currentTimeMillis();
		}
	}
}

