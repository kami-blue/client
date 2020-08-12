// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.mc;

import net.minecraft.util.text.ITextComponent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.input.Mouse;
import me.zeroeightsix.kami.command.syntax.SyntaxChunk;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import me.zeroeightsix.kami.KamiMod;
import java.util.HashMap;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;
import me.zeroeightsix.kami.command.Command;
import net.minecraft.client.gui.GuiChat;

public class KamiGuiChat extends GuiChat
{
    private String startString;
    private String currentFillinLine;
    private int cursor;
    
    public KamiGuiChat(final String startString, final String historybuffer, final int sentHistoryCursor) {
        super(startString);
        this.startString = startString;
        if (!startString.equals(Command.getCommandPrefix())) {
            this.calculateCommand(startString.substring(Command.getCommandPrefix().length()));
        }
        this.field_146410_g = historybuffer;
        this.cursor = sentHistoryCursor;
    }
    
    protected void func_73869_a(final char typedChar, final int keyCode) throws IOException {
        this.field_146416_h = this.cursor;
        super.func_73869_a(typedChar, keyCode);
        this.cursor = this.field_146416_h;
        final String chatLine = this.field_146415_a.func_146179_b();
        if (Command.getCommandPrefix() != null && !chatLine.startsWith(Command.getCommandPrefix())) {
            final GuiChat newGUI = new GuiChat(chatLine) {
                int cursor = KamiGuiChat.this.cursor;
                
                protected void func_73869_a(final char typedChar, final int keyCode) throws IOException {
                    this.field_146416_h = this.cursor;
                    super.func_73869_a(typedChar, keyCode);
                    this.cursor = this.field_146416_h;
                }
            };
            newGUI.field_146410_g = this.field_146410_g;
            this.field_146297_k.func_147108_a((GuiScreen)newGUI);
            return;
        }
        if (chatLine.equals(Command.getCommandPrefix())) {
            this.currentFillinLine = "";
            return;
        }
        this.calculateCommand(chatLine.substring(Command.getCommandPrefix().length()));
    }
    
    protected void calculateCommand(final String line) {
        final String[] args = line.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        final HashMap<String, Command> options = new HashMap<String, Command>();
        if (args.length == 0) {
            return;
        }
        for (final Command c : KamiMod.getInstance().getCommandManager().getCommands()) {
            if ((c.getLabel().startsWith(args[0]) && !line.endsWith(" ")) || c.getLabel().equals(args[0])) {
                options.put(c.getLabel(), c);
            }
        }
        if (options.isEmpty()) {
            this.currentFillinLine = "";
            return;
        }
        final TreeMap<String, Command> map = new TreeMap<String, Command>(options);
        final Command alphaCommand = map.firstEntry().getValue();
        this.currentFillinLine = alphaCommand.getLabel().substring(args[0].length());
        if (alphaCommand.getSyntaxChunks() == null || alphaCommand.getSyntaxChunks().length == 0) {
            return;
        }
        if (!line.endsWith(" ")) {
            this.currentFillinLine += " ";
        }
        final SyntaxChunk[] chunks = alphaCommand.getSyntaxChunks();
        boolean cutSpace = false;
        for (int i = 0; i < chunks.length; ++i) {
            if (i + 1 >= args.length - 1) {
                final SyntaxChunk c2 = chunks[i];
                final String result = c2.getChunk(chunks, c2, args, (i + 1 == args.length - 1) ? args[i + 1] : null);
                if (result != "" && (!result.startsWith("<") || !result.endsWith(">")) && (!result.startsWith("[") || !result.endsWith("]"))) {
                    cutSpace = true;
                }
                this.currentFillinLine = this.currentFillinLine + result + ((result == "") ? "" : " ") + "";
            }
        }
        if (cutSpace) {
            this.currentFillinLine = this.currentFillinLine.substring(1);
        }
    }
    
    public void func_73863_a(final int mouseX, final int mouseY, final float partialTicks) {
        func_73734_a(2, this.field_146295_m - 14, this.field_146294_l - 2, this.field_146295_m - 2, Integer.MIN_VALUE);
        final int x = this.field_146415_a.field_146211_a.func_78256_a(this.field_146415_a.func_146179_b() + "") + 4;
        final int y = this.field_146415_a.func_146181_i() ? (this.field_146415_a.field_146210_g + (this.field_146415_a.field_146219_i - 8) / 2) : this.field_146415_a.field_146210_g;
        this.field_146415_a.field_146211_a.func_175063_a(this.currentFillinLine, (float)x, (float)y, 6710886);
        this.field_146415_a.func_146194_f();
        final ITextComponent itextcomponent = this.field_146297_k.field_71456_v.func_146158_b().func_146236_a(Mouse.getX(), Mouse.getY());
        if (itextcomponent != null && itextcomponent.func_150256_b().func_150210_i() != null) {
            this.func_175272_a(itextcomponent, mouseX, mouseY);
        }
        final boolean a = GL11.glIsEnabled(3042);
        final boolean b = GL11.glIsEnabled(3553);
        GL11.glDisable(3042);
        GL11.glDisable(3553);
        GL11.glColor3f(0.8f, 0.1f, 0.0f);
        GL11.glBegin(1);
        GL11.glVertex2f((float)(this.field_146415_a.field_146209_f - 2), (float)(this.field_146415_a.field_146210_g - 2));
        GL11.glVertex2f((float)(this.field_146415_a.field_146209_f + this.field_146415_a.func_146200_o() - 2), (float)(this.field_146415_a.field_146210_g - 2));
        GL11.glVertex2f((float)(this.field_146415_a.field_146209_f + this.field_146415_a.func_146200_o() - 2), (float)(this.field_146415_a.field_146210_g - 2));
        GL11.glVertex2f((float)(this.field_146415_a.field_146209_f + this.field_146415_a.func_146200_o() - 2), (float)(this.field_146415_a.field_146210_g + this.field_146415_a.field_146219_i - 2));
        GL11.glVertex2f((float)(this.field_146415_a.field_146209_f + this.field_146415_a.func_146200_o() - 2), (float)(this.field_146415_a.field_146210_g + this.field_146415_a.field_146219_i - 2));
        GL11.glVertex2f((float)(this.field_146415_a.field_146209_f - 2), (float)(this.field_146415_a.field_146210_g + this.field_146415_a.field_146219_i - 2));
        GL11.glVertex2f((float)(this.field_146415_a.field_146209_f - 2), (float)(this.field_146415_a.field_146210_g + this.field_146415_a.field_146219_i - 2));
        GL11.glVertex2f((float)(this.field_146415_a.field_146209_f - 2), (float)(this.field_146415_a.field_146210_g - 2));
        GL11.glEnd();
        if (a) {
            GL11.glEnable(3042);
        }
        if (b) {
            GL11.glEnable(3553);
        }
    }
}
