package org.kamiblue.client.event.events

import org.kamiblue.client.event.Event
import net.minecraft.util.text.ITextComponent

class PrintChatMessageEvent(val chatComponent: ITextComponent) : Event