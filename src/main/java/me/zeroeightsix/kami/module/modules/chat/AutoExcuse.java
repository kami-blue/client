package me.zeroeightsix.kami.module.modules.chat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.GuiScreenEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.gui.GuiGameOver;

import java.util.Random;

import static me.zeroeightsix.kami.util.MessageSendHelper.sendServerMessage;

/**
 * @author sourTaste000
 * @since 7/8/2020
 * most :smoothbrain: code ever
 */

@Module.Info(
        name = "AutoExcuse",
        description = "Makes an excuse for you when you die",
        category = Module.Category.CHAT
)
public class AutoExcuse extends Module {
    Random rand = new Random();

    private final Setting<Mode> modeSetting = register(Settings.enumBuilder(Mode.class).withName("Mode").withValue(Mode.CRYSTAL).withVisibility(v -> true).build());

    @EventHandler
    public Listener<GuiScreenEvent.Displayed> listener = new Listener<>(event -> {
        if (event.getScreen() instanceof GuiGameOver) {
            mc.player.respawnPlayer();
            switch (modeSetting.getValue()) {
                case CRYSTAL:
                    switch (rand.nextInt(3)) {
                        case 0:
                            sendServerMessage("my ping is so bad");
                            break;

                        case 1:
                            sendServerMessage("i was changing my config :(");
                            break;

                        case 2:
                            sendServerMessage("why did my autototem break");
                            break;

                        case 3:
                            sendServerMessage("i was desynced");
                            break;
                    }
                    break;

                case ANARCHY:
                    switch (rand.nextInt(3)) {
                        case 0:
                            sendServerMessage("i hate withers");
                            break;

                        case 1:
                            sendServerMessage("im trying to escape why did u kill me :((");
                            break;

                        case 2:
                            sendServerMessage("can someone give me food");
                            break;

                        case 3:
                            sendServerMessage("ouch i broke my legs");
                            break;
                    }
                    break;
            }
        }
    });

    private enum Mode {CRYSTAL, ANARCHY}
}
