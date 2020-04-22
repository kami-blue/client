package me.zeroeightsix.kami.module.modules.chat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.ColourTextFormatting;
import me.zeroeightsix.kami.util.Friends;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import static me.zeroeightsix.kami.util.ColourTextFormatting.toTextMap;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendErrorMessage;

@Module.Info(
        name = "FriendHighlight",
        description = "Highlights your friends names in chat",
        category = Module.Category.CHAT,
        showOnArray = Module.ShowOnArray.OFF
)
public class FriendHighlight extends Module {
    private Setting<Boolean> bold = register(Settings.b("Bold", true));
    private Setting<ColourTextFormatting.ColourCode> colour = register(Settings.e("Colour", ColourTextFormatting.ColourCode.GRAY));

    public void onEnable() {
        if (Friends.friends.getValue().size() > 100) {
            sendErrorMessage(getChatName() + "Your friends list is bigger then 100, disabling as it would cause too much of a performance impact.");
            disable();
        }
    }

    @EventHandler
    public Listener<ClientChatReceivedEvent> listener = new Listener<>(event -> {
        if (mc.player == null) return;
        final String[] converted = {""};
        Friends.friends.getValue().forEach(friend -> converted[0] = event.getMessage().getFormattedText().replaceAll("(?i)" + friend.getUsername(), colour() + bold() + friend.getUsername() + TextFormatting.RESET.toString()));
        TextComponentString message = new TextComponentString(converted[0]);
        event.setMessage(message);
    });

    private String bold() {
        if (!bold.getValue()) return "";
        return TextFormatting.BOLD.toString();
    }

    private String colour() {
        return toTextMap.get(colour.getValue()).toString();
    }

}
