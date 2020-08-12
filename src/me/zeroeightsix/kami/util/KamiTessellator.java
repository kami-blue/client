// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.util;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.Tessellator;

public class KamiTessellator extends Tessellator
{
    public static KamiTessellator INSTANCE;
    
    public KamiTessellator() {
        super(2097152);
    }
    
    public static void prepare(final int mode) {
        prepareGL();
        begin(mode);
    }
    
    public static void prepareGL() {
        GL11.glBlendFunc(770, 771);
        GlStateManager.func_187428_a(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.func_187441_d(1.5f);
        GlStateManager.func_179090_x();
        GlStateManager.func_179132_a(false);
        GlStateManager.func_179147_l();
        GlStateManager.func_179097_i();
        GlStateManager.func_179140_f();
        GlStateManager.func_179129_p();
        GlStateManager.func_179141_d();
        GlStateManager.func_179124_c(1.0f, 1.0f, 1.0f);
    }
    
    public static void begin(final int mode) {
        KamiTessellator.INSTANCE.func_178180_c().func_181668_a(mode, DefaultVertexFormats.field_181706_f);
    }
    
    public static void release() {
        render();
        releaseGL();
    }
    
    public static void render() {
        KamiTessellator.INSTANCE.func_78381_a();
    }
    
    public static void releaseGL() {
        GlStateManager.func_179089_o();
        GlStateManager.func_179132_a(true);
        GlStateManager.func_179098_w();
        GlStateManager.func_179147_l();
        GlStateManager.func_179126_j();
    }
    
    public static void drawBox(final BlockPos blockPos, final int argb, final int sides) {
        final int a = argb >>> 24 & 0xFF;
        final int r = argb >>> 16 & 0xFF;
        final int g = argb >>> 8 & 0xFF;
        final int b = argb & 0xFF;
        drawBox(blockPos, r, g, b, a, sides);
    }
    
    public static void drawBox(final float x, final float y, final float z, final int argb, final int sides) {
        final int a = argb >>> 24 & 0xFF;
        final int r = argb >>> 16 & 0xFF;
        final int g = argb >>> 8 & 0xFF;
        final int b = argb & 0xFF;
        drawBox(KamiTessellator.INSTANCE.func_178180_c(), x, y, z, 1.0f, 1.0f, 1.0f, r, g, b, a, sides);
    }
    
    public static void drawBox(final BlockPos blockPos, final int r, final int g, final int b, final int a, final int sides) {
        drawBox(KamiTessellator.INSTANCE.func_178180_c(), (float)blockPos.field_177962_a, (float)blockPos.field_177960_b, (float)blockPos.field_177961_c, 1.0f, 1.0f, 1.0f, r, g, b, a, sides);
    }
    
    public static BufferBuilder getBufferBuilder() {
        return KamiTessellator.INSTANCE.func_178180_c();
    }
    
    public static void drawBox(final BufferBuilder buffer, final float x, final float y, final float z, final float w, final float h, final float d, final int r, final int g, final int b, final int a, final int sides) {
        if ((sides & 0x1) != 0x0) {
            buffer.func_181662_b((double)(x + w), (double)y, (double)z).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)(x + w), (double)y, (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)x, (double)y, (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)x, (double)y, (double)z).func_181669_b(r, g, b, a).func_181675_d();
        }
        if ((sides & 0x2) != 0x0) {
            buffer.func_181662_b((double)(x + w), (double)(y + h), (double)z).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)x, (double)(y + h), (double)z).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)x, (double)(y + h), (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)(x + w), (double)(y + h), (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
        }
        if ((sides & 0x4) != 0x0) {
            buffer.func_181662_b((double)(x + w), (double)y, (double)z).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)x, (double)y, (double)z).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)x, (double)(y + h), (double)z).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)(x + w), (double)(y + h), (double)z).func_181669_b(r, g, b, a).func_181675_d();
        }
        if ((sides & 0x8) != 0x0) {
            buffer.func_181662_b((double)x, (double)y, (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)(x + w), (double)y, (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)(x + w), (double)(y + h), (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)x, (double)(y + h), (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
        }
        if ((sides & 0x10) != 0x0) {
            buffer.func_181662_b((double)x, (double)y, (double)z).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)x, (double)y, (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)x, (double)(y + h), (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)x, (double)(y + h), (double)z).func_181669_b(r, g, b, a).func_181675_d();
        }
        if ((sides & 0x20) != 0x0) {
            buffer.func_181662_b((double)(x + w), (double)y, (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)(x + w), (double)y, (double)z).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)(x + w), (double)(y + h), (double)z).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)(x + w), (double)(y + h), (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
        }
    }
    
    public static void drawFace(final BlockPos blockPos, final int argb, final int sides) {
        final int a = argb >>> 24 & 0xFF;
        final int r = argb >>> 16 & 0xFF;
        final int g = argb >>> 8 & 0xFF;
        final int b = argb & 0xFF;
        drawFace(blockPos, r, g, b, a, sides);
    }
    
    public static void drawFace(final BlockPos blockPos, final int r, final int g, final int b, final int a, final int sides) {
        drawFace(KamiTessellator.INSTANCE.func_178180_c(), (float)blockPos.field_177962_a, (float)blockPos.field_177960_b, (float)blockPos.field_177961_c, 1.0f, 1.0f, 1.0f, r, g, b, a, sides);
    }
    
    public static void drawFace(final BufferBuilder buffer, final float x, final float y, final float z, final float w, final float h, final float d, final int r, final int g, final int b, final int a, final int sides) {
        if ((sides & 0x1) != 0x0) {
            buffer.func_181662_b((double)(x + w), (double)y, (double)z).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)(x + w), (double)y, (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)x, (double)y, (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)x, (double)y, (double)z).func_181669_b(r, g, b, a).func_181675_d();
        }
    }
    
    public static void drawFaceOutline(final BlockPos blockPos, final int r, final int g, final int b, final int a, final int sides) {
        drawFaceOutline(KamiTessellator.INSTANCE.func_178180_c(), (float)blockPos.field_177962_a, (float)blockPos.field_177960_b, (float)blockPos.field_177961_c, 1.0f, 1.0f, 1.0f, r, g, b, a, sides);
    }
    
    public static void drawFaceOutline(final BufferBuilder buffer, final float x, final float y, final float z, final float w, final float h, final float d, final int r, final int g, final int b, final int a, final int sides) {
        if ((sides & 0x1) != 0x0) {
            buffer.func_181662_b((double)(x + w), (double)y, (double)z).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)(x + w), (double)y, z + 0.02).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)x, (double)y, z + 0.02).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)x, (double)y, (double)z).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b(x + 0.02, (double)y, (double)z).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b(x + 0.02, (double)y, (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)x, (double)y, (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)x, (double)y, (double)z).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)(x + w), (double)y, z + d - 0.02).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)(x + w), (double)y, (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)x, (double)y, (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)x, (double)y, z + d - 0.02).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)(x + w), (double)y, (double)z).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)(x + w), (double)y, (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b(x + w - 0.02, (double)y, (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b(x + w - 0.02, (double)y, (double)z).func_181669_b(r, g, b, a).func_181675_d();
        }
    }
    
    public static void drawBoxOutline(final BlockPos blockPos, final int argb, final int sides) {
        final int a = argb >>> 24 & 0xFF;
        final int r = argb >>> 16 & 0xFF;
        final int g = argb >>> 8 & 0xFF;
        final int b = argb & 0xFF;
        drawBoxOutline(blockPos, r, g, b, a, sides);
    }
    
    public static void drawBoxOutline(final float x, final float y, final float z, final int argb, final int sides) {
        final int a = argb >>> 24 & 0xFF;
        final int r = argb >>> 16 & 0xFF;
        final int g = argb >>> 8 & 0xFF;
        final int b = argb & 0xFF;
        drawBoxOutline(KamiTessellator.INSTANCE.func_178180_c(), x, y, z, 1.0f, 1.0f, 1.0f, r, g, b, a, sides);
    }
    
    public static void drawBoxOutline(final BlockPos blockPos, final int r, final int g, final int b, final int a, final int sides) {
        drawBoxOutline(KamiTessellator.INSTANCE.func_178180_c(), (float)blockPos.field_177962_a, (float)blockPos.field_177960_b, (float)blockPos.field_177961_c, 1.0f, 1.0f, 1.0f, r, g, b, a, sides);
    }
    
    public static void drawBoxOutline(final BufferBuilder buffer, final float x, final float y, final float z, final float w, final float h, final float d, final int r, final int g, final int b, final int a, final int sides) {
        if ((sides & 0x1) != 0x0) {
            buffer.func_181662_b((double)(x + w), (double)y, (double)z).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)(x + w), (double)y, z + 0.02).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)x, (double)y, z + 0.02).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)x, (double)y, (double)z).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b(x + 0.02, (double)y, (double)z).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b(x + 0.02, (double)y, (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)x, (double)y, (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)x, (double)y, (double)z).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)(x + w), (double)y, z + d - 0.02).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)(x + w), (double)y, (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)x, (double)y, (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)x, (double)y, z + d - 0.02).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)(x + w), (double)y, (double)z).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)(x + w), (double)y, (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b(x + w - 0.02, (double)y, (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b(x + w - 0.02, (double)y, (double)z).func_181669_b(r, g, b, a).func_181675_d();
        }
    }
    
    public static void drawLines(final BlockPos blockPos, final int argb, final int sides) {
        final int a = argb >>> 24 & 0xFF;
        final int r = argb >>> 16 & 0xFF;
        final int g = argb >>> 8 & 0xFF;
        final int b = argb & 0xFF;
        drawLines(blockPos, r, g, b, a, sides);
    }
    
    public static void drawLines(final BlockPos blockPos, final int r, final int g, final int b, final int a, final int sides) {
        drawLines(KamiTessellator.INSTANCE.func_178180_c(), (float)blockPos.field_177962_a, (float)blockPos.field_177960_b, (float)blockPos.field_177961_c, 1.0f, 1.0f, 1.0f, r, g, b, a, sides);
    }
    
    public static void drawLines(final BufferBuilder buffer, final float x, final float y, final float z, final float w, final float h, final float d, final int r, final int g, final int b, final int a, final int sides) {
        if ((sides & 0x11) != 0x0) {
            buffer.func_181662_b((double)x, (double)y, (double)z).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)x, (double)y, (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
        }
        if ((sides & 0x12) != 0x0) {
            buffer.func_181662_b((double)x, (double)(y + h), (double)z).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)x, (double)(y + h), (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
        }
        if ((sides & 0x21) != 0x0) {
            buffer.func_181662_b((double)(x + w), (double)y, (double)z).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)(x + w), (double)y, (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
        }
        if ((sides & 0x22) != 0x0) {
            buffer.func_181662_b((double)(x + w), (double)(y + h), (double)z).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)(x + w), (double)(y + h), (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
        }
        if ((sides & 0x5) != 0x0) {
            buffer.func_181662_b((double)x, (double)y, (double)z).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)(x + w), (double)y, (double)z).func_181669_b(r, g, b, a).func_181675_d();
        }
        if ((sides & 0x6) != 0x0) {
            buffer.func_181662_b((double)x, (double)(y + h), (double)z).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)(x + w), (double)(y + h), (double)z).func_181669_b(r, g, b, a).func_181675_d();
        }
        if ((sides & 0x9) != 0x0) {
            buffer.func_181662_b((double)x, (double)y, (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)(x + w), (double)y, (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
        }
        if ((sides & 0xA) != 0x0) {
            buffer.func_181662_b((double)x, (double)(y + h), (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)(x + w), (double)(y + h), (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
        }
        if ((sides & 0x14) != 0x0) {
            buffer.func_181662_b((double)x, (double)y, (double)z).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)x, (double)(y + h), (double)z).func_181669_b(r, g, b, a).func_181675_d();
        }
        if ((sides & 0x24) != 0x0) {
            buffer.func_181662_b((double)(x + w), (double)y, (double)z).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)(x + w), (double)(y + h), (double)z).func_181669_b(r, g, b, a).func_181675_d();
        }
        if ((sides & 0x18) != 0x0) {
            buffer.func_181662_b((double)x, (double)y, (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)x, (double)(y + h), (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
        }
        if ((sides & 0x28) != 0x0) {
            buffer.func_181662_b((double)(x + w), (double)y, (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
            buffer.func_181662_b((double)(x + w), (double)(y + h), (double)(z + d)).func_181669_b(r, g, b, a).func_181675_d();
        }
    }
    
    public static void drawBox2(final AxisAlignedBB bb, final int r, final int g, final int b, final int a, final int sides) {
        final Tessellator tessellator = Tessellator.func_178181_a();
        final BufferBuilder bufferBuilder = tessellator.func_178180_c();
        bufferBuilder.func_181668_a(7, DefaultVertexFormats.field_181706_f);
        if ((sides & 0x1) != 0x0) {
            bufferBuilder.func_181662_b(bb.field_72336_d, bb.field_72338_b, bb.field_72339_c).func_181669_b(r, g, b, a).func_181675_d();
            bufferBuilder.func_181662_b(bb.field_72336_d, bb.field_72338_b, bb.field_72334_f).func_181669_b(r, g, b, a).func_181675_d();
            bufferBuilder.func_181662_b(bb.field_72340_a, bb.field_72338_b, bb.field_72334_f).func_181669_b(r, g, b, a).func_181675_d();
            bufferBuilder.func_181662_b(bb.field_72340_a, bb.field_72338_b, bb.field_72339_c).func_181669_b(r, g, b, a).func_181675_d();
        }
        if ((sides & 0x2) != 0x0) {
            bufferBuilder.func_181662_b(bb.field_72336_d, bb.field_72337_e, bb.field_72339_c).func_181669_b(r, g, b, a).func_181675_d();
            bufferBuilder.func_181662_b(bb.field_72340_a, bb.field_72337_e, bb.field_72339_c).func_181669_b(r, g, b, a).func_181675_d();
            bufferBuilder.func_181662_b(bb.field_72340_a, bb.field_72337_e, bb.field_72334_f).func_181669_b(r, g, b, a).func_181675_d();
            bufferBuilder.func_181662_b(bb.field_72336_d, bb.field_72337_e, bb.field_72334_f).func_181669_b(r, g, b, a).func_181675_d();
        }
        if ((sides & 0x4) != 0x0) {
            bufferBuilder.func_181662_b(bb.field_72336_d, bb.field_72338_b, bb.field_72339_c).func_181669_b(r, g, b, a).func_181675_d();
            bufferBuilder.func_181662_b(bb.field_72340_a, bb.field_72338_b, bb.field_72339_c).func_181669_b(r, g, b, a).func_181675_d();
            bufferBuilder.func_181662_b(bb.field_72340_a, bb.field_72337_e, bb.field_72339_c).func_181669_b(r, g, b, a).func_181675_d();
            bufferBuilder.func_181662_b(bb.field_72336_d, bb.field_72337_e, bb.field_72339_c).func_181669_b(r, g, b, a).func_181675_d();
        }
        if ((sides & 0x8) != 0x0) {
            bufferBuilder.func_181662_b(bb.field_72340_a, bb.field_72338_b, bb.field_72334_f).func_181669_b(r, g, b, a).func_181675_d();
            bufferBuilder.func_181662_b(bb.field_72336_d, bb.field_72338_b, bb.field_72334_f).func_181669_b(r, g, b, a).func_181675_d();
            bufferBuilder.func_181662_b(bb.field_72336_d, bb.field_72337_e, bb.field_72334_f).func_181669_b(r, g, b, a).func_181675_d();
            bufferBuilder.func_181662_b(bb.field_72340_a, bb.field_72337_e, bb.field_72334_f).func_181669_b(r, g, b, a).func_181675_d();
        }
        if ((sides & 0x10) != 0x0) {
            bufferBuilder.func_181662_b(bb.field_72340_a, bb.field_72338_b, bb.field_72339_c).func_181669_b(r, g, b, a).func_181675_d();
            bufferBuilder.func_181662_b(bb.field_72340_a, bb.field_72338_b, bb.field_72334_f).func_181669_b(r, g, b, a).func_181675_d();
            bufferBuilder.func_181662_b(bb.field_72340_a, bb.field_72337_e, bb.field_72334_f).func_181669_b(r, g, b, a).func_181675_d();
            bufferBuilder.func_181662_b(bb.field_72340_a, bb.field_72337_e, bb.field_72339_c).func_181669_b(r, g, b, a).func_181675_d();
        }
        if ((sides & 0x20) != 0x0) {
            bufferBuilder.func_181662_b(bb.field_72336_d, bb.field_72338_b, bb.field_72334_f).func_181669_b(r, g, b, a).func_181675_d();
            bufferBuilder.func_181662_b(bb.field_72336_d, bb.field_72338_b, bb.field_72339_c).func_181669_b(r, g, b, a).func_181675_d();
            bufferBuilder.func_181662_b(bb.field_72336_d, bb.field_72337_e, bb.field_72339_c).func_181669_b(r, g, b, a).func_181675_d();
            bufferBuilder.func_181662_b(bb.field_72336_d, bb.field_72337_e, bb.field_72334_f).func_181669_b(r, g, b, a).func_181675_d();
        }
        tessellator.func_78381_a();
    }
    
    public static void drawFullBox2(final AxisAlignedBB bb, final BlockPos blockPos, final float width, final int argb) {
        final int a = argb >>> 24 & 0xFF;
        final int r = argb >>> 16 & 0xFF;
        final int g = argb >>> 8 & 0xFF;
        final int b = argb & 0xFF;
        drawFullBox2(bb, blockPos, width, r, g, b, a);
    }
    
    public static void drawFullBox2(final AxisAlignedBB bb, final BlockPos blockPos, final float width, final int red, final int green, final int blue, final int alpha) {
        prepare(7);
        drawBox2(bb, red, green, blue, alpha, 63);
        release();
        drawBoundingBox(bb, width, red, green, blue, 255);
    }
    
    public static void drawFullBox(final AxisAlignedBB bb, final BlockPos blockPos, final float width, final int argb) {
        final int a = argb >>> 24 & 0xFF;
        final int r = argb >>> 16 & 0xFF;
        final int g = argb >>> 8 & 0xFF;
        final int b = argb & 0xFF;
        drawFullBox(bb, blockPos, width, r, g, b, a);
    }
    
    public static void drawFullBox(final AxisAlignedBB bb, final BlockPos blockPos, final float width, final int red, final int green, final int blue, final int alpha) {
        prepare(7);
        drawBox(blockPos, red, green, blue, alpha, 63);
        release();
        drawBoundingBox(bb, width, red, green, blue, 255);
    }
    
    public static void drawBoundingBox(final AxisAlignedBB bb, final float width, final int argb) {
        final int a = argb >>> 24 & 0xFF;
        final int r = argb >>> 16 & 0xFF;
        final int g = argb >>> 8 & 0xFF;
        final int b = argb & 0xFF;
        drawBoundingBox(bb, width, r, g, b, a);
    }
    
    public static void drawBoundingBox(final AxisAlignedBB bb, final float width, final int red, final int green, final int blue, final int alpha) {
        GlStateManager.func_179094_E();
        GlStateManager.func_179147_l();
        GlStateManager.func_179097_i();
        GlStateManager.func_179120_a(770, 771, 0, 1);
        GlStateManager.func_179090_x();
        GlStateManager.func_179132_a(false);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glLineWidth(width);
        final Tessellator tessellator = Tessellator.func_178181_a();
        final BufferBuilder bufferbuilder = tessellator.func_178180_c();
        bufferbuilder.func_181668_a(3, DefaultVertexFormats.field_181706_f);
        bufferbuilder.func_181662_b(bb.field_72340_a, bb.field_72338_b, bb.field_72339_c).func_181669_b(red, green, blue, alpha).func_181675_d();
        bufferbuilder.func_181662_b(bb.field_72340_a, bb.field_72338_b, bb.field_72334_f).func_181669_b(red, green, blue, alpha).func_181675_d();
        bufferbuilder.func_181662_b(bb.field_72336_d, bb.field_72338_b, bb.field_72334_f).func_181669_b(red, green, blue, alpha).func_181675_d();
        bufferbuilder.func_181662_b(bb.field_72336_d, bb.field_72338_b, bb.field_72339_c).func_181669_b(red, green, blue, alpha).func_181675_d();
        bufferbuilder.func_181662_b(bb.field_72340_a, bb.field_72338_b, bb.field_72339_c).func_181669_b(red, green, blue, alpha).func_181675_d();
        bufferbuilder.func_181662_b(bb.field_72340_a, bb.field_72337_e, bb.field_72339_c).func_181669_b(red, green, blue, alpha).func_181675_d();
        bufferbuilder.func_181662_b(bb.field_72340_a, bb.field_72337_e, bb.field_72334_f).func_181669_b(red, green, blue, alpha).func_181675_d();
        bufferbuilder.func_181662_b(bb.field_72340_a, bb.field_72338_b, bb.field_72334_f).func_181669_b(red, green, blue, alpha).func_181675_d();
        bufferbuilder.func_181662_b(bb.field_72336_d, bb.field_72338_b, bb.field_72334_f).func_181669_b(red, green, blue, alpha).func_181675_d();
        bufferbuilder.func_181662_b(bb.field_72336_d, bb.field_72337_e, bb.field_72334_f).func_181669_b(red, green, blue, alpha).func_181675_d();
        bufferbuilder.func_181662_b(bb.field_72340_a, bb.field_72337_e, bb.field_72334_f).func_181669_b(red, green, blue, alpha).func_181675_d();
        bufferbuilder.func_181662_b(bb.field_72336_d, bb.field_72337_e, bb.field_72334_f).func_181669_b(red, green, blue, alpha).func_181675_d();
        bufferbuilder.func_181662_b(bb.field_72336_d, bb.field_72337_e, bb.field_72339_c).func_181669_b(red, green, blue, alpha).func_181675_d();
        bufferbuilder.func_181662_b(bb.field_72336_d, bb.field_72338_b, bb.field_72339_c).func_181669_b(red, green, blue, alpha).func_181675_d();
        bufferbuilder.func_181662_b(bb.field_72336_d, bb.field_72337_e, bb.field_72339_c).func_181669_b(red, green, blue, alpha).func_181675_d();
        bufferbuilder.func_181662_b(bb.field_72340_a, bb.field_72337_e, bb.field_72339_c).func_181669_b(red, green, blue, alpha).func_181675_d();
        tessellator.func_78381_a();
        GL11.glDisable(2848);
        GlStateManager.func_179132_a(true);
        GlStateManager.func_179126_j();
        GlStateManager.func_179098_w();
        GlStateManager.func_179084_k();
        GlStateManager.func_179121_F();
    }
    
    public static void drawBoundingBoxFace(final AxisAlignedBB bb, final float width, final int red, final int green, final int blue, final int alpha) {
        GlStateManager.func_179094_E();
        GlStateManager.func_179147_l();
        GlStateManager.func_179097_i();
        GlStateManager.func_179120_a(770, 771, 0, 1);
        GlStateManager.func_179090_x();
        GlStateManager.func_179132_a(false);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glLineWidth(width);
        final Tessellator tessellator = Tessellator.func_178181_a();
        final BufferBuilder bufferbuilder = tessellator.func_178180_c();
        bufferbuilder.func_181668_a(3, DefaultVertexFormats.field_181706_f);
        bufferbuilder.func_181662_b(bb.field_72340_a, bb.field_72338_b, bb.field_72339_c).func_181669_b(red, green, blue, alpha).func_181675_d();
        bufferbuilder.func_181662_b(bb.field_72340_a, bb.field_72338_b, bb.field_72334_f).func_181669_b(red, green, blue, alpha).func_181675_d();
        bufferbuilder.func_181662_b(bb.field_72336_d, bb.field_72338_b, bb.field_72334_f).func_181669_b(red, green, blue, alpha).func_181675_d();
        bufferbuilder.func_181662_b(bb.field_72336_d, bb.field_72338_b, bb.field_72339_c).func_181669_b(red, green, blue, alpha).func_181675_d();
        bufferbuilder.func_181662_b(bb.field_72340_a, bb.field_72338_b, bb.field_72339_c).func_181669_b(red, green, blue, alpha).func_181675_d();
        tessellator.func_78381_a();
        GL11.glDisable(2848);
        GlStateManager.func_179132_a(true);
        GlStateManager.func_179126_j();
        GlStateManager.func_179098_w();
        GlStateManager.func_179084_k();
        GlStateManager.func_179121_F();
    }
    
    static {
        KamiTessellator.INSTANCE = new KamiTessellator();
    }
}
