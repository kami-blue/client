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

@SuppressWarnings("FieldMayBeFinal")
@Module.Info(
        name = "ChatTranslate",
        description = "Translates Chat",
        category = Module.Category.CHAT
)
public class ChatTranslate extends Module{
    private Setting<Boolean> fullChat = register(Settings.b("Translate Everything", false));
    private Setting<Boolean> dmsOnly = register(Settings.b("Only Translate /msg", true));
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
                Translate.TranslateOption.sourceLanguage(String.valueOf(Languages.SIMPLIFIED_CHINESE)),
                Translate.TranslateOption.targetLanguage(String.valueOf(Languages.ENGLISH)),
                Translate.TranslateOption.model("base")
        );
        sendChatMessage(translation.toString());
    }
}
