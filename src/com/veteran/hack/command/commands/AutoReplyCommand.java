// 
// Decompiled by Procyon v0.5.36
// 

package com.veteran.hack.command.commands;

import com.veteran.hack.command.syntax.SyntaxChunk;
import com.veteran.hack.command.syntax.ChunkBuilder;
import com.veteran.hack.module.ModuleManager;
import com.veteran.hack.module.modules.chat.AutoReply;
import com.veteran.hack.command.Command;

public class AutoReplyCommand extends Command
{
    private static final int[] lIIllIllllll;
    
    private static boolean llIllllIlllIl(final int llllllllllllllIlllllIlIlIIIlIlII, final int llllllllllllllIlllllIlIlIIIlIIll) {
        return llllllllllllllIlllllIlIlIIIlIlII < llllllllllllllIlllllIlIlIIIlIIll;
    }
    
    public void call(final String[] llllllllllllllIlllllIlIlIIIlllIl) {
        final AutoReply llllllllllllllIlllllIlIlIIIllllI = (AutoReply)ModuleManager.getModuleByName("AutoReply");
        if (llIllllIllIll(llllllllllllllIlllllIlIlIIIllllI)) {
            Command.sendErrorMessage("&cThe AutoReply module is not available for some reason. Make sure the name you're calling is correct and that you have the module installed!!");
            return;
        }
        if (llIllllIlllII(llllllllllllllIlllllIlIlIIIllllI.isEnabled() ? 1 : 0)) {
            Command.sendWarningMessage("&6Warning: The AutoReply module is not enabled!");
            Command.sendWarningMessage("The command will still work, but will not visibly do anything.");
        }
        final boolean llllllllllllllIlllllIlIlIIIllIll = (Object)llllllllllllllIlllllIlIlIIIlllIl;
        final int llllllllllllllIlllllIlIlIIIllIlI = llllllllllllllIlllllIlIlIIIllIll.length;
        int llllllllllllllIlllllIlIlIIIllIIl = AutoReplyCommand.lIIllIllllll[1];
        while (llIllllIlllIl(llllllllllllllIlllllIlIlIIIllIIl, llllllllllllllIlllllIlIlIIIllIlI)) {
            final String llllllllllllllIlllllIlIlIIlIIIIl = llllllllllllllIlllllIlIlIIIllIll[llllllllllllllIlllllIlIlIIIllIIl];
            if (llIllllIllIll(llllllllllllllIlllllIlIlIIlIIIIl)) {
                "".length();
                if (" ".length() << (" ".length() << " ".length()) < "   ".length()) {
                    return;
                }
            }
            else if (llIllllIllllI(llllllllllllllIlllllIlIlIIlIIIIl.startsWith("=") ? 1 : 0)) {
                final String llllllllllllllIlllllIlIlIIlIIIll = llllllllllllllIlllllIlIlIIlIIIIl.replace("=", "");
                llllllllllllllIlllllIlIlIIIllllI.listener.setValue((Object)llllllllllllllIlllllIlIlIIlIIIll);
                "".length();
                Command.sendChatMessage(String.valueOf(new StringBuilder().append("Set the AutoReply listener to <").append(llllllllllllllIlllllIlIlIIlIIIll).append(">")));
                if (llIllllIlllII(((boolean)llllllllllllllIlllllIlIlIIIllllI.customListener.getValue()) ? 1 : 0)) {
                    Command.sendWarningMessage("&6Warning: You don't have Custom Listener enabled in AutoReply!");
                    Command.sendWarningMessage("The command will still work, but will not visibly do anything.");
                }
                "".length();
                if (" ".length() == 0) {
                    return;
                }
            }
            else if (llIllllIllllI(llllllllllllllIlllllIlIlIIlIIIIl.startsWith("-") ? 1 : 0)) {
                final String llllllllllllllIlllllIlIlIIlIIIlI = llllllllllllllIlllllIlIlIIlIIIIl.replace("-", "");
                llllllllllllllIlllllIlIlIIIllllI.replyCommand.setValue((Object)llllllllllllllIlllllIlIlIIlIIIlI);
                "".length();
                Command.sendChatMessage(String.valueOf(new StringBuilder().append("Set the AutoReply reply command to <").append(llllllllllllllIlllllIlIlIIlIIIlI).append(">")));
                if (llIllllIlllII(((boolean)llllllllllllllIlllllIlIlIIIllllI.customReplyCommand.getValue()) ? 1 : 0)) {
                    Command.sendWarningMessage("&6Warning: You don't have Custom Reply Command enabled in AutoReply!");
                    Command.sendWarningMessage("The command will still work, but will not visibly do anything.");
                }
                "".length();
                if (" ".length() == 0) {
                    return;
                }
            }
            else {
                llllllllllllllIlllllIlIlIIIllllI.message.setValue((Object)llllllllllllllIlllllIlIlIIlIIIIl);
                "".length();
                Command.sendChatMessage(String.valueOf(new StringBuilder().append("Set the AutoReply message to <").append(llllllllllllllIlllllIlIlIIlIIIIl).append(">")));
                if (llIllllIlllII(((boolean)llllllllllllllIlllllIlIlIIIllllI.customMessage.getValue()) ? 1 : 0)) {
                    Command.sendWarningMessage("&6Warning: You don't have Custom Message enabled in AutoReply!");
                    Command.sendWarningMessage("The command will still work, but will not visibly do anything.");
                }
            }
            ++llllllllllllllIlllllIlIlIIIllIIl;
            "".length();
            if (" ".length() != " ".length()) {
                return;
            }
        }
    }
    
    static {
        llIllllIllIlI();
    }
    
    private static boolean llIllllIlllII(final int llllllllllllllIlllllIlIlIIIIllIl) {
        return llllllllllllllIlllllIlIlIIIIllIl == 0;
    }
    
    private static boolean llIllllIllllI(final int llllllllllllllIlllllIlIlIIIIllll) {
        return llllllllllllllIlllllIlIlIIIIllll != 0;
    }
    
    public AutoReplyCommand() {
        final String s = "autoreply";
        final SyntaxChunk[] build = new ChunkBuilder().append("message").append("=listener").append("-replyCommand").build();
        final String[] array = new String[AutoReplyCommand.lIIllIllllll[0]];
        array[AutoReplyCommand.lIIllIllllll[1]] = "reply";
        super(s, build, array);
        this.setDescription("Allows you to customize AutoReply's settings");
    }
    
    private static void llIllllIllIlI() {
        (lIIllIllllll = new int[2])[0] = " ".length();
        AutoReplyCommand.lIIllIllllll[1] = ((0xCF ^ 0xC4) << "   ".length() & ~((0x5D ^ 0x56) << "   ".length()));
    }
    
    private static boolean llIllllIllIll(final Object llllllllllllllIlllllIlIlIIIlIIIl) {
        return llllllllllllllIlllllIlIlIIIlIIIl == null;
    }
}
