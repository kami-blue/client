package me.zeroeightsix.kami.command.commands

import io.netty.buffer.Unpooled
import me.zeroeightsix.kami.command.ClientCommand
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.item.ItemWritableBook
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTTagString
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.CPacketCustomPayload
import java.util.*
import java.util.stream.Collectors

/**
 * @author 0x2E | PretendingToCode
 * @author EarthComputer
 *
 * The characterGenerator is from here: https://github.com/ImpactDevelopment/ImpactIssues/issues/1123#issuecomment-482721273
 * Which was written by EarthComputer for both EvilSourcerer and 0x2E
 */
object DupeBookCommand : ClientCommand(
    name = "dupebook",
    alias = arrayOf("bookbot"),
    description = "Generates books used for chunk save state dupe."
) {
    init {
        executeSafe {
            val heldItem = player.inventory.getCurrentItem()

            if (heldItem.item is ItemWritableBook) {
                val characterGenerator = Random()
                    .ints(0x80, 0x10ffff - 0x800)
                    .map { if (it < 0xd800) it else it + 0x800 }

                val joinedPages = characterGenerator
                    .limit(50 * 210)
                    .mapToObj { it.toString() }
                    .collect(Collectors.joining())

                val pages = NBTTagList()

                for (page in 0..49) {
                    pages.appendTag(NBTTagString(joinedPages.substring(page * 210, (page + 1) * 210)))
                }

                if (heldItem.hasTagCompound()) {
                    heldItem.tagCompound!!.setTag("pages", pages)
                    heldItem.tagCompound!!.setTag("title", NBTTagString(""))
                    heldItem.tagCompound!!.setTag("author", NBTTagString(Wrapper.player!!.name))
                } else {
                    heldItem.setTagInfo("pages", pages)
                    heldItem.setTagInfo("title", NBTTagString(""))
                    heldItem.setTagInfo("author", NBTTagString(Wrapper.player!!.name))
                }

                val buffer = PacketBuffer(Unpooled.buffer())
                buffer.writeItemStack(heldItem)
                player.connection.sendPacket(CPacketCustomPayload("MC|BEdit", buffer))
                MessageSendHelper.sendChatMessage("Dupe book generated.")
            } else {
                MessageSendHelper.sendErrorMessage("You must be holding a writable book.")
            }

        }
    }
}