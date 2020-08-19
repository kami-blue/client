package me.zeroeightsix.kami.event.events;

import me.zeroeightsix.kami.event.KamiEvent;
import net.minecraft.util.text.ITextComponent;

public class PrintChatMessageEvent extends KamiEvent {
    private String unformatted;
    private ITextComponent chatComponent;

    public PrintChatMessageEvent(ITextComponent chatComponent, String unformatted) {
        this.chatComponent = chatComponent;
        this.unformatted = unformatted;
    }

    public ITextComponent getChatComponent() {
        return chatComponent;
    }

    public String getUnformatted() {
        return unformatted;
    }
}
