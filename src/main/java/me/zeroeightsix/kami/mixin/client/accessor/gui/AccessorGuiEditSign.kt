package me.zeroeightsix.kami.mixin.client.accessor.gui

import net.minecraft.client.gui.inventory.GuiEditSign
import net.minecraft.tileentity.TileEntitySign

val GuiEditSign.tileSign: TileEntitySign get() = (this as AccessorGuiEditSign).tileSign

val GuiEditSign.edtiLine: Int get() = (this as AccessorGuiEditSign).editLine