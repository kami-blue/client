package org.kamiblue.client.via.viaforge.gui;


import org.kamiblue.client.via.viafabric.ViaFabric;
import org.kamiblue.client.via.viafabric.util.ProtocolUtils;
import org.kamiblue.client.via.viaforge.utils.ProtocolSorter;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class GuiProtocolSelector extends GuiScreen {

    public SlotList list;

    private GuiScreen parent;

    public GuiProtocolSelector(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.add(new GuiButton(1, width / 2 - 100, height - 27, 200,
                20, "Back"));

        list = new SlotList(mc, width, height, 32, height - 32, 10);
    }

    @Override
    protected void actionPerformed(GuiButton p_actionPerformed_1_) throws IOException {
        list.actionPerformed(p_actionPerformed_1_);

        if (p_actionPerformed_1_.id == 1)
            mc.displayGuiScreen(parent);
    }

    @Override
    public void handleMouseInput() throws IOException {
        list.handleMouseInput();
        super.handleMouseInput();
    }

    @Override
    public void drawScreen(int p_drawScreen_1_, int p_drawScreen_2_, float p_drawScreen_3_) {
        list.drawScreen(p_drawScreen_1_, p_drawScreen_2_, p_drawScreen_3_);

        GL11.glPushMatrix();
        GL11.glScalef(2.0F, 2.0F, 2.0F);
        this.drawCenteredString(this.fontRenderer, ChatFormatting.GOLD.toString() + "Kami Version",
                this.width / 4, 6, 16777215);
        GL11.glPopMatrix();

        super.drawScreen(p_drawScreen_1_, p_drawScreen_2_, p_drawScreen_3_);
    }

    class SlotList extends GuiSlot {


        public SlotList(Minecraft p_i1052_1_, int p_i1052_2_, int p_i1052_3_, int p_i1052_4_, int p_i1052_5_, int p_i1052_6_) {
            super(p_i1052_1_, p_i1052_2_, p_i1052_3_, p_i1052_4_, p_i1052_5_, p_i1052_6_);
        }

        @Override
        protected int getSize() {
            return ProtocolSorter.getProtocolVersions().size();
        }

        @Override
        protected void elementClicked(int i, boolean b, int i1, int i2) {
            ViaFabric.clientSideVersion = ProtocolSorter.getProtocolVersions().get(i).getVersion();
        }

        @Override
        protected boolean isSelected(int i) {
            return false;
        }

        @Override
        protected void drawBackground() {
            drawDefaultBackground();
        }


        @Override
        protected void drawSlot(int i, int i1, int i2, int unknown, int i3, int i4, float i5) {
            drawCenteredString(mc.fontRenderer,(ViaFabric.clientSideVersion ==
                    ProtocolSorter.getProtocolVersions().get(i).getVersion() ? ChatFormatting.GREEN.toString() :
                    ChatFormatting.DARK_RED.toString()) + ProtocolUtils.getProtocolName(ProtocolSorter.
                            getProtocolVersions().get(i).getVersion()), width / 2, i2 + 2, -1);
        }
    }
}
