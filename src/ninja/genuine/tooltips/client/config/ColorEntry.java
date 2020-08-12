// 
// Decompiled by Procyon v0.5.36
// 

package ninja.genuine.tooltips.client.config;

import net.minecraft.client.gui.GuiScreen;
import ninja.genuine.tooltips.client.gui.GuiColorPicker;
import net.minecraftforge.fml.client.config.HoverChecker;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import ninja.genuine.tooltips.client.gui.GuiColorButton;
import net.minecraftforge.fml.client.config.GuiConfigEntries;

public class ColorEntry extends GuiConfigEntries.StringEntry
{
    private GuiColorButton button;
    
    public ColorEntry(final GuiConfig parent, final GuiConfigEntries entries, final IConfigElement element) {
        super(parent, entries, element);
        this.button = new GuiColorButton(11, entries.controlX + 2, entries.field_148153_b - 1, element.get().toString(), element.getDefault().toString());
    }
    
    public void func_192634_a(final int slotIndex, final int x, final int y, final int listWidth, final int slotHeight, final int mouseX, final int mouseY, final boolean isSelected, final float partial) {
        final boolean isChanged = this.isChanged();
        if (this.drawLabel) {
            final String label = (this.isValidValue ? (isChanged ? TextFormatting.WHITE.toString() : TextFormatting.GRAY.toString()) : TextFormatting.RED.toString()) + (isChanged ? TextFormatting.ITALIC.toString() : "") + this.name;
            this.mc.field_71466_p.func_78276_b(label, this.owningScreen.entryList.labelX, y + slotHeight / 2 - this.mc.field_71466_p.field_78288_b / 2, 16777215);
        }
        this.btnUndoChanges.field_146128_h = this.owningEntryList.scrollBarX - 44;
        this.btnUndoChanges.field_146129_i = y;
        this.btnUndoChanges.field_146124_l = (this.enabled() && isChanged);
        this.btnUndoChanges.func_191745_a(this.mc, mouseX, mouseY, partial);
        this.btnDefault.field_146128_h = this.owningEntryList.scrollBarX - 22;
        this.btnDefault.field_146129_i = y;
        this.btnDefault.field_146124_l = (this.enabled() && !this.isDefault());
        this.btnDefault.func_191745_a(this.mc, mouseX, mouseY, partial);
        if (this.tooltipHoverChecker == null) {
            this.tooltipHoverChecker = new HoverChecker(y, y + slotHeight, x, this.owningScreen.entryList.controlX - 8, 800);
        }
        else {
            this.tooltipHoverChecker.updateBounds(y, y + slotHeight, x, this.owningScreen.entryList.controlX - 8);
        }
        this.textFieldValue.field_146209_f = this.owningEntryList.controlX + 2;
        this.textFieldValue.field_146210_g = y + 1;
        this.textFieldValue.field_146218_h = this.owningEntryList.controlWidth - 24;
        this.textFieldValue.func_146184_c(this.enabled());
        this.button.update(this.textFieldValue.func_146179_b());
        this.button.field_146128_h = this.owningEntryList.controlX + this.textFieldValue.field_146218_h + 4;
        this.button.field_146129_i = y - 1;
        this.textFieldValue.func_146194_f();
        this.button.func_191745_a(this.mc, mouseX, mouseY, partial);
    }
    
    public void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) {
        if (this.button.func_146116_c(this.mc, mouseX, mouseY)) {
            this.mc.func_147108_a((GuiScreen)new GuiColorPicker((GuiScreen)this.owningScreen, this.textFieldValue, this.beforeValue));
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    public void keyTyped(final char eventChar, final int eventKey) {
        super.keyTyped(eventChar, eventKey);
        Config.save();
    }
}
