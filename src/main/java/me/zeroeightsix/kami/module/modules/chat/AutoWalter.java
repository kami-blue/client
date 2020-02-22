package me.zeroeightsix.kami.module.modules.chat;

import static me.zeroeightsix.kami.KamiMod.KAMI_BLUE;
import static me.zeroeightsix.kami.KamiMod.KAMI_JAPANESE_ONTOP;
import static me.zeroeightsix.kami.KamiMod.KAMI_ONTOP;
import static me.zeroeightsix.kami.KamiMod.KAMI_WEBSITE;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.modules.chat.ChatSuffix.TextMode;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

/*
 *  AutoWalter
 *  By polymer 21 Feb 2020 
 *
 */

@Module.Info(name = "Auto Walter", category = Module.Category.CHAT, description = "automatically gloats at people when they die using walter-themed insults")

public class AutoWalter extends Module {
	private Setting<Message> message = register(Settings.e("Mode", Message.ONTOP));
	
	enum Message {
			OVER_EVERYTHING,
			ONTOP,
			EZD,
			EZ_HYPIXEL,
			NAENAE,
	};
	  private String getText(Message t) {
	        switch (t) {
	            case OVER_EVERYTHING: return "Walter over everything!";
	            case ONTOP: return "Walter Client on top!";
	            case EZD: return "You just got EZ'd!!!";
	            case EZ_HYPIXEL: return "E Z Win!";
	            case NAENAE: return "You just got naenae'd by Walter Client!";
	            default: return "";
	        }
	    }
	@EventHandler public Listener<LivingDeathEvent> livingDeathEventListener = new Listener<>(event -> { if (event.getEntity() instanceof EntityPlayerMP && event.getEntity() != mc.player) mc.player.sendChatMessage("Nice fight " + event.getEntity().getCustomNameTag() + "! " + getText(message.getValue()) ); });
}
