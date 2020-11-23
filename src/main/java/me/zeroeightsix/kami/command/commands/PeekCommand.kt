package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.syntax.SyntaxChunk
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.inventory.GuiShulkerBox
import net.minecraft.item.ItemShulkerBox
import net.minecraft.tileentity.TileEntityShulkerBox
import java.util.*

class PeekCommand : Command("peek", SyntaxChunk.EMPTY) {
    override fun call(args: Array<String>) {
        Wrapper.world?.let { world ->
            Wrapper.player?.let { player ->
                val itemStack = player.inventory.getCurrentItem()
                val item = itemStack.item
                if (item is ItemShulkerBox) {
                    val entityBox = TileEntityShulkerBox().apply {
                        this.world = world
                    }
                    val nbtTag = itemStack.tagCompound ?: return
                    entityBox.readFromNBT(nbtTag.getCompoundTag("BlockEntityTag"))

                    val scaledResolution = ScaledResolution(mc)
                    val scaledWidth = scaledResolution.scaledWidth
                    val scaledHeight = scaledResolution.scaledHeight
                    val gui = GuiShulkerBox(player.inventory, entityBox)
                    gui.setWorldAndResolution(mc, scaledWidth, scaledHeight)

                    Timer().schedule(object : TimerTask() {
                        override fun run() {
                            mc.displayGuiScreen(gui)
                        }
                    }, 50L)
                } else {
                    sendChatMessage("You aren't carrying a shulker box.")
                }
            }
        }
    }

    init {
        setDescription("Look inside the contents of a shulker box without opening it")
    }
}