package me.zeroeightsix.kami.gui.mc;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.util.WebUtils;
import net.minecraft.client.gui.*;

import java.net.URI;
import java.net.URISyntaxException;

import static me.zeroeightsix.kami.KamiMod.WEBSITE_LINK;

/**
 * Created by Dewy on 09/04/2020
 */
public class KamiGuiUpdateNotification extends GuiScreen {

    private final String title;
    private final String message;
    private final int singleOrMulti;

    public KamiGuiUpdateNotification(String title, String message, int singleOrMulti) {
        super();

        this.title = title;
        this.message = message;
        this.singleOrMulti = singleOrMulti;
    }

    @Override
    public void initGui() {
        super.initGui();

        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, 200, "Download Latest (Recommended)"));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, 230, KamiMod.colour + "cUse Current Version"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawGradientRect(0, 0, this.width, this.height, -12574688, -11530224);

        drawCenteredString(this.fontRenderer, this.title, this.width / 2, 80, 10260478);
        drawCenteredString(this.fontRenderer, this.message, this.width / 2, 110, 16777215);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
    }

    @Override
    protected void actionPerformed(GuiButton button) {

        if (button.id == 0) {
            try {
                WebUtils.openWebLink(new URI(WEBSITE_LINK + "/download"));

                if (singleOrMulti == 1) {
                    mc.displayGuiScreen(new GuiWorldSelection(new GuiMainMenu()));

                    return;
                }

                mc.displayGuiScreen(new GuiMultiplayer(new GuiMainMenu())); // Multi
            } catch (URISyntaxException e) {
                KamiMod.log.error("Contact the KAMI Blue developers. Download link could not be parsed into URI reference form.");
            }

            return;
        }

        if (singleOrMulti == 1) { // Single
            mc.displayGuiScreen(new GuiWorldSelection(new GuiMainMenu()));

            return;
        }

        mc.displayGuiScreen(new GuiMultiplayer(new GuiMainMenu())); // Multi
    }
}
