package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.client.gui.GuiScreen

abstract class GuiEvent(var screen: GuiScreen?) : KamiEvent() {
    class Displayed(screen: GuiScreen?) : GuiEvent(screen)
    class Closed(screen: GuiScreen?) : GuiEvent(screen)
}