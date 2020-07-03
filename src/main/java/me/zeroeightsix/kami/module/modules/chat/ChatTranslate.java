package me.zeroeightsix.kami.module.modules.chat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.module.Module;
import com.google.gson.*;
import com.google.cloud.translate.*;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import static me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage;

/**
 * Created by sourTaste000 on 7/3/2020
 */
@SuppressWarnings("FieldMayBeFinal")
@Module.Info(
        name = "ChatTranslate",
        description = "Translates Chat",
        category = Module.Category.CHAT
)
public class ChatTranslate extends Module{
    private Setting<Languages> fromLanguage = register(Settings.e("From Language", Languages.TRADITIONAL_CHINESE));
    private Setting<Languages> toLanguage = register(Settings.e("To Language", Languages.ENGLISH));


    Translate translate = TranslateOptions.getDefaultInstance().getService();
    Translation translation;
    String msg;

    enum Languages {
        ENGLISH("en"),
        SPANISH("es"),
        SIMPLIFIED_CHINESE("zh-CN"),
        TRADITIONAL_CHINESE("zh-TW"),
        RUSSIAN("ru"),
        FRENCH("fr"),
        GERMAN("de");

        public final String value;

        Languages(String value) {
            this.value = value;
        }
    }

        @EventHandler
        public Listener<ClientChatReceivedEvent> listener = new Listener<>(event -> {
            msg = event.getMessage().getUnformattedText();
            onUpdate();
        });

    @Override
    public void onUpdate(){
        translation = translate.translate(
                msg,
                Translate.TranslateOption.sourceLanguage(String.valueOf(fromLanguage)),
                Translate.TranslateOption.targetLanguage(String.valueOf(toLanguage)),
                Translate.TranslateOption.model("base")
        );
        sendChatMessage("Translated text (From " + fromLanguage + " to " + toLanguage + ") " + translation.toString());
    }
}
