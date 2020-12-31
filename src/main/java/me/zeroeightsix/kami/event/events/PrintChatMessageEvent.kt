package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.util.text.ITextComponent

class PrintChatMessageEvent(val chatComponent: ITextComponent) : KamiEvent()