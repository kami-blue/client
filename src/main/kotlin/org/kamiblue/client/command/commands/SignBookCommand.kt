package org.kamiblue.client.command.commands

import net.minecraft.item.ItemWritableBook
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTTagString
import org.kamiblue.client.command.ClientCommand
import org.kamiblue.client.util.items.itemPayload
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.text.MessageSendHelper.sendChatMessage
import org.kamiblue.client.util.text.formatValue
import org.kamiblue.commons.extension.max

object SignBookCommand : ClientCommand(
    name = "signbook",
    alias = arrayOf("sign"),
    description = "Colored book names. &f#n&7 for a new line and &f&&7 for color codes"
) {
    init {
        greedy("title") { titleArg ->
            executeSafe {
                val item = player.inventory.getCurrentItem()

                if (item.item is ItemWritableBook) {
                    val title = titleArg.value
                        .replace("&", 0x00A7.toString())
                        .replace("#n", "\n")
                        .replace("null", "")
                        .max(31)

                    val pages = NBTTagList()
                    val bookData = item.tagCompound // have to save this
                    pages.appendTag(NBTTagString(""))

                    if (item.hasTagCompound()) {
                        bookData?.let { item.tagCompound = it }
                        item.tagCompound!!.setTag("title", NBTTagString(title))
                        item.tagCompound!!.setTag("author", NBTTagString(player.name))
                    } else {
                        item.setTagInfo("pages", pages)
                        item.setTagInfo("title", NBTTagString(title))
                        item.setTagInfo("author", NBTTagString(player.name))
                    }

                    itemPayload(item, "MC|BSign")
                    sendChatMessage("Signed book with title: ${formatValue(title)}")
                } else {
                    MessageSendHelper.sendErrorMessage("You're not holding a writable book!")
                }
            }
        }
    }
}
