// 
// Decompiled by Procyon v0.5.36
// 

package ninja.genuine.tooltips.client.config;

import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.config.ConfigCategory;
import java.util.Objects;
import net.minecraftforge.common.config.Configuration;

public class Config
{
    private static Config instance;
    public static final String category_general = "General";
    public static final String category_appearance = "Appearance";
    public static final String category_behavior = "Behavior";
    private Configuration base;
    
    public static Config getInstance() {
        if (Objects.isNull(Config.instance)) {
            Config.instance = new Config();
        }
        return Config.instance;
    }
    
    public static void setConfiguration(final Configuration config) {
        getInstance().base = config;
    }
    
    public static void save() {
        getInstance().base.save();
    }
    
    public static ConfigCategory getCategory(final String name) {
        return getInstance().base.getCategory(name);
    }
    
    public static void populate() {
        getInstance().internalPopulation();
    }
    
    private Config() {
    }
    
    public boolean isEnabled() {
        return this.base.getBoolean("Enable Mod", "General", true, "Enable rendering the tooltips.");
    }
    
    public int getRenderDistance() {
        return this.base.getInt("Maximum Drawing Distance", "Behavior", 12, 2, 64, "Sets the maximum distance that tooltips will be displayed.");
    }
    
    public int getMaxTooltips() {
        return this.base.getInt("Max Tooltips", "Behavior", 4, 0, 999, "Sets the maximum number of tooltips shown on screenat once.");
    }
    
    public int getShowTime() {
        return this.base.getInt("Ticks to Show", "Behavior", 40, 0, 1000, "Sets the number of ticks to show the tooltips before they fade.");
    }
    
    public int getFadeTime() {
        return this.base.getInt("Fade Duration", "Behavior", 10, 0, 1000, "Sets the duration in ticks for the fading process.");
    }
    
    public boolean isOverridingOutline() {
        return this.base.getBoolean("Override Outline Color", "Behavior", false, "Use the custom outline color instead.");
    }
    
    public boolean isHidingModName() {
        return this.base.getBoolean("Hide Mod Name", "Behavior", false, "Hide mod names on tooltips. Enable this if you see two mod names.");
    }
    
    public Property getOpacity() {
        return this.base.get("Appearance", "Tooltip Opacity", 0.75, "Sets the opacity for the tooltips; 0 being completely invisible and 1 being completely opaque.", 0.0, 1.0);
    }
    
    public Property getScale() {
        return this.base.get("Appearance", "Tooltip Scale", 1.0, "Sets the scale for the tooltips; 0.1 being one thenth the size and 4 being four times the size.", 0.1, 4.0);
    }
    
    public Property getOutline() {
        return this.base.get("Appearance", "Outline Color", "0x5000FF", "Choose a color using the gui by clicking the color button or type in a color manually.", Property.Type.COLOR);
    }
    
    public Property getBackground() {
        return this.base.get("Appearance", "Background Color", "0x100010", "Choose a color using the gui by clicking the color button or type in a color manually.", Property.Type.COLOR);
    }
    
    public int getBackgroundColor() {
        return this.decodeProperty(this.getBackground()) & 0xFFFFFF;
    }
    
    public int getOutlineColor() {
        return this.decodeProperty(this.getOutline()) & 0xFFFFFF;
    }
    
    private void internalPopulation() {
        this.isEnabled();
        this.getOpacity().setConfigEntryClass((Class)GuiConfigEntries.NumberSliderEntry.class);
        this.getScale().setConfigEntryClass((Class)GuiConfigEntries.NumberSliderEntry.class);
        this.getOutline().setConfigEntryClass((Class)ColorEntry.class);
        this.getBackground().setConfigEntryClass((Class)ColorEntry.class);
        this.getRenderDistance();
        this.getMaxTooltips();
        this.getShowTime();
        this.getFadeTime();
        this.isHidingModName();
        this.isOverridingOutline();
    }
    
    private int decodeProperty(final Property property) {
        try {
            return Integer.decode(property.getString());
        }
        catch (NumberFormatException e) {
            return Integer.decode(property.getDefault());
        }
    }
}
