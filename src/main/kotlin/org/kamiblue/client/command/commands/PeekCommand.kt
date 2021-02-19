package org.kamiblue.client.command.commands

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.inventory.GuiShulkerBox
import net.minecraft.item.ItemShulkerBox
import net.minecraft.tileentity.TileEntityShulkerBox
import org.kamiblue.client.command.ClientCommand
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.threads.defaultScope
import org.kamiblue.client.util.threads.onMainThreadSafe
import java.util.*

object PeekCommand : ClientCommand(
    name = "peek",
    alias = arrayOf("shulkerpeek"),
    description = "Look inside the contents of a shulker box without opening it."
) {
    init {
        executeSafe {
            val itemStack = player.inventory.getCurrentItem()
            val item = itemStack.item

            if (item is ItemShulkerBox) {
                val entityBox = TileEntityShulkerBox().apply {
                    this.world = this@executeSafe.world
                }

                val nbtTag = itemStack.tagCompound ?: return@executeSafe
                entityBox.readFromNBT(nbtTag.getCompoundTag("BlockEntityTag"))

                val scaledResolution = ScaledResolution(mc)
                val gui = GuiShulkerBox(player.inventory, entityBox)
                gui.setWorldAndResolution(mc, scaledResolution.scaledWidth, scaledResolution.scaledHeight)

                defaultScope.launch {
                    delay(50L)
                    onMainThreadSafe {
                        mc.displayGuiScreen(gui)
                    }
                }
            } else {
                MessageSendHelper.sendErrorMessage("You aren't holding a shulker box.")
            }
        }
    }
}