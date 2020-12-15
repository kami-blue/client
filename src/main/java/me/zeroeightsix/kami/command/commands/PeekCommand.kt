package me.zeroeightsix.kami.command.commands

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.inventory.GuiShulkerBox
import net.minecraft.item.ItemShulkerBox
import net.minecraft.tileentity.TileEntityShulkerBox
import java.util.*

object PeekCommand : ClientCommand(
    name = "peek",
    alias = arrayOf("shulkerpeek"),
    description = "Look inside the contents of a shulker box without opening it."
) {
    init {
        execute {
            if (world == null || player == null) return@execute

            val itemStack = player.inventory.getCurrentItem()
            val item = itemStack.item
            if (item is ItemShulkerBox) {
                val entityBox = TileEntityShulkerBox().apply {
                    this.world = this@execute.world
                }
                val nbtTag = itemStack.tagCompound ?: return@execute
                entityBox.readFromNBT(nbtTag.getCompoundTag("BlockEntityTag"))

                val scaledResolution = ScaledResolution(mc)
                val gui = GuiShulkerBox(player.inventory, entityBox)
                gui.setWorldAndResolution(mc, scaledResolution.scaledWidth, scaledResolution.scaledHeight)

                commandScope.launch {
                    delay(50L)
                    onMainThread {
                        mc.displayGuiScreen(gui)
                    }
                }
            } else {
                MessageSendHelper.sendChatMessage("You aren't carrying a shulker box.")
            }
        }
    }
}