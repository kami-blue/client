package me.zeroeightsix.kami.module.modules.experimental;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.event.events.ClientPlayerAttackEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.entity.EntityLivingBase;
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
	boolean hasKilledEntity = false;
	
	enum Mode {
		OVER_EVERYTHING, ONTOP, EZD, EZ_HYPIXEL, NAENAE
	}

	private String getText(Mode m) {
		switch (m) {
			case OVER_EVERYTHING: return "KAMI BLUE over everything! GG, ";
			case ONTOP: return "KAMI BLUE on top! ez ";
			case EZD: return "You just got ez'd ";
			case EZ_HYPIXEL: return "E Z Win ";
			case NAENAE: return "You just got naenae'd by kami blue plus, ";
			default: return null;
		}
	}
	
	@EventHandler public Listener<ClientPlayerAttackEvent> livingDeathEventListener = new Listener<>(event -> {
		if (event.getTargetEntity() instanceof EntityPlayerMP) {
			if (((EntityLivingBase) event.getTargetEntity()).getHealth() >= 0) {
				Command.sendChatMessage( "[Client] You just attacked" + event.getTargetEntity());
				hasKilledEntity = false;
			}
			else if (((EntityLivingBase) event.getTargetEntity()).getHealth() <= 0) {
				hasKilledEntity = true;
			}
			if (hasKilledEntity = true && getText(mode.getValue()) != null) {
				mc.player.sendChatMessage(getText(mode.getValue()) +  event.getTargetEntity() );
				hasKilledEntity = false;
			}
		} 
	});
	
}
