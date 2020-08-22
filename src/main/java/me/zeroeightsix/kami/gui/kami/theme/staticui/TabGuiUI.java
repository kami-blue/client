package me.zeroeightsix.kami.gui.kami.theme.staticui;

import me.zeroeightsix.kami.gui.kami.KamiGUI;
import me.zeroeightsix.kami.gui.kami.component.TabGUI;
import me.zeroeightsix.kami.gui.rgui.render.AbstractComponentUI;
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.util.Wrapper;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.glColor3f;

/**
 * Created by 086 on 11/08/2017.
 */
public class TabGuiUI extends AbstractComponentUI<TabGUI> {

    long lastms = System.currentTimeMillis();

    @Override
    public void renderComponent(TabGUI component, FontRenderer fontRenderer) {
        boolean updatelerp = false;
        float difference = System.currentTimeMillis() - lastms;
        if (difference > 2) {
            component.selectedLerpY = component.selectedLerpY + ((component.selected * 10) - component.selectedLerpY) * difference * .02f;
            updatelerp = true;
            lastms = System.currentTimeMillis();
        }

        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);

        GL11.glPushMatrix();

        int x = 2;
        int y = 2;

        GL11.glTranslatef(x, y, 0);
        drawBox(0, 0, component.width, component.height);

        /*GL11.glScissor(x * factor, (sr.getScaledHeight() - height - y) * factor,
                width * factor, height * factor);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);*/

        KamiGUI.primaryColour.setGLColour();
        glColor3f(.59f, .05f, .11f);
        GL11.glBegin(GL11.GL_QUADS);
        {
            GL11.glVertex2d(0, component.selectedLerpY);
            GL11.glVertex2d(0, component.selectedLerpY + 10);
            GL11.glVertex2d(component.width, component.selectedLerpY + 10);
            GL11.glVertex2d(component.width, component.selectedLerpY);
        }
        GL11.glEnd();

        int textY = 1;
        for (int i = 0; i < component.tabs.size(); i++) {
            String tabName = component.tabs.get(i).name;
            /*if (i == selected)
                tabName = (tabOpened ? "<" : ">") + tabName;*/

            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glColor3f(1, 1, 1);
            Wrapper.getFontRenderer().drawStringWithShadow(2, textY, 255, 255, 255, "\u00A77" + tabName);
            textY += 10;
        }

        if (component.tabOpened) {
            GL11.glPushMatrix();
            GL11.glDisable(GL11.GL_TEXTURE_2D);

            TabGUI.Tab tab = component.tabs.get(component.selected);

            GL11.glTranslatef(component.width + 2, 0, 0);
            drawBox(0, 0, tab.width, tab.height);

            if (updatelerp)
                tab.lerpSelectY = tab.lerpSelectY + ((tab.selected * 10) - tab.lerpSelectY) * difference * .02f;

            glColor3f(.60f, .56f, 1.00f);
            GL11.glBegin(GL11.GL_QUADS);
            {
                GL11.glVertex2d(0, tab.lerpSelectY);
                GL11.glVertex2d(0, tab.lerpSelectY + 10);
                GL11.glVertex2d(tab.width, tab.lerpSelectY + 10);
                GL11.glVertex2d(tab.width, tab.lerpSelectY);
            }
            GL11.glEnd();

            int tabTextY = 1;
            for (int i = 0; i < tab.features.size(); i++) {
                Module feature = tab.features.get(i);
                String fName = (feature.isEnabled() ? "\u00A7c" : "\u00A77") + feature.getName();

                /*if (i == tab.selected)
                    fName = "\u00A7b" + fName;*/

                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glColor3f(1, 1, 1);
                Wrapper.getFontRenderer().drawStringWithShadow(2, tabTextY, 255, 255, 255, fName);
                tabTextY += 10;
            }

            GL11.glDisable(GL11.GL_SCISSOR_TEST);

            GL11.glPopMatrix();
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    private void drawBox(int x1, int y1, int x2, int y2) {
        // colour
        GL11.glColor4f(0, 0, 0, 0.6f);

        // box
        GL11.glBegin(GL11.GL_QUADS);
        {
            GL11.glVertex2i(x1, y1);
            GL11.glVertex2i(x2, y1);
            GL11.glVertex2i(x2, y2);
            GL11.glVertex2i(x1, y2);
        }
        GL11.glEnd();

        // outline positions
        double xi1 = x1 - 0.1;
        double xi2 = x2 + 0.1;
        double yi1 = y1 - 0.1;
        double yi2 = y2 + 0.1;

        // outline
        GL11.glLineWidth(1.5f);
        glColor3f(.59f, .05f, .11f);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        {
            GL11.glVertex2d(xi1, yi1);
            GL11.glVertex2d(xi2, yi1);
            GL11.glVertex2d(xi2, yi2);
            GL11.glVertex2d(xi1, yi2);
        }
        GL11.glEnd();

        // shadow positions
        xi1 -= 0.9;
        xi2 += 0.9;
        yi1 -= 0.9;
        yi2 += 0.9;

        // top left
        GL11.glBegin(GL11.GL_POLYGON);
        {
            GL11.glColor4f(0.125F, 0.125F, 0.125F, 0.75F);
            GL11.glVertex2d(x1, y1);
            GL11.glVertex2d(x2, y1);
            GL11.glColor4f(0, 0, 0, 0);
            GL11.glVertex2d(xi2, yi1);
            GL11.glVertex2d(xi1, yi1);
            GL11.glVertex2d(xi1, yi2);
            GL11.glColor4f(0.125F, 0.125F, 0.125F, 0.75F);
            GL11.glVertex2d(x1, y2);
        }
        GL11.glEnd();

        // bottom right
        GL11.glBegin(GL11.GL_POLYGON);
        {
            GL11.glVertex2d(x2, y2);
            GL11.glVertex2d(x2, y1);
            GL11.glColor4f(0, 0, 0, 0);
            GL11.glVertex2d(xi2, yi1);
            GL11.glVertex2d(xi2, yi2);
            GL11.glVertex2d(xi1, yi2);
            GL11.glColor4f(0.125F, 0.125F, 0.125F, 0.75F);
            GL11.glVertex2d(x1, y2);
        }
        GL11.glEnd();
    }

}
