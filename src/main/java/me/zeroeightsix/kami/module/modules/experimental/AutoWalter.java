package me.zeroeightsix.kami.module.modules.experimental;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

/**
 * @author polymer
 * Created by polymer on 21 Feb 2020
 * Updated by S-B99 on 21/02/20
 */
@Module.Info(name = "Auto Walter", category = Module.Category.EXPERIMENTAL, description = "Sends a walter-themed insult in chat after killing someone")
public class AutoWalter extends Module {
	private Setting<Mode> mode = register(Settings.e("Mode", Mode.ONTOP));

	enum Mode {
		OVER_EVERYTHING, ONTOP, EZD, EZ_HYPIXEL, NAENAE
	}

	private String getText(Mode m) {
		switch (m) {
			case OVER_EVERYTHING: return "Walter over everything!";
			case ONTOP: return "Walter Client on top!";
			case EZD: return "You just got EZ'd!!!";
			case EZ_HYPIXEL: return "E Z Win!";
			case NAENAE: return "You just got naenae'd by Walter Client!";
			default: return "";
		}
	}
	
	@EventHandler public Listener<LivingDeathEvent> livingDeathEventListener = new Listener<>(event -> {
		if (event.getEntity() instanceof EntityPlayerMP && event.getEntity() != mc.player)
			mc.player.sendChatMessage("Nice fight " + event.getEntity().getCustomNameTag() + "! " + getText(mode.getValue()));
	});
}
