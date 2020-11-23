package me.zeroeightsix.kami.mixin.client.accessor.gui

import net.minecraft.client.gui.GuiDisconnected
import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.text.ITextComponent

val GuiDisconnected.parentScreen: GuiScreen get() = (this as AccessorGuiDisconnected).parentScreen

val GuiDisconnected.reason: String get() = (this as AccessorGuiDisconnected).reason

val GuiDisconnected.message: ITextComponent get() = (this as AccessorGuiDisconnected).message