package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.util.text.ITextComponent

/**
 * Updated by Xiaro on 18/08/20
 */
class PrintChatMessageEvent(val chatComponent: ITextComponent, val unformatted: String) : KamiEvent()