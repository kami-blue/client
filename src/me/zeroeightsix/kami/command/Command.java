// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.util.text.TextComponentBase;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.launchwrapper.LogWrapper;
import net.minecraft.util.text.ITextComponent;
import me.zeroeightsix.kami.util.Wrapper;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.command.syntax.SyntaxChunk;
import net.minecraft.client.Minecraft;

public abstract class Command
{
    protected String label;
    protected String syntax;
    protected String description;
    public final Minecraft mc;
    protected SyntaxChunk[] syntaxChunks;
    public static Setting<String> commandPrefix;
    public static final char SECTION_SIGN = '§';
    
    public Command(final String label, final SyntaxChunk[] syntaxChunks) {
        this.mc = Minecraft.func_71410_x();
        this.label = label;
        this.syntaxChunks = syntaxChunks;
        this.description = "Descriptionless";
    }
    
    public static void sendChatMessage(final String message) {
        sendRawChatMessage("&0\u300e&9AstraMod&0\u300f &r" + message);
    }
    
    public static void sendErrorMessage(final String message) {
        sendRawChatMessage("&0\u300e&9AstraMod&0\u300f &r" + message);
    }
    
    public static void sendWarningMessage(final String message) {
        sendRawChatMessage("&0\u300e&9AstraMod&0\u300f &r" + message);
    }
    
    public static void sendStringChatMessage(final String[] messages) {
        sendChatMessage("");
        for (final String s : messages) {
            sendRawChatMessage(s);
        }
    }
    
    public static void sendRawChatMessage(final String message) {
        if (isSendable()) {
            Wrapper.getPlayer().func_145747_a((ITextComponent)new ChatMessage(message));
        }
        else {
            LogWrapper.info("KAMI Blue: Avoided NPE by logging to file instead of chat\n" + message, new Object[0]);
        }
    }
    
    public static boolean isSendable() {
        return Minecraft.func_71410_x().field_71439_g != null;
    }
    
    protected void setDescription(final String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public static String getCommandPrefix() {
        return Command.commandPrefix.getValue();
    }
    
    public String getLabel() {
        return this.label;
    }
    
    public abstract void call(final String[] p0);
    
    public SyntaxChunk[] getSyntaxChunks() {
        return this.syntaxChunks;
    }
    
    protected SyntaxChunk getSyntaxChunk(final String name) {
        for (final SyntaxChunk c : this.syntaxChunks) {
            if (c.getType().equals(name)) {
                return c;
            }
        }
        return null;
    }
    
    public static char SECTIONSIGN() {
        return '§';
    }
    
    static {
        Command.commandPrefix = Settings.s("commandPrefix", ".");
    }
    
    public static class ChatMessage extends TextComponentBase
    {
        String text;
        
        public ChatMessage(final String text) {
            final Pattern p = Pattern.compile("&[0123456789abcdefrlosmk]");
            final Matcher m = p.matcher(text);
            final StringBuffer sb = new StringBuffer();
            while (m.find()) {
                final String replacement = "§" + m.group().substring(1);
                m.appendReplacement(sb, replacement);
            }
            m.appendTail(sb);
            this.text = sb.toString();
        }
        
        public String func_150261_e() {
            return this.text;
        }
        
        public ITextComponent func_150259_f() {
            return (ITextComponent)new ChatMessage(this.text);
        }
    }
}
