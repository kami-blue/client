package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.module.modules.render.NoRender;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Idea from littlebroto1
 */
@Mixin(MapItemRenderer.Instance.class)
public class MixinMapItemRenderer {

    @Shadow
    @Final
    private ResourceLocation location;

    @Redirect(method = "render",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/MapItemRenderer$Instance;location:Lnet/minecraft/util/ResourceLocation;", opcode = Opcodes.GETFIELD))
    public ResourceLocation render(MapItemRenderer.Instance i) {
        if (NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.getMap().getValue())
            return new ResourceLocation("kamiblue/kamimap.png");
        else return this.location;
    }
}
