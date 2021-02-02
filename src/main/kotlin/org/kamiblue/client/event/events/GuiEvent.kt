package org.kamiblue.client.event.events

import org.kamiblue.client.event.Event
import net.minecraft.client.gui.GuiScreen

abstract class GuiEvent(var screen: GuiScreen?) : Event {
    class Displayed(screen: GuiScreen?) : GuiEvent(screen)
    class Closed(screen: GuiScreen?) : GuiEvent(screen)
}