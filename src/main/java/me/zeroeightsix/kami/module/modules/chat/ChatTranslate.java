package me.zeroeightsix.kami.module.modules.chat;

import me.zeroeightsix.kami.module.Module;
import com.google.gson.*;
import com.google.cloud.translate.*;
import me.zeroeightsix.kami.util.MessageDetectionHelper;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage;

@Module.Info(
        name = "ChatTranslate",
        description = "Translates Chat",
        category = Module.Category.CHAT
)
public class ChatTranslate extends Module{
    public ChatTranslate(){}
    Translate translate = TranslateOptions.getDefaultInstance().getService();
    String msg;
    Translation translation;

    @Override
    public void onEnable(){
        if(MessageDetectionHelper.isDirect(true, msg)){
            translation = translate.translate(msg);
            sendChatMessage(translation.toString());
        }
    }

    @Override
    public void onUpdate(){
        if(MessageDetectionHelper.isDirect(true, msg)){
            translation = translate.translate(msg);
            sendChatMessage(translation.toString());
        }
    }
}
