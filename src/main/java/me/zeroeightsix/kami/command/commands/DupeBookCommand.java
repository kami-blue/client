package me.zeroeightsix.kami.command.commands;

import io.netty.buffer.Unpooled;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static me.zeroeightsix.kami.util.text.MessageSendHelper.sendChatMessage;
import static me.zeroeightsix.kami.util.text.MessageSendHelper.sendErrorMessage;

/**
 * @author 0x2E | PretendingToCode
 * @author EarthComputer
 * <p>
 * The characterGenerator is from here: https://github.com/ImpactDevelopment/ImpactIssues/issues/1123#issuecomment-482721273
 * Which was written by EarthComputer for both EvilSourcerer and 0x2E
 */
public class DupeBookCommand extends Command {

    public DupeBookCommand() {
        super("dupebook", new ChunkBuilder().append("name").build(), "bookbot");
        setDescription("Generates books used for chunk savestate dupe.");
    }

    @Override
    public void call(String[] args) {
        ItemStack heldItem = Wrapper.getPlayer().inventory.getCurrentItem();

        if (heldItem.getItem() instanceof ItemWritableBook) {
            IntStream characterGenerator = new Random().ints(0x80, 0x10ffff - 0x800).map(i -> i < 0xd800 ? i : i + 0x800);
            NBTTagList pages = new NBTTagList();
            String joinedPages = characterGenerator.limit(50 * 210).mapToObj(i -> String.valueOf((char) i)).collect(Collectors.joining());

            for (int page = 0; page < 50; page++) {
                pages.appendTag(new NBTTagString(joinedPages.substring(page * 210, (page + 1) * 210)));
            }

            if (heldItem.hasTagCompound()) {
                assert heldItem.getTagCompound() != null;
                heldItem.getTagCompound().setTag("pages", pages);
                heldItem.getTagCompound().setTag("title", new NBTTagString(""));
                heldItem.getTagCompound().setTag("author", new NBTTagString(Wrapper.getPlayer().getName()));
            } else {
                heldItem.setTagInfo("pages", pages);
                heldItem.setTagInfo("title", new NBTTagString(""));
                heldItem.setTagInfo("author", new NBTTagString(Wrapper.getPlayer().getName()));
            }

            PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
            buf.writeItemStack(heldItem);

            Wrapper.getPlayer().connection.sendPacket(new CPacketCustomPayload("MC|BEdit", buf));
            sendChatMessage("Dupe book generated.");
        } else {
            sendErrorMessage("You must be holding a writable book.");
        }
    }
}
