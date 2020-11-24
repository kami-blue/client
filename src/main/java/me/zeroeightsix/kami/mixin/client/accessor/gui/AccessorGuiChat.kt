package me.zeroeightsix.kami.mixin.client.accessor.gui

import net.minecraft.client.gui.GuiChat

var GuiChat.historyBuffer: String
    get() = (this as AccessorGuiChat).historyBuffer
    set(value) {
        (this as AccessorGuiChat).historyBuffer = value
    }

var GuiChat.sentHistoryCursor: Int
    get() = (this as AccessorGuiChat).sentHistoryCursor
    set(value) {
        (this as AccessorGuiChat).sentHistoryCursor = value
    }