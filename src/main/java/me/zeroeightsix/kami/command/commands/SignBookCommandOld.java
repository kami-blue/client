package me.zeroeightsix.kami.command.commands;

import io.netty.buffer.Unpooled;
import me.zeroeightsix.kami.command.CommandOld;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;

import java.util.ArrayList;
import java.util.Arrays;

import static me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage;

/**
 * @author 0x2E | PretendingToCode
 */
public class SignBookCommandOld extends CommandOld {

    public SignBookCommandOld() {
        super("signbook", new ChunkBuilder().append("name").build(), "sign");
        setDescription("Colored book names. &f#n&7 for a new line and &f&&7 for colour codes");
    }

    @Override
    public void call(String[] args) {
        ItemStack item = Wrapper.getPlayer().inventory.getCurrentItem();
        int c = 0x00A7;

        if (args.length == 1) {
            sendChatMessage("Please specify a title.");
            return;
        }

        if (item.getItem() instanceof ItemWritableBook) {

            ArrayList<String> toAdd = new ArrayList<>(Arrays.asList(args));

            String futureTitle = String.join(" ", toAdd);
            futureTitle = futureTitle.replaceAll("&", Character.toString((char) c));
            futureTitle = futureTitle.replaceAll("#n", "\n");
            futureTitle = futureTitle.replaceAll("null", ""); // Random extra null added sometimes

            if (futureTitle.length() > 31) {
                sendChatMessage("Title cannot be over 31 characters.");
                return;
            }

            NBTTagList pages = new NBTTagList();
            String pageText = "";
            pages.appendTag(new NBTTagString(pageText));

            NBTTagCompound bookData = item.getTagCompound();

            if (item.hasTagCompound()) {
                if (bookData != null) {
                    item.setTagCompound(bookData);
                }
                item.getTagCompound().setTag("title", new NBTTagString(futureTitle));
                item.getTagCompound().setTag("author", new NBTTagString(Wrapper.getPlayer().getName()));
            } else {
                item.setTagInfo("pages", pages);
                item.setTagInfo("title", new NBTTagString(futureTitle));
                item.setTagInfo("author", new NBTTagString(Wrapper.getPlayer().getName()));
            }

            PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
            buf.writeItemStack(item);

            Wrapper.getPlayer().connection.sendPacket(new CPacketCustomPayload("MC|BSign", buf));
            sendChatMessage("Signed book with title: " + futureTitle + "&r");
        } else {
            sendChatMessage("You must be holding a writable book.");
        }
    }
}
