// 
// Decompiled by Procyon v0.5.36
// 

package the_fireplace.ias.gui;

import net.minecraft.client.gui.GuiScreen;
import joptsimple.internal.Strings;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

public class GuiPasswordField extends GuiTextField
{
    public GuiPasswordField(final int componentId, final FontRenderer fontrendererObj, final int x, final int y, final int par5Width, final int par6Height) {
        super(componentId, fontrendererObj, x, y, par5Width, par6Height);
    }
    
    public void func_146194_f() {
        final String password = this.func_146179_b();
        this.replaceText(Strings.repeat('*', this.func_146179_b().length()));
        super.func_146194_f();
        this.replaceText(password);
    }
    
    public boolean func_146201_a(final char typedChar, final int keyCode) {
        return !GuiScreen.func_175280_f(keyCode) && !GuiScreen.func_175277_d(keyCode) && super.func_146201_a(typedChar, keyCode);
    }
    
    public boolean func_146192_a(final int mouseX, final int mouseY, final int mouseButton) {
        final String password = this.func_146179_b();
        this.replaceText(Strings.repeat('*', this.func_146179_b().length()));
        super.func_146192_a(mouseX, mouseY, mouseButton);
        this.replaceText(password);
        return true;
    }
    
    private void replaceText(final String newText) {
        final int cursorPosition = this.func_146198_h();
        final int selectionEnd = this.func_146186_n();
        this.func_146180_a(newText);
        this.func_146190_e(cursorPosition);
        this.func_146199_i(selectionEnd);
    }
}
