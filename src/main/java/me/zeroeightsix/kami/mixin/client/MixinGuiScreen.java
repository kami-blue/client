package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.module.modules.render.CleanGUI;
import me.zeroeightsix.kami.module.modules.render.MapPreview;
import me.zeroeightsix.kami.module.modules.render.ShulkerPreview;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.MapData;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;
import static org.lwjgl.opengl.GL11.glDepthRange;

/**
 * Created by 086 on 24/12/2017.
 */
@Mixin(GuiScreen.class)
public class MixinGuiScreen {

    @Shadow public Minecraft mc;
    RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
    FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
    private static final ResourceLocation RES_MAP_BACKGROUND = new ResourceLocation("textures/map/map_background.png");

    @Inject(method = "renderToolTip", at = @At("HEAD"), cancellable = true)
    public void renderToolTip(ItemStack stack, int x, int y, CallbackInfo info) {
        if (MODULE_MANAGER.isModuleEnabled(ShulkerPreview.class) && stack.getItem() instanceof ItemShulkerBox) {
            NBTTagCompound tagCompound = stack.getTagCompound();
            if (tagCompound != null && tagCompound.hasKey("BlockEntityTag", 10)) {
                NBTTagCompound blockEntityTag = tagCompound.getCompoundTag("BlockEntityTag");
                if (blockEntityTag.hasKey("Items", 9)) {
                    // We'll take over!
                    info.cancel();

                    NonNullList<ItemStack> nonnulllist = NonNullList.<ItemStack>withSize(27, ItemStack.EMPTY);
                    ItemStackHelper.loadAllItems(blockEntityTag, nonnulllist);

                    GlStateManager.enableBlend();
                    GlStateManager.disableRescaleNormal();
                    RenderHelper.disableStandardItemLighting();
                    GlStateManager.disableLighting();
                    GlStateManager.disableDepth();


                    int width = Math.max(144, fontRenderer.getStringWidth(stack.getDisplayName()) + 3); //9*16

                    int x1 = x + 12;
                    int y1 = y - 12;
                    int height = 48 + 9; //3*16

                    this.itemRender.zLevel = 300.0F;
                    this.drawGradientRectP(x1 - 3, y1 - 4, x1 + width + 3, y1 - 3, -267386864, -267386864);
                    this.drawGradientRectP(x1 - 3, y1 + height + 3, x1 + width + 3, y1 + height + 4, -267386864, -267386864);
                    this.drawGradientRectP(x1 - 3, y1 - 3, x1 + width + 3, y1 + height + 3, -267386864, -267386864);
                    this.drawGradientRectP(x1 - 4, y1 - 3, x1 - 3, y1 + height + 3, -267386864, -267386864);
                    this.drawGradientRectP(x1 + width + 3, y1 - 3, x1 + width + 4, y1 + height + 3, -267386864, -267386864);
                    this.drawGradientRectP(x1 - 3, y1 - 3 + 1, x1 - 3 + 1, y1 + height + 3 - 1, 1347420415, 1344798847);
                    this.drawGradientRectP(x1 + width + 2, y1 - 3 + 1, x1 + width + 3, y1 + height + 3 - 1, 1347420415, 1344798847);
                    this.drawGradientRectP(x1 - 3, y1 - 3, x1 + width + 3, y1 - 3 + 1, 1347420415, 1347420415);
                    this.drawGradientRectP(x1 - 3, y1 + height + 2, x1 + width + 3, y1 + height + 3, 1344798847, 1344798847);

                    fontRenderer.drawString(stack.getDisplayName(), x + 12, y - 12, 0xffffff);

                    GlStateManager.enableBlend();
                    GlStateManager.enableAlpha();
                    GlStateManager.enableTexture2D();
                    GlStateManager.enableLighting();
                    GlStateManager.enableDepth();
                    RenderHelper.enableGUIStandardItemLighting();
                    for (int i = 0; i < nonnulllist.size(); i++) {
                        int iX = x + (i % 9) * 16 + 11;
                        int iY = y + (i / 9) * 16 - 11 + 8;
                        ItemStack itemStack = nonnulllist.get(i);

                        itemRender.renderItemAndEffectIntoGUI(itemStack, iX, iY);
                        itemRender.renderItemOverlayIntoGUI(this.fontRenderer, itemStack, iX, iY, null);
                    }
                    RenderHelper.disableStandardItemLighting();
                    this.itemRender.zLevel = 0.0F;

                    GlStateManager.enableLighting();
                    GlStateManager.enableDepth();
                    RenderHelper.enableStandardItemLighting();
                    GlStateManager.enableRescaleNormal();
                }
            }
        } else if (MODULE_MANAGER.isModuleEnabled(MapPreview.class) && stack.getItem() instanceof ItemMap) {
            MapData mapData = MapPreview.getMapData(stack);
            if (mapData == null) return;
            info.cancel();

            MapPreview mp = MODULE_MANAGER.getModuleT(MapPreview.class);
            int xl = x + 6;
            int yl = y + 6;

            GL11.glPushMatrix();
            GlStateManager.color(1f, 1f, 1f); // this dumbass shit is needed for some fucking reason???? thanks tiger for figuring this one out

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();

            GlStateManager.translate(xl, yl, 0.0);
            GlStateManager.scale(mp.getScale().getValue() / 5.0, mp.getScale().getValue() / 5.0, 0.0);
            RenderHelper.enableGUIStandardItemLighting(); // needed to make lighting work inside non inventory containers
            mc.getTextureManager().bindTexture(RES_MAP_BACKGROUND);

            /* taken from mc code, draw the maps frame */
            if (mp.getFrame().getValue()) {
                glDepthRange(0, 0.01); // fix drawing under other layers, just draw over everything
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
                bufferbuilder.pos(-7.0D, 135.0D, 0.0D).tex(0.0D, 1.0D).endVertex();
                bufferbuilder.pos(135.0D, 135.0D, 0.0D).tex(1.0D, 1.0D).endVertex();
                bufferbuilder.pos(135.0D, -7.0D, 0.0D).tex(1.0D, 0.0D).endVertex();
                bufferbuilder.pos(-7.0D, -7.0D, 0.0D).tex(0.0D, 0.0D).endVertex();
                tessellator.draw();
                glDepthRange(0, 1.0); // undo changes to rendering order
            }

            /* Draw the map */
            GlStateManager.disableDepth(); // needed to keep it on top of the frame
            glDepthRange(0, 0.01);
            mc.entityRenderer.getMapItemRenderer().renderMap(mapData, !mp.getFrame().getValue());
            glDepthRange(0, 1.0);
            GlStateManager.enableDepth(); // originally enabled

            RenderHelper.disableStandardItemLighting(); // originally disabled
            GL11.glPopMatrix();
        }
    }

    /**
     * @author cookiedragon234
     * see https://github.com/kami-blue/client/pull/293 for discussion
     * authors words:
     * Also @dominikaaaa you should be more careful with merging commits, especially from people who are new to coding. Stuff like this is obviously stolen, and can get your repository DMCA'd.
     *
     * as shown be the rest of his discussion, he was fine with it
     * I even aknowledged when it was added, after cookies approval, that it was pasted from backdoored. 
     */
    @Inject(method = "Lnet/minecraft/client/gui/GuiScreen;drawWorldBackground(I)V", at = @At("HEAD"), cancellable = true)
    private void drawWorldBackgroundWrapper(final int tint, final CallbackInfo ci) {
        if (this.mc.world != null && MODULE_MANAGER.isModuleEnabled(CleanGUI.class) && (MODULE_MANAGER.getModuleT(CleanGUI.class).inventoryGlobal.getValue())) {
            ci.cancel();
        }
    }

    private void drawGradientRectP(int left, int top, int right, int bottom, int startColor, int endColor) {
        float f = (float) (startColor >> 24 & 255) / 255.0F;
        float f1 = (float) (startColor >> 16 & 255) / 255.0F;
        float f2 = (float) (startColor >> 8 & 255) / 255.0F;
        float f3 = (float) (startColor & 255) / 255.0F;
        float f4 = (float) (endColor >> 24 & 255) / 255.0F;
        float f5 = (float) (endColor >> 16 & 255) / 255.0F;
        float f6 = (float) (endColor >> 8 & 255) / 255.0F;
        float f7 = (float) (endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(right, top, 300).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(left, top, 300).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(left, bottom, 300).color(f5, f6, f7, f4).endVertex();
        bufferbuilder.pos(right, bottom, 300).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

}
