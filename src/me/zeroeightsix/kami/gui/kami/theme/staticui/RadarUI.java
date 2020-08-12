// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.kami.theme.staticui;

import me.zeroeightsix.kami.gui.rgui.component.Component;
import java.util.Iterator;
import me.zeroeightsix.kami.util.EntityUtil;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.Entity;
import me.zeroeightsix.kami.util.Wrapper;
import me.zeroeightsix.kami.gui.kami.RenderHelper;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;
import me.zeroeightsix.kami.gui.kami.component.Radar;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;

public class RadarUI extends AbstractComponentUI<Radar>
{
    float scale;
    public static final int radius = 45;
    
    public RadarUI() {
        this.scale = 2.0f;
    }
    
    @Override
    public void handleSizeComponent(final Radar component) {
        component.setWidth(90);
        component.setHeight(90);
    }
    
    @Override
    public void renderComponent(final Radar component, final FontRenderer fontRenderer) {
        this.scale = 2.0f;
        GL11.glTranslated((double)(component.getWidth() / 2), (double)(component.getHeight() / 2), 0.0);
        GlStateManager.func_179090_x();
        GlStateManager.func_179140_f();
        GlStateManager.func_179147_l();
        GlStateManager.func_179129_p();
        GlStateManager.func_179094_E();
        GL11.glColor4f(0.11f, 0.11f, 0.11f, 0.6f);
        RenderHelper.drawCircle(0.0f, 0.0f, 45.0f);
        GL11.glRotatef(Wrapper.getPlayer().field_70177_z + 180.0f, 0.0f, 0.0f, -1.0f);
        for (final Entity e : Wrapper.getWorld().field_72996_f) {
            if (!(e instanceof EntityLiving)) {
                continue;
            }
            float red = 1.0f;
            float green = 1.0f;
            if (EntityUtil.isPassive(e)) {
                red = 0.0f;
            }
            else {
                green = 0.0f;
            }
            final double dX = e.field_70165_t - Wrapper.getPlayer().field_70165_t;
            final double dZ = e.field_70161_v - Wrapper.getPlayer().field_70161_v;
            final double distance = Math.sqrt(Math.pow(dX, 2.0) + Math.pow(dZ, 2.0));
            if (distance > 45.0f * this.scale) {
                continue;
            }
            if (Math.abs(Wrapper.getPlayer().field_70163_u - e.field_70163_u) > 30.0) {
                continue;
            }
            GL11.glColor4f(red, green, 0.0f, 0.5f);
            RenderHelper.drawCircle((int)dX / this.scale, (int)dZ / this.scale, 2.5f / this.scale);
        }
        GL11.glColor3f(1.0f, 1.0f, 1.0f);
        RenderHelper.drawCircle(0.0f, 0.0f, 3.0f / this.scale);
        GL11.glLineWidth(1.8f);
        GL11.glColor3f(0.6f, 0.56f, 1.0f);
        GL11.glEnable(2848);
        RenderHelper.drawCircleOutline(0.0f, 0.0f, 45.0f);
        GL11.glDisable(2848);
        component.getTheme().getFontRenderer().drawString(-component.getTheme().getFontRenderer().getStringWidth("+z") / 2, 45 - component.getTheme().getFontRenderer().getFontHeight(), "§7z+");
        GL11.glRotatef(90.0f, 0.0f, 0.0f, 1.0f);
        component.getTheme().getFontRenderer().drawString(-component.getTheme().getFontRenderer().getStringWidth("+x") / 2, 45 - component.getTheme().getFontRenderer().getFontHeight(), "§7x-");
        GL11.glRotatef(90.0f, 0.0f, 0.0f, 1.0f);
        component.getTheme().getFontRenderer().drawString(-component.getTheme().getFontRenderer().getStringWidth("-z") / 2, 45 - component.getTheme().getFontRenderer().getFontHeight(), "§7z-");
        GL11.glRotatef(90.0f, 0.0f, 0.0f, 1.0f);
        component.getTheme().getFontRenderer().drawString(-component.getTheme().getFontRenderer().getStringWidth("+x") / 2, 45 - component.getTheme().getFontRenderer().getFontHeight(), "§7x+");
        GlStateManager.func_179121_F();
        GlStateManager.func_179098_w();
        GL11.glTranslated((double)(-component.getWidth() / 2), (double)(-component.getHeight() / 2), 0.0);
    }
}
