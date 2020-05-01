package me.zeroeightsix.kami.module.modules.chat;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static me.zeroeightsix.kami.util.MessageSendHelper.*;

/**
 * @author dominikaaaa
 * Created by dominikaaaa on 10/04/20
 */
@Module.Info(
        name = "Spammer",
        description = "Spams text from a file on a set delay into the chat",
        category = Module.Category.CHAT
)
public class Spammer extends Module {
    private Setting<Integer> timeoutTime = register(Settings.integerBuilder().withName("Timeout (s)").withMinimum(1).withMaximum(240).withValue(10).build());

    List<String> tempLines = new ArrayList<>();
    String[] spammer;

    public void onEnable() {
        BufferedReader bufferedReader;
        try {
            sendChatMessage(getChatName() + "Trying to find '&7spammer.txt&f'");
            bufferedReader = new BufferedReader(new FileReader("spammer.txt"));
            String line;
            tempLines.clear();
            while ((line = bufferedReader.readLine()) != null) {
                tempLines.add(line);
            }
            bufferedReader.close();
            spammer = tempLines.toArray(new String[]{});
        } catch (FileNotFoundException exception) {
            sendErrorMessage(getChatName() + "Couldn't find a file called '&7spammer.txt&f' inside your '&7.minecraft&f' folder, disabling");
            disable();
            return;
        } catch (IOException exception) {
            sendErrorMessage(exception.toString());
            return;
        }
        sendChatMessage(getChatName() + "Found '&7spammer.txt&f'!");

    }

    public void onUpdate() {
        sendMsg(getRandom(spammer));
    }

    private static long startTime = 0;
    private void sendMsg(String message) {
        if (startTime == 0) startTime = System.currentTimeMillis();
        if (startTime + (timeoutTime.getValue() * 1000) <= System.currentTimeMillis()) { // 1 timeout = 1 second = 1000 ms
            startTime = System.currentTimeMillis();
            sendServerMessage(message);
        }
    }

    public static String getRandom(String[] array) {
        int rand = new Random().nextInt(array.length);
        while (array[rand].isEmpty() || array[rand].equals(" ")) {
            rand = new Random().nextInt(array.length); // big meme to try to avoid sending empty messages
        }
        return array[rand];
    }
}
