// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.event.events;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.renderer.Tessellator;
import me.zeroeightsix.kami.event.KamiEvent;

public class RenderEvent extends KamiEvent
{
    private final Tessellator tessellator;
    private final Vec3d renderPos;
    
    public RenderEvent(final Tessellator tessellator, final Vec3d renderPos) {
        this.tessellator = tessellator;
        this.renderPos = renderPos;
    }
    
    public Tessellator getTessellator() {
        return this.tessellator;
    }
    
    public BufferBuilder getBuffer() {
        return this.tessellator.func_178180_c();
    }
    
    public Vec3d getRenderPos() {
        return this.renderPos;
    }
    
    public void setTranslation(final Vec3d translation) {
        this.getBuffer().func_178969_c(-translation.field_72450_a, -translation.field_72448_b, -translation.field_72449_c);
    }
    
    public void resetTranslation() {
        this.setTranslation(this.renderPos);
    }
}
