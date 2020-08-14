package me.zeroeightsix.kami.gui.kami.theme.staticui;

import me.zeroeightsix.kami.gui.kami.RenderHelper;
import me.zeroeightsix.kami.gui.kami.component.Radar;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;
import me.zeroeightsix.kami.util.EntityUtils;
import me.zeroeightsix.kami.util.Friends;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by 086 on 11/08/2017.
 */
public class RadarUI extends AbstractComponentUI<Radar> {

    float scale = 2;
    public final static int radius = 45;

    @Override
    public void handleSizeComponent(Radar component) {
        component.setWidth(radius * 2);
        component.setHeight(radius * 2);
    }

    @Override
    public void renderComponent(Radar component, FontRenderer fontRenderer) {
        scale = 2;
        GL11.glTranslated(component.getWidth() / 2.0, component.getHeight() / 2.0, 0);

        GlStateManager.disableCull();

        GlStateManager.pushMatrix();
        //GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        glColor4f(.11f, .11f, .11f, 0.6f);
        RenderHelper.drawCircle(0, 0, radius);

        GL11.glRotatef(Wrapper.getPlayer().rotationYaw + 180, 0, 0, -1);

        for (Entity e : Wrapper.getWorld().loadedEntityList) {
            if (e == null || e.isDead) continue;
            float red = 1f;
            float green = 1f;
            float blue = 1f;

            if (EntityUtils.isPassiveMob(e) || Friends.isFriend(e.getName())) { // green
                red = 0;
                blue = 0;
            } else if (EntityUtils.isCurrentlyNeutral(e)) { // blue
                green = 0;
                red = 0;
            } else { // red
                blue = 0;
                green = 0;
            }

            double dX = e.posX - Wrapper.getPlayer().posX;
            double dZ = e.posZ - Wrapper.getPlayer().posZ;

            double distance = Math.sqrt(Math.pow(dX, 2) + Math.pow(dZ, 2));

            if (distance > radius * scale || Math.abs(Wrapper.getPlayer().posY - e.posY) > 30)
                continue;

            glColor4f(red, green, blue, 0.5f);
            RenderHelper.drawCircle((int) dX / scale, (int) dZ / scale, 2.5f / scale);
        }

        glColor3f(1f, 1f, 1f);
        RenderHelper.drawCircle(0, 0, 3 / scale);

        GL11.glLineWidth(1.8f);
//        glColor4f(1,1,1,1f);
        glColor3f(.60f, .56f, 1.00f);
        RenderHelper.drawCircleOutline(0, 0, radius);

        component.getTheme().getFontRenderer().drawString(-component.getTheme().getFontRenderer().getStringWidth("+z") / 2, radius - component.getTheme().getFontRenderer().getFontHeight(), "\u00A77z+");
        glRotatef(90, 0, 0, 1);
        component.getTheme().getFontRenderer().drawString(-component.getTheme().getFontRenderer().getStringWidth("+x") / 2, radius - component.getTheme().getFontRenderer().getFontHeight(), "\u00A77x-");
        glRotatef(90, 0, 0, 1);
        component.getTheme().getFontRenderer().drawString(-component.getTheme().getFontRenderer().getStringWidth("-z") / 2, radius - component.getTheme().getFontRenderer().getFontHeight(), "\u00A77z-");
        glRotatef(90, 0, 0, 1);
        component.getTheme().getFontRenderer().drawString(-component.getTheme().getFontRenderer().getStringWidth("+x") / 2, radius - component.getTheme().getFontRenderer().getFontHeight(), "\u00A77x+");

        GlStateManager.popMatrix();

        GL11.glTranslated(-component.getWidth() / 2.0, -component.getHeight() / 2.0, 0);
    }
}
