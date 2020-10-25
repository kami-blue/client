package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.module.modules.render.NoRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.renderer.tileentity.TileEntitySignRenderer;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TileEntitySignRenderer.class)
public class MixinTileEntitySignRenderer {
    private Minecraft mc = Minecraft.getMinecraft();

    @Redirect(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/tileentity/TileEntitySign;signText:[Lnet/minecraft/util/text/ITextComponent;", opcode = Opcodes.GETFIELD))
    public ITextComponent[] getRenderViewEntity(TileEntitySign sign) {
        if (NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.getSignText().getValue()) {
            if (mc.currentScreen instanceof GuiEditSign) {
                if (((GuiEditSign) mc.currentScreen).tileSign.equals(sign))
                    return sign.signText;
            }
            return new ITextComponent[]{};
        }
        return sign.signText;
    }
}
