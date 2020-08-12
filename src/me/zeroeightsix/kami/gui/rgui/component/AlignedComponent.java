// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.rgui.component;

public class AlignedComponent extends AbstractComponent
{
    Alignment alignment;
    
    public Alignment getAlignment() {
        return this.alignment;
    }
    
    public void setAlignment(final Alignment alignment) {
        this.alignment = alignment;
    }
    
    public enum Alignment
    {
        LEFT(0), 
        CENTER(1), 
        RIGHT(2);
        
        int index;
        
        private Alignment(final int index) {
            this.index = index;
        }
        
        public int getIndex() {
            return this.index;
        }
    }
}
