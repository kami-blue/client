package me.zeroeightsix.kami.mixin.client.gui;

import me.zeroeightsix.kami.module.modules.render.CleanGUI;
import me.zeroeightsix.kami.module.modules.render.MapPreview;
import me.zeroeightsix.kami.module.modules.render.ShulkerPreview;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.MapData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiScreen.class)
public class MixinGuiScreen {

    @Inject(method = "renderToolTip", at = @At("HEAD"), cancellable = true)
    public void renderToolTip(ItemStack stack, int x, int y, CallbackInfo info) {
        if (ShulkerPreview.INSTANCE.isEnabled() && stack.getItem() instanceof ItemShulkerBox) {
            NBTTagCompound tagCompound = ShulkerPreview.getShulkerData(stack);

            if (tagCompound != null) {
                info.cancel();
                ShulkerPreview.renderShulkerAndItems(stack, x, y, tagCompound);
            }
        } else if (MapPreview.INSTANCE.isEnabled() && stack.getItem() instanceof ItemMap) {
            MapData mapData = MapPreview.getMapData(stack);

            if (mapData != null) {
                info.cancel();
                MapPreview.drawMap(stack, mapData, x, y);
            }
        }
    }

    @Inject(method = "drawWorldBackground(I)V", at = @At("HEAD"), cancellable = true)
    private void drawWorldBackgroundWrapper(final int tint, final CallbackInfo ci) {
        if (Wrapper.getWorld() != null && CleanGUI.INSTANCE.isEnabled() && (CleanGUI.INSTANCE.getInventoryGlobal().getValue())) {
            ci.cancel();
        }
    }
}
