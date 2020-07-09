package me.zeroeightsix.kami.module.modules.chat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.network.play.server.SPacketUpdateHealth;
import java.util.Random;

import static me.zeroeightsix.kami.util.MessageSendHelper.*;

/**
 * @author sourTaste000
 * I can't believe I'm actually making this
 */

@Module.Info(
        name = "AutoExcuse",
        description = "Makes an excuse for you when you die",
        category = Module.Category.CHAT
)
public class AutoExcuse extends Module{
    private Setting<Mode> modeSetting = register(Settings.enumBuilder(Mode.class).withName("Mode").withValue(Mode.CRYSTAL).withVisibility(v -> true).build());

    private enum Mode {CRYSTAL, ANARCHY}

    Random rand = new Random();


    @EventHandler
    public Listener<SPacketUpdateHealth> listener = new Listener<>(event -> {
        if((mc.player != null) && (mc.player.getHealth() <= 0.0F)){
            do {
                switch (modeSetting.getValue()){
                    case CRYSTAL:
                        switch (rand.nextInt(3)){
                            case 1:
                                sendServerMessage("oops i think my autototem is off lol");
                                break;

                            case 2:
                                sendServerMessage("my crystalaura settings are shit");
                                break;

                            case 3:
                                sendServerMessage("why is my syrround not working");
                                break;
                        }
                        break;

                    case ANARCHY:
                        switch (rand.nextInt(3)){
                            case 1:
                                sendServerMessage("fucking spawnfags");
                                break;

                            case 2:
                                sendServerMessage("why are you hitting me we are both escaping");
                                break;

                            case 3:
                                sendServerMessage("ouch i broke my legs");
                                break;
                        }
                        break;
                }
                break;
            }while((mc.player != null) && (mc.player.getHealth() >= 5.0F));
        }
    });
}
