package me.zeroeightsix.kami.gui.mc;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.module.modules.misc.AntiDisconnect;
import me.zeroeightsix.kami.module.modules.movement.AutoWalk;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.realms.RealmsBridge;

public class KamiGuiAntiDisconnect extends GuiScreen {

    private int disconnectCount = KamiMod.MODULE_MANAGER.getModuleT(AntiDisconnect.class).requiredButtonPresses.getValue();

    @Override
    public void initGui() {
        super.initGui();

        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, 200, "Back to Game"));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, 230, String.format(KamiMod.colour + "cPress me %s time(s) to disconnect.", disconnectCount)));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                this.mc.displayGuiScreen(null);
                this.mc.setIngameFocus();

                break;
            case 1:
                if (disconnectCount > 1) {
                    disconnectCount--;

                    buttonList.remove(1);
                    buttonList.add(new GuiButton(1, this.width / 2 - 100, 230, String.format(KamiMod.colour + "cPress me %s time(s) to disconnect.", disconnectCount)));

                    break;
                }

                boolean single = mc.isIntegratedServerRunning();
                boolean connectedToRealms = mc.isConnectedToRealms();

                if (KamiMod.MODULE_MANAGER.isModuleEnabled(AutoWalk.class) && KamiMod.MODULE_MANAGER.getModuleT(AutoWalk.class).mode.getValue().equals(AutoWalk.AutoWalkMode.BARITONE)) {
                    if (button.id == 1) {
                        KamiMod.MODULE_MANAGER.getModuleT(AutoWalk.class).disable();
                    }
                }

                button.enabled = false;

                mc.world.sendQuittingDisconnectingPacket();
                mc.loadWorld(null);

                if (single)
                {
                    mc.displayGuiScreen(new GuiMainMenu());
                }
                else if (connectedToRealms)
                {
                    RealmsBridge realmsbridge = new RealmsBridge();

                    realmsbridge.switchToRealms(new GuiMainMenu());
                }
                else
                {
                    mc.displayGuiScreen(new GuiMultiplayer(new GuiMainMenu()));
                }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, "Disconnect Confirmation", this.width / 2, 40, 10195199);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
