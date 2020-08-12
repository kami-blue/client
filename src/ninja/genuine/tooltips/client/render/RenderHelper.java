// 
// Decompiled by Procyon v0.5.36
// 

package ninja.genuine.tooltips.client.render;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.Minecraft;
import ninja.genuine.tooltips.client.Tooltip;

public class RenderHelper
{
    public static void renderTooltip(final Tooltip tooltip, final double partialTicks) {
        final RenderManager rm = Minecraft.func_71410_x().func_175598_ae();
        final EntityItem e = tooltip.getEntity();
        final double interpX = rm.field_78730_l - (e.field_70165_t - (e.field_70169_q - e.field_70165_t) * partialTicks);
        final double interpY = rm.field_78731_m - 0.65 - (e.field_70163_u - (e.field_70167_r - e.field_70163_u) * partialTicks);
        final double interpZ = rm.field_78728_n - (e.field_70161_v - (e.field_70166_s - e.field_70161_v) * partialTicks);
        GlStateManager.func_179094_E();
        GlStateManager.func_179123_a();
        GlStateManager.func_179091_B();
        GlStateManager.func_179141_d();
        GlStateManager.func_179092_a(516, 0.1f);
        GlStateManager.func_179147_l();
        GlStateManager.func_179097_i();
        GlStateManager.func_187401_a(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.func_179131_c(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.func_179137_b(-interpX, -interpY, -interpZ);
        GlStateManager.func_179114_b(rm.field_78735_i + 180.0f, 0.0f, -1.0f, 0.0f);
        GlStateManager.func_179114_b(rm.field_78732_j, -1.0f, 0.0f, 0.0f);
        GlStateManager.func_179139_a(tooltip.scale, -tooltip.scale, tooltip.scale);
        renderTooltipTile(tooltip);
        renderTooltipText(tooltip);
        GlStateManager.func_179139_a(1.0 / tooltip.scale, 1.0 / -tooltip.scale, 1.0 / tooltip.scale);
        GlStateManager.func_179114_b(rm.field_78732_j, 1.0f, 0.0f, 0.0f);
        GlStateManager.func_179114_b(rm.field_78735_i - 180.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.func_179137_b(interpX, interpY, interpZ);
        GlStateManager.func_179126_j();
        GlStateManager.func_179118_c();
        GlStateManager.func_179101_C();
        GlStateManager.func_179140_f();
        GlStateManager.func_179099_b();
        GlStateManager.func_179121_F();
    }
    
    private static void renderTooltipTile(final Tooltip tooltip) {
        final int x = -tooltip.getWidth() / 2;
        final int y = -tooltip.getHeight() / 2;
        final int w = tooltip.getWidth();
        final int h = tooltip.getHeight();
        final int c1 = tooltip.colorBackground;
        final int c2 = tooltip.colorOutline;
        final int c3 = tooltip.colorOutlineShade;
        renderStyle1(x, y, w, h, c1, c2, c3);
    }
    
    private static void renderStyle1(final int x, final int y, final int w, final int h, final int c1, final int c2, final int c3) {
        drawRect(x - 3 + 0, y - 4 + 0, 0.0, w + 6, 1.0, c1);
        drawRect(x + w + 3, y - 3 + 0, 0.0, 1.0, h + 6, c1);
        drawRect(x - 3 + 0, y + h + 3, 0.0, w + 6, 1.0, c1);
        drawRect(x - 4 + 0, y - 3 + 0, 0.0, 1.0, h + 6, c1);
        drawRect(x - 2 + 0, y - 2 + 0, 0.0, w + 4, h + 4, c1);
        drawRect(x - 3 + 0, y - 3 + 0, 0.0, w + 6, 1.0, c2);
        drawGradientRect(x + w + 2, y - 2 + 0, 0.0, 1.0, h + 4, c2, c3);
        drawRect(x - 3 + 0, y + h + 2, 0.0, w + 6, 1.0, c3);
        drawGradientRect(x - 3 + 0, y - 2 + 0, 0.0, 1.0, h + 4, c2, c3);
    }
    
    private static void renderStyle2(final int x, final int y, final int w, final int h, final int c1, final int c2, final int c3) {
        drawRect(x - 2 + 0, y - 2 + 0, 0.0, w + 4, h + 4, c1);
        drawRect(x - 2 + 0, y - 3 + 0, 0.0, w + 4, 1.0, c2);
        drawGradientRect(x + w + 2, y - 2 + 0, 0.0, 1.0, h + 4, c2, c3);
        drawRect(x - 2 + 0, y + h + 2, 0.0, w + 4, 1.0, c3);
        drawGradientRect(x - 3 + 0, y - 2 + 0, 0.0, 1.0, h + 4, c2, c3);
    }
    
    private static void renderStyle3(final int x, final int y, final int w, final int h, final int c1, final int c2, final int c3) {
        drawRect(x - 2 + 0, y - 2 + 0, 0.0, w + 4, h + 4, c1);
        drawRect(x - 3 + 0, y - 3 + 0, 0.0, w + 6, 1.0, c2);
        drawGradientRect(x + w + 2, y - 2 + 0, 0.0, 1.0, h + 4, c2, c3);
        drawRect(x - 3 + 0, y + h + 2, 0.0, w + 6, 1.0, c3);
        drawGradientRect(x - 3 + 0, y - 2 + 0, 0.0, 1.0, h + 4, c2, c3);
    }
    
    private static void renderStyle4(final int x, final int y, final int w, final int h, final int c1, final int c2, final int c3) {
        drawRect(x - 2 + 0, y - 2 + 0, 0.0, w + 4, h + 4, c1);
    }
    
    private static void renderTooltipText(final Tooltip tooltip) {
        if ((tooltip.alpha & 0xFC000000) == 0x0) {
            return;
        }
        final int x = -tooltip.getWidth() / 2;
        int y = -tooltip.getHeight() / 2;
        GlStateManager.func_179094_E();
        GlStateManager.func_179147_l();
        GlStateManager.func_187428_a(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        for (int i = 0; i < tooltip.getText().size(); ++i) {
            String s = tooltip.getText().get(i);
            if (i == 0) {
                s = tooltip.formattingColor() + s;
            }
            Minecraft.func_71410_x().field_71466_p.func_175065_a(s, (float)x, (float)y, 0xFFFFFF | tooltip.alpha, true);
            if (i == 0) {
                y += 2;
            }
            y += 10;
        }
        GlStateManager.func_179084_k();
        GlStateManager.func_179121_F();
    }
    
    public static void drawHuePicker(final double x, final double y, final double z, final double width, double height) {
        height /= 5.0;
        drawGradientRect(x - 1.0, y + 0.0 * height, z, width, height, -65536, -256);
        drawGradientRect(x - 1.0, y + 1.0 * height, z, width, height, -256, -16711936);
        drawGradientRect(x - 1.0, y + 2.0 * height, z, width, height, -16711936, -16711681);
        drawGradientRect(x - 1.0, y + 3.0 * height, z, width, height, -16711681, -16776961);
        drawGradientRect(x - 1.0, y + 4.0 * height, z, width, height, -16776961, -65536);
    }
    
    public static void drawColorPicker(final double x, final double y, final double z, final double w, final double h, final int hue) {
        final int r = hue >> 16 & 0xFF;
        final int g = hue >> 8 & 0xFF;
        final int b = hue >> 0 & 0xFF;
        GlStateManager.func_179090_x();
        GlStateManager.func_179147_l();
        GlStateManager.func_179118_c();
        GlStateManager.func_187428_a(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.func_179103_j(7425);
        GlStateManager.func_179131_c(1.0f, 1.0f, 1.0f, 1.0f);
        final Tessellator tess = Tessellator.func_178181_a();
        final BufferBuilder bb = tess.func_178180_c();
        bb.func_181668_a(7, DefaultVertexFormats.field_181706_f);
        bb.func_181662_b(x + 0.0, y + 0.0, z).func_181669_b(r, g, b, 255).func_181675_d();
        bb.func_181662_b(x + 0.0, y + h, z).func_181669_b(r, g, b, 255).func_181675_d();
        bb.func_181662_b(x + w, y + h, z).func_181669_b(r, g, b, 255).func_181675_d();
        bb.func_181662_b(x + w, y + 0.0, z).func_181669_b(r, g, b, 255).func_181675_d();
        tess.func_78381_a();
        bb.func_181668_a(7, DefaultVertexFormats.field_181706_f);
        bb.func_181662_b(x + 0.0, y + 0.0, z).func_181669_b(255, 255, 255, 255).func_181675_d();
        bb.func_181662_b(x + 0.0, y + h, z).func_181669_b(255, 255, 255, 255).func_181675_d();
        bb.func_181662_b(x + w, y + h, z).func_181669_b(255, 255, 255, 0).func_181675_d();
        bb.func_181662_b(x + w, y + 0.0, z).func_181669_b(255, 255, 255, 0).func_181675_d();
        tess.func_78381_a();
        bb.func_181668_a(7, DefaultVertexFormats.field_181706_f);
        bb.func_181662_b(x + 0.0, y + 0.0, z).func_181669_b(0, 0, 0, 0).func_181675_d();
        bb.func_181662_b(x + 0.0, y + h, z).func_181669_b(0, 0, 0, 255).func_181675_d();
        bb.func_181662_b(x + w, y + h, z).func_181669_b(0, 0, 0, 255).func_181675_d();
        bb.func_181662_b(x + w, y + 0.0, z).func_181669_b(0, 0, 0, 0).func_181675_d();
        tess.func_78381_a();
        GlStateManager.func_179103_j(7424);
        GlStateManager.func_179084_k();
        GlStateManager.func_179141_d();
        GlStateManager.func_179098_w();
    }
    
    public static void drawRect(final double x, final double y, final double z, final double w, final double h, final int c1) {
        drawGradientRect(x, y, z, w, h, c1, c1, c1, c1);
    }
    
    public static void drawGradientRect(final double x, final double y, final double z, final double w, final double h, final int c1) {
        final int alpha = c1 >> 24 & 0xFF;
        final int c2 = (c1 & 0xFEFEFE) >> 1 | alpha;
        drawGradientRect(x, y, z, w, h, c1, c2, c2, c1);
    }
    
    public static void drawGradientRect(final double x, final double y, final double z, final double w, final double h, final int c1, final int c2) {
        drawGradientRect(x, y, z, w, h, c1, c2, c2, c1);
    }
    
    public static void drawGradientRect(final double x, final double y, final double z, final double w, final double h, final int c1, final int c2, final int c3, final int c4) {
        final int a1 = c1 >> 24 & 0xFF;
        final int r1 = c1 >> 16 & 0xFF;
        final int g1 = c1 >> 8 & 0xFF;
        final int b1 = c1 >> 0 & 0xFF;
        final int a2 = c2 >> 24 & 0xFF;
        final int r2 = c2 >> 16 & 0xFF;
        final int g2 = c2 >> 8 & 0xFF;
        final int b2 = c2 >> 0 & 0xFF;
        final int a3 = c3 >> 24 & 0xFF;
        final int r3 = c3 >> 16 & 0xFF;
        final int g3 = c3 >> 8 & 0xFF;
        final int b3 = c3 >> 0 & 0xFF;
        final int a4 = c4 >> 24 & 0xFF;
        final int r4 = c4 >> 16 & 0xFF;
        final int g4 = c4 >> 8 & 0xFF;
        final int b4 = c4 >> 0 & 0xFF;
        GlStateManager.func_179090_x();
        GlStateManager.func_179147_l();
        GlStateManager.func_179118_c();
        GlStateManager.func_187428_a(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.func_179103_j(7425);
        GlStateManager.func_179131_c(1.0f, 1.0f, 1.0f, 1.0f);
        final Tessellator tess = Tessellator.func_178181_a();
        final BufferBuilder bb = tess.func_178180_c();
        bb.func_181668_a(7, DefaultVertexFormats.field_181706_f);
        bb.func_181662_b(x + 0.0, y + 0.0, z).func_181669_b(r1, g1, b1, a1).func_181675_d();
        bb.func_181662_b(x + 0.0, y + h, z).func_181669_b(r2, g2, b2, a2).func_181675_d();
        bb.func_181662_b(x + w, y + h, z).func_181669_b(r3, g3, b3, a3).func_181675_d();
        bb.func_181662_b(x + w, y + 0.0, z).func_181669_b(r4, g4, b4, a4).func_181675_d();
        tess.func_78381_a();
        GlStateManager.func_179103_j(7424);
        GlStateManager.func_179084_k();
        GlStateManager.func_179141_d();
        GlStateManager.func_179098_w();
    }
}
