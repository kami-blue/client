// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.event.events;

import net.minecraft.client.gui.GuiScreen;

public class GuiScreenEvent
{
    private GuiScreen screen;
    
    public GuiScreenEvent(final GuiScreen screen) {
        this.screen = screen;
    }
    
    public GuiScreen getScreen() {
        return this.screen;
    }
    
    public void setScreen(final GuiScreen screen) {
        this.screen = screen;
    }
    
    public static class Displayed extends GuiScreenEvent
    {
        public Displayed(final GuiScreen screen) {
            super(screen);
        }
    }
    
    public static class Closed extends GuiScreenEvent
    {
        public Closed(final GuiScreen screen) {
            super(screen);
        }
    }
}
