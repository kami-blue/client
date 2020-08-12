// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.kami.theme.staticui;

import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.util.Wrapper;
import me.zeroeightsix.kami.gui.kami.KamiGUI;
import org.lwjgl.opengl.GL11;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;
import me.zeroeightsix.kami.gui.kami.component.TabGUI;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;

public class TabGuiUI extends AbstractComponentUI<TabGUI>
{
    long lastms;
    
    public TabGuiUI() {
        this.lastms = System.currentTimeMillis();
    }
    
    @Override
    public void renderComponent(final TabGUI component, final FontRenderer fontRenderer) {
        boolean updatelerp = false;
        final float difference = (float)(System.currentTimeMillis() - this.lastms);
        if (difference > 2.0f) {
            component.selectedLerpY += (component.selected * 10 - component.selectedLerpY) * difference * 0.02f;
            updatelerp = true;
            this.lastms = System.currentTimeMillis();
        }
        GL11.glDisable(2884);
        GL11.glDisable(3553);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glShadeModel(7425);
        GL11.glPushMatrix();
        final int x = 2;
        final int y = 2;
        GL11.glTranslatef((float)x, (float)y, 0.0f);
        this.drawBox(0, 0, component.width, component.height);
        KamiGUI.primaryColour.setGLColour();
        GL11.glColor3f(0.59f, 0.05f, 0.11f);
        GL11.glBegin(7);
        GL11.glVertex2d(0.0, (double)component.selectedLerpY);
        GL11.glVertex2d(0.0, (double)(component.selectedLerpY + 10.0f));
        GL11.glVertex2d((double)component.width, (double)(component.selectedLerpY + 10.0f));
        GL11.glVertex2d((double)component.width, (double)component.selectedLerpY);
        GL11.glEnd();
        int textY = 1;
        for (int i = 0; i < component.tabs.size(); ++i) {
            final String tabName = component.tabs.get(i).name;
            GL11.glEnable(3553);
            GL11.glColor3f(1.0f, 1.0f, 1.0f);
            Wrapper.getFontRenderer().drawStringWithShadow(2, textY, 255, 255, 255, "§7" + tabName);
            textY += 10;
        }
        if (component.tabOpened) {
            GL11.glPushMatrix();
            GL11.glDisable(3553);
            final TabGUI.Tab tab = component.tabs.get(component.selected);
            GL11.glTranslatef((float)(component.width + 2), 0.0f, 0.0f);
            this.drawBox(0, 0, tab.width, tab.height);
            if (updatelerp) {
                tab.lerpSelectY += (tab.selected * 10 - tab.lerpSelectY) * difference * 0.02f;
            }
            GL11.glColor3f(0.6f, 0.56f, 1.0f);
            GL11.glBegin(7);
            GL11.glVertex2d(0.0, (double)tab.lerpSelectY);
            GL11.glVertex2d(0.0, (double)(tab.lerpSelectY + 10.0f));
            GL11.glVertex2d((double)tab.width, (double)(tab.lerpSelectY + 10.0f));
            GL11.glVertex2d((double)tab.width, (double)tab.lerpSelectY);
            GL11.glEnd();
            int tabTextY = 1;
            for (int j = 0; j < tab.features.size(); ++j) {
                final Module feature = tab.features.get(j);
                final String fName = (feature.isEnabled() ? "§c" : "§7") + feature.getName();
                GL11.glEnable(3553);
                GL11.glColor3f(1.0f, 1.0f, 1.0f);
                Wrapper.getFontRenderer().drawStringWithShadow(2, tabTextY, 255, 255, 255, fName);
                tabTextY += 10;
            }
            GL11.glDisable(3089);
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        GL11.glEnable(2884);
    }
    
    private void drawBox(final int x1, final int y1, final int x2, final int y2) {
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.6f);
        GL11.glBegin(7);
        GL11.glVertex2i(x1, y1);
        GL11.glVertex2i(x2, y1);
        GL11.glVertex2i(x2, y2);
        GL11.glVertex2i(x1, y2);
        GL11.glEnd();
        double xi1 = x1 - 0.1;
        double xi2 = x2 + 0.1;
        double yi1 = y1 - 0.1;
        double yi2 = y2 + 0.1;
        GL11.glLineWidth(1.5f);
        GL11.glColor3f(0.59f, 0.05f, 0.11f);
        GL11.glBegin(2);
        GL11.glVertex2d(xi1, yi1);
        GL11.glVertex2d(xi2, yi1);
        GL11.glVertex2d(xi2, yi2);
        GL11.glVertex2d(xi1, yi2);
        GL11.glEnd();
        xi1 -= 0.9;
        xi2 += 0.9;
        yi1 -= 0.9;
        yi2 += 0.9;
        GL11.glBegin(9);
        GL11.glColor4f(0.125f, 0.125f, 0.125f, 0.75f);
        GL11.glVertex2d((double)x1, (double)y1);
        GL11.glVertex2d((double)x2, (double)y1);
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.0f);
        GL11.glVertex2d(xi2, yi1);
        GL11.glVertex2d(xi1, yi1);
        GL11.glVertex2d(xi1, yi2);
        GL11.glColor4f(0.125f, 0.125f, 0.125f, 0.75f);
        GL11.glVertex2d((double)x1, (double)y2);
        GL11.glEnd();
        GL11.glBegin(9);
        GL11.glVertex2d((double)x2, (double)y2);
        GL11.glVertex2d((double)x2, (double)y1);
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.0f);
        GL11.glVertex2d(xi2, yi1);
        GL11.glVertex2d(xi2, yi2);
        GL11.glVertex2d(xi1, yi2);
        GL11.glColor4f(0.125f, 0.125f, 0.125f, 0.75f);
        GL11.glVertex2d((double)x1, (double)y2);
        GL11.glEnd();
    }
}
