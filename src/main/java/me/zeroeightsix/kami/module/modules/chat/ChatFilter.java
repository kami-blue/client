package me.zeroeightsix.kami.module.modules.chat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Pattern;

import static me.zeroeightsix.kami.util.MessageDetectionHelper.isDirect;
import static me.zeroeightsix.kami.util.MessageDetectionHelper.isDirectOther;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendErrorMessage;

/**
 * @author dominikaaaa
 * Updated by suretic on 13/05/20
 * Updated by domikaaaa on 23/05/20
 */
@Module.Info(
        name = "ChatFilter",
        description = "Filters custom words or phrases from the chat",
        category = Module.Category.CHAT,
        showOnArray = Module.ShowOnArray.OFF
)
public class ChatFilter extends Module {
    private Setting<Boolean> filterOwn = register(Settings.b("FilterOwn", false));
    private Setting<Boolean> filterDMs = register(Settings.b("FilterDMs", false));
    private Setting<Boolean> hasRunInfo = register(Settings.booleanBuilder("Info").withValue(false).withVisibility(v -> false).build());

    private static ArrayList<Pattern> chatFilter = new ArrayList<>();

    @EventHandler
    public Listener<ClientChatReceivedEvent> listener = new Listener<>(event -> {
        if (mc.player == null) return;

        if (isDetected(event.getMessage().getUnformattedText())) {
            event.setCanceled(true);
        }
    });

    private boolean isDetected(String message) {
        final String OWN_MESSAGE = "^<" + mc.player.getName() + "> ";
        if ((!filterOwn.getValue() && customMatch(OWN_MESSAGE, message)) || isDirect(filterDMs.getValue(), message) || isDirectOther(filterDMs.getValue(), message)) {
            return false;
        } else {
            return isMatched(chatFilter, message);
        }
    }

    private boolean isMatched(ArrayList<Pattern> patterns, String message) {
        for (Pattern pattern : patterns) {
            if (pattern.matcher(message).find()) {
                return true;
            }
        }
        return false;
    }

    private boolean customMatch(String filter, String message) {
        return Pattern.compile(filter, Pattern.CASE_INSENSITIVE).matcher(message).find();
    }

    public void onEnable() {
        BufferedReader bufferedReader;
        try {
            sendChatMessage(getChatName() + " Trying to find '&7chat_filter.txt&f'");
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream("chat_filter.txt"), "UTF-8"));
            String line;
            chatFilter.clear();
            while ((line = bufferedReader.readLine()) != null) {
                while (customMatch("[ ]$", line)) { /* remove trailing spaces */
                    line = line.substring(0, line.length() - 1);
                }
                while (customMatch("^[ ]", line)) {
                    line = line.substring(1); /* remove beginning spaces */
                }
                if (line.length() <= 0) return;
                chatFilter.add(Pattern.compile("\\b" + line + "\\b", Pattern.CASE_INSENSITIVE));
            }
            bufferedReader.close();
        } catch (FileNotFoundException exception) {
            sendErrorMessage(getChatName() + " Couldn't find a file called '&7chat_filter.txt&f' inside your '&7.minecraft&f' folder, disabling");
            disable();
        } catch (IOException exception) {
            sendErrorMessage(exception.toString());
        }
        if (isDisabled()) return;

        sendChatMessage(getChatName() + " Found '&7chat_filter.txt&f'!");

        if (!hasRunInfo.getValue()) {
            sendChatMessage(getChatName() + " Tip: this supports &lregex&r if you know how to use those. This also uses &lword boundaries&r meaning it will match whole words, not part of a word. Eg if your filter has 'hell' then 'hello' will not be filtered.");
            hasRunInfo.setValue(true);
        }
    }
}
