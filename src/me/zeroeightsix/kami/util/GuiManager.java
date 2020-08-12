// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.util;

import com.mojang.realmsclient.gui.ChatFormatting;

public class GuiManager
{
    private float guiRed;
    private float guiGreen;
    private float guiBlue;
    private String textColor;
    private int moduleListRed;
    private int moduleListGreen;
    private int moduleListBlue;
    private ModuleListMode moduleListMode;
    private boolean textRadarPots;
    private int textRadarPlayers;
    
    public boolean isTextRadarPots() {
        return this.textRadarPots;
    }
    
    public void setTextRadarPots(final boolean textRadarPots) {
        this.textRadarPots = textRadarPots;
    }
    
    public GuiManager() {
        this.guiRed = 0.55f;
        this.guiGreen = 0.7f;
        this.guiBlue = 0.25f;
        this.textColor = ChatFormatting.GRAY.toString();
        this.moduleListMode = ModuleListMode.RAINBOW;
        this.moduleListRed = 255;
        this.moduleListGreen = 255;
        this.moduleListBlue = 255;
        this.textRadarPots = true;
        this.textRadarPlayers = 8;
    }
    
    public int getTextRadarPlayers() {
        return this.textRadarPlayers;
    }
    
    public void setTextRadarPlayers(final int textRadarPlayers) {
        this.textRadarPlayers = textRadarPlayers;
    }
    
    public ModuleListMode getModuleListMode() {
        return this.moduleListMode;
    }
    
    public void setModuleListMode(final ModuleListMode moduleListMode) {
        this.moduleListMode = moduleListMode;
    }
    
    public void setModuleListColors(final int r, final int g, final int b) {
        this.moduleListRed = r;
        this.moduleListGreen = g;
        this.moduleListBlue = b;
    }
    
    public void setGuiColors(final float r, final float g, final float b) {
        this.guiRed = r;
        this.guiGreen = g;
        this.guiBlue = b;
    }
    
    public String getTextColor() {
        return this.textColor;
    }
    
    public void setTextColor(final String textColor) {
        this.textColor = textColor;
    }
    
    public float getGuiRed() {
        return this.guiRed;
    }
    
    public float getGuiGreen() {
        return this.guiGreen;
    }
    
    public float getGuiBlue() {
        return this.guiBlue;
    }
    
    public int getModuleListRed() {
        return this.moduleListRed;
    }
    
    public int getModuleListGreen() {
        return this.moduleListGreen;
    }
    
    public int getModuleListBlue() {
        return this.moduleListBlue;
    }
    
    public enum ModuleListMode
    {
        STATIC, 
        RAINBOW;
    }
}
