package org.kamiblue.client.command.commands

import net.minecraft.item.ItemWritableBook
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTTagString
import org.kamiblue.client.command.ClientCommand
import org.kamiblue.client.event.SafeExecuteEvent
import org.kamiblue.client.util.items.itemPayload
import org.kamiblue.client.util.text.MessageSendHelper
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
        boolean("sign book") { signBookArg ->
            executeSafe {
                createBook(signBookArg.value)
            }
        }

        executeSafe {
            createBook(false)
        }
    }

    private fun SafeExecuteEvent.createBook(sign: Boolean) {
        val heldItem = player.inventory.getCurrentItem()

        if (heldItem.item is ItemWritableBook) {
            val characterGenerator = Random()
                .ints(0x80, 0x10ffff - 0x800)
                .map { if (it < 0xd800) it else it + 0x800 }

            val joinedPages = characterGenerator
                .limit(50 * 210)
                .mapToObj { it.toChar().toString() } // this has to be turned into a Char first, otherwise you will get the raw Int value
                .collect(Collectors.joining())

            val pages = NBTTagList()
            val title = if (sign) UUID.randomUUID().toString().substring(0, 5) else ""

            for (page in 0..49) {
                pages.appendTag(NBTTagString(joinedPages.substring(page * 210, (page + 1) * 210)))
            }

            if (heldItem.hasTagCompound()) {
                heldItem.tagCompound!!.setTag("pages", pages)
                heldItem.tagCompound!!.setTag("title", NBTTagString(title))
                heldItem.tagCompound!!.setTag("author", NBTTagString(player.name))
            } else {
                heldItem.setTagInfo("pages", pages)
                heldItem.setTagInfo("title", NBTTagString(title))
                heldItem.setTagInfo("author", NBTTagString(player.name))
            }

            itemPayload(heldItem, "MC|BEdit")

            if (sign) {
                itemPayload(heldItem, "MC|BSign")
            }

            MessageSendHelper.sendChatMessage("Dupe book generated.")
        } else {
            MessageSendHelper.sendErrorMessage("You must be holding a writable book.")
        }
    }

}