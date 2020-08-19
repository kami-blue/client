package me.zeroeightsix.kami.event.events

import net.minecraft.client.gui.GuiScreen

/**
 * Created by 086 on 17/11/2017.
 * Updated by Xiaro on 18/08/20
 */
open class GuiScreenEvent(var screen: GuiScreen?) {

    class Displayed(screen: GuiScreen?) : GuiScreenEvent(screen)
    class Closed(screen: GuiScreen?) : GuiScreenEvent(screen)
}