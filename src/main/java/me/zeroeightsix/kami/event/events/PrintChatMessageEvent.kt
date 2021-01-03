package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.Event
import net.minecraft.util.text.ITextComponent

class PrintChatMessageEvent(val chatComponent: ITextComponent) : Event