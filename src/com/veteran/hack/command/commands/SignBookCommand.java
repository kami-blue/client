// 
// Decompiled by Procyon v0.5.36
// 

package com.veteran.hack.command.commands;

import com.veteran.hack.command.syntax.SyntaxChunk;
import com.veteran.hack.command.syntax.ChunkBuilder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.PacketBuffer;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.nbt.NBTTagList;
import java.util.ArrayList;
import net.minecraft.item.ItemWritableBook;
import com.veteran.hack.util.Wrapper;
import com.veteran.hack.command.Command;

public class SignBookCommand extends Command
{
    private static final int[] lIlIlIIlIlII;
    
    static {
        llllIIlIIlIII();
    }
    
    public void call(final String[] llllllllllllllIlllIIIIlIlllIIIIl) {
        final ItemStack llllllllllllllIlllIIIIlIlllIIIII = Wrapper.getPlayer().field_71071_by.func_70448_g();
        final int llllllllllllllIlllIIIIlIllIlllll = SignBookCommand.lIlIlIIlIlII[2];
        if (llllIIlIIlIIl(llllllllllllllIlllIIIIlIlllIIIIl.length, SignBookCommand.lIlIlIIlIlII[0])) {
            Command.sendChatMessage("Please specify a title.");
            return;
        }
        if (llllIIlIIlIlI((llllllllllllllIlllIIIIlIlllIIIII.func_77973_b() instanceof ItemWritableBook) ? 1 : 0)) {
            final ArrayList<String> llllllllllllllIlllIIIIlIlllIlIII = new ArrayList<String>();
            int llllllllllllllIlllIIIIlIlllIlIIl = SignBookCommand.lIlIlIIlIlII[1];
            while (llllIIlIIlIll(llllllllllllllIlllIIIIlIlllIlIIl, llllllllllllllIlllIIIIlIlllIIIIl.length)) {
                llllllllllllllIlllIIIIlIlllIlIII.add(llllllllllllllIlllIIIIlIlllIIIIl[llllllllllllllIlllIIIIlIlllIlIIl]);
                "".length();
                ++llllllllllllllIlllIIIIlIlllIlIIl;
                "".length();
                if (" ".length() << (" ".length() << " ".length()) < -" ".length()) {
                    return;
                }
            }
            String llllllllllllllIlllIIIIlIlllIIlll = String.join(" ", llllllllllllllIlllIIIIlIlllIlIII);
            llllllllllllllIlllIIIIlIlllIIlll = llllllllllllllIlllIIIIlIlllIIlll.replaceAll("&", Character.toString((char)llllllllllllllIlllIIIIlIllIlllll));
            llllllllllllllIlllIIIIlIlllIIlll = llllllllllllllIlllIIIIlIlllIIlll.replaceAll("#n", "\n");
            llllllllllllllIlllIIIIlIlllIIlll = llllllllllllllIlllIIIIlIlllIIlll.replaceAll("null", "");
            if (llllIIlIIllII(llllllllllllllIlllIIIIlIlllIIlll.length(), SignBookCommand.lIlIlIIlIlII[3])) {
                Command.sendChatMessage("Title cannot be over 31 characters.");
                return;
            }
            final NBTTagList llllllllllllllIlllIIIIlIlllIIllI = new NBTTagList();
            final String llllllllllllllIlllIIIIlIlllIIlIl = "";
            llllllllllllllIlllIIIIlIlllIIllI.func_74742_a((NBTBase)new NBTTagString(llllllllllllllIlllIIIIlIlllIIlIl));
            final NBTTagCompound llllllllllllllIlllIIIIlIlllIIlII = llllllllllllllIlllIIIIlIlllIIIII.func_77978_p();
            if (llllIIlIIlIlI(llllllllllllllIlllIIIIlIlllIIIII.func_77942_o() ? 1 : 0)) {
                if (llllIIlIIllIl(llllllllllllllIlllIIIIlIlllIIlII)) {
                    llllllllllllllIlllIIIIlIlllIIIII.func_77982_d(llllllllllllllIlllIIIIlIlllIIlII);
                }
                llllllllllllllIlllIIIIlIlllIIIII.func_77978_p().func_74782_a("title", (NBTBase)new NBTTagString(llllllllllllllIlllIIIIlIlllIIlll));
                llllllllllllllIlllIIIIlIlllIIIII.func_77978_p().func_74782_a("author", (NBTBase)new NBTTagString(Wrapper.getPlayer().func_70005_c_()));
                "".length();
                if (null != null) {
                    return;
                }
            }
            else {
                llllllllllllllIlllIIIIlIlllIIIII.func_77983_a("pages", (NBTBase)llllllllllllllIlllIIIIlIlllIIllI);
                llllllllllllllIlllIIIIlIlllIIIII.func_77983_a("title", (NBTBase)new NBTTagString(llllllllllllllIlllIIIIlIlllIIlll));
                llllllllllllllIlllIIIIlIlllIIIII.func_77983_a("author", (NBTBase)new NBTTagString(Wrapper.getPlayer().func_70005_c_()));
            }
            final PacketBuffer llllllllllllllIlllIIIIlIlllIIIll = new PacketBuffer(Unpooled.buffer());
            llllllllllllllIlllIIIIlIlllIIIll.func_150788_a(llllllllllllllIlllIIIIlIlllIIIII);
            "".length();
            Wrapper.getPlayer().field_71174_a.func_147297_a((Packet)new CPacketCustomPayload("MC|BSign", llllllllllllllIlllIIIIlIlllIIIll));
            Command.sendChatMessage(String.valueOf(new StringBuilder().append("Signed book with title: ").append(llllllllllllllIlllIIIIlIlllIIlll).append("&r")));
            "".length();
            if (" ".length() << (" ".length() << " ".length()) <= "   ".length()) {
                return;
            }
        }
        else {
            Command.sendChatMessage("You must be holding a writable book.");
        }
    }
    
    private static boolean llllIIlIIlIll(final int llllllllllllllIlllIIIIlIllIIllll, final int llllllllllllllIlllIIIIlIllIIlllI) {
        return llllllllllllllIlllIIIIlIllIIllll < llllllllllllllIlllIIIIlIllIIlllI;
    }
    
    private static boolean llllIIlIIllIl(final Object llllllllllllllIlllIIIIlIllIIlIII) {
        return llllllllllllllIlllIIIIlIllIIlIII != null;
    }
    
    public SignBookCommand() {
        final String s = "signbook";
        final SyntaxChunk[] build = new ChunkBuilder().append("name").build();
        final String[] array = new String[SignBookCommand.lIlIlIIlIlII[0]];
        array[SignBookCommand.lIlIlIIlIlII[1]] = "sign";
        super(s, build, array);
        this.setDescription("Colored book names. &f#n&7 for a new line and &f&&7 for colour codes");
    }
    
    private static void llllIIlIIlIII() {
        (lIlIlIIlIlII = new int[4])[0] = " ".length();
        SignBookCommand.lIlIlIIlIlII[1] = ("   ".length() << "   ".length() & ~("   ".length() << "   ".length()));
        SignBookCommand.lIlIlIIlIlII[2] = 13 + 111 - 53 + 96;
        SignBookCommand.lIlIlIIlIlII[3] = (0xAA ^ 0xB5);
    }
    
    private static boolean llllIIlIIlIlI(final int llllllllllllllIlllIIIIlIllIIIllI) {
        return llllllllllllllIlllIIIIlIllIIIllI != 0;
    }
    
    private static boolean llllIIlIIlIIl(final int llllllllllllllIlllIIIIlIllIlIIll, final int llllllllllllllIlllIIIIlIllIlIIlI) {
        return llllllllllllllIlllIIIIlIllIlIIll == llllllllllllllIlllIIIIlIllIlIIlI;
    }
    
    private static boolean llllIIlIIllII(final int llllllllllllllIlllIIIIlIllIIlIll, final int llllllllllllllIlllIIIIlIllIIlIlI) {
        return llllllllllllllIlllIIIIlIllIIlIll > llllllllllllllIlllIIIIlIllIIlIlI;
    }
}
