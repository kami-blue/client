// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.mixin.client;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.Tessellator;
import me.zeroeightsix.kami.module.modules.gui.CleanGUI;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.util.NonNullList;
import net.minecraft.item.ItemShulkerBox;
import me.zeroeightsix.kami.module.ModuleManager;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderItem;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({ GuiScreen.class })
public class MixinGuiScreen
{
    @Shadow
    public Minecraft field_146297_k;
    RenderItem itemRender;
    FontRenderer fontRenderer;
    
    public MixinGuiScreen() {
        this.itemRender = Minecraft.func_71410_x().func_175599_af();
        this.fontRenderer = Minecraft.func_71410_x().field_71466_p;
    }
    
    @Inject(method = { "renderToolTip" }, at = { @At("HEAD") }, cancellable = true)
    public void renderToolTip(final ItemStack stack, final int x, final int y, final CallbackInfo info) {
        if (ModuleManager.isModuleEnabled("ShulkerPreview") && stack.func_77973_b() instanceof ItemShulkerBox) {
            final NBTTagCompound tagCompound = stack.func_77978_p();
            if (tagCompound != null && tagCompound.func_150297_b("BlockEntityTag", 10)) {
                final NBTTagCompound blockEntityTag = tagCompound.func_74775_l("BlockEntityTag");
                if (blockEntityTag.func_150297_b("Items", 9)) {
                    info.cancel();
                    final NonNullList<ItemStack> nonnulllist = (NonNullList<ItemStack>)NonNullList.func_191197_a(27, (Object)ItemStack.field_190927_a);
                    ItemStackHelper.func_191283_b(blockEntityTag, (NonNullList)nonnulllist);
                    GlStateManager.func_179147_l();
                    GlStateManager.func_179101_C();
                    RenderHelper.func_74518_a();
                    GlStateManager.func_179140_f();
                    GlStateManager.func_179097_i();
                    final int width = Math.max(144, this.fontRenderer.func_78256_a(stack.func_82833_r()) + 3);
                    final int x2 = x + 12;
                    final int y2 = y - 12;
                    final int height = 57;
                    this.itemRender.field_77023_b = 300.0f;
                    this.drawGradientRectP(x2 - 3, y2 - 4, x2 + width + 3, y2 - 3, -267386864, -267386864);
                    this.drawGradientRectP(x2 - 3, y2 + height + 3, x2 + width + 3, y2 + height + 4, -267386864, -267386864);
                    this.drawGradientRectP(x2 - 3, y2 - 3, x2 + width + 3, y2 + height + 3, -267386864, -267386864);
                    this.drawGradientRectP(x2 - 4, y2 - 3, x2 - 3, y2 + height + 3, -267386864, -267386864);
                    this.drawGradientRectP(x2 + width + 3, y2 - 3, x2 + width + 4, y2 + height + 3, -267386864, -267386864);
                    this.drawGradientRectP(x2 - 3, y2 - 3 + 1, x2 - 3 + 1, y2 + height + 3 - 1, 1347420415, 1344798847);
                    this.drawGradientRectP(x2 + width + 2, y2 - 3 + 1, x2 + width + 3, y2 + height + 3 - 1, 1347420415, 1344798847);
                    this.drawGradientRectP(x2 - 3, y2 - 3, x2 + width + 3, y2 - 3 + 1, 1347420415, 1347420415);
                    this.drawGradientRectP(x2 - 3, y2 + height + 2, x2 + width + 3, y2 + height + 3, 1344798847, 1344798847);
                    this.fontRenderer.func_78276_b(stack.func_82833_r(), x + 12, y - 12, 16777215);
                    GlStateManager.func_179147_l();
                    GlStateManager.func_179141_d();
                    GlStateManager.func_179098_w();
                    GlStateManager.func_179145_e();
                    GlStateManager.func_179126_j();
                    RenderHelper.func_74520_c();
                    for (int i = 0; i < nonnulllist.size(); ++i) {
                        final int iX = x + i % 9 * 16 + 11;
                        final int iY = y + i / 9 * 16 - 11 + 8;
                        final ItemStack itemStack = (ItemStack)nonnulllist.get(i);
                        this.itemRender.func_180450_b(itemStack, iX, iY);
                        this.itemRender.func_180453_a(this.fontRenderer, itemStack, iX, iY, (String)null);
                    }
                    RenderHelper.func_74518_a();
                    this.itemRender.field_77023_b = 0.0f;
                    GlStateManager.func_179145_e();
                    GlStateManager.func_179126_j();
                    RenderHelper.func_74519_b();
                    GlStateManager.func_179091_B();
                }
            }
        }
    }
    
    @Inject(method = { "Lnet/minecraft/client/gui/GuiScreen;drawWorldBackground(I)V" }, at = { @At("HEAD") }, cancellable = true)
    private void drawWorldBackgroundWrapper(final int tint, final CallbackInfo ci) {
        if (this.field_146297_k.field_71441_e != null && ModuleManager.isModuleEnabled("CleanGUI") && ((CleanGUI)ModuleManager.getModuleByName("CleanGUI")).inventoryGlobal.getValue()) {
            ci.cancel();
        }
    }
    
    private void drawGradientRectP(final int left, final int top, final int right, final int bottom, final int startColor, final int endColor) {
        final float f = (startColor >> 24 & 0xFF) / 255.0f;
        final float f2 = (startColor >> 16 & 0xFF) / 255.0f;
        final float f3 = (startColor >> 8 & 0xFF) / 255.0f;
        final float f4 = (startColor & 0xFF) / 255.0f;
        final float f5 = (endColor >> 24 & 0xFF) / 255.0f;
        final float f6 = (endColor >> 16 & 0xFF) / 255.0f;
        final float f7 = (endColor >> 8 & 0xFF) / 255.0f;
        final float f8 = (endColor & 0xFF) / 255.0f;
        GlStateManager.func_179090_x();
        GlStateManager.func_179147_l();
        GlStateManager.func_179118_c();
        GlStateManager.func_187428_a(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.func_179103_j(7425);
        final Tessellator tessellator = Tessellator.func_178181_a();
        final BufferBuilder bufferbuilder = tessellator.func_178180_c();
        bufferbuilder.func_181668_a(7, DefaultVertexFormats.field_181706_f);
        bufferbuilder.func_181662_b((double)right, (double)top, 300.0).func_181666_a(f2, f3, f4, f).func_181675_d();
        bufferbuilder.func_181662_b((double)left, (double)top, 300.0).func_181666_a(f2, f3, f4, f).func_181675_d();
        bufferbuilder.func_181662_b((double)left, (double)bottom, 300.0).func_181666_a(f6, f7, f8, f5).func_181675_d();
        bufferbuilder.func_181662_b((double)right, (double)bottom, 300.0).func_181666_a(f6, f7, f8, f5).func_181675_d();
        tessellator.func_78381_a();
        GlStateManager.func_179103_j(7424);
        GlStateManager.func_179084_k();
        GlStateManager.func_179141_d();
        GlStateManager.func_179098_w();
    }
}
