package org.kamiblue.client.mixin.client.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.GuiIngameForge;
import org.kamiblue.client.module.modules.player.Freecam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = GuiIngameForge.class, remap = false)
public class MixinGuiIngameForge {
    @ModifyVariable(method = "renderAir", at = @At(value = "STORE", ordinal = 0))
    private EntityPlayer renderAir$getRenderViewEntity(EntityPlayer renderViewEntity) {
        return Freecam.getRenderViewEntity(renderViewEntity);
    }

    @ModifyVariable(method = "renderHealth", at = @At(value = "STORE", ordinal = 0))
    private EntityPlayer renderHealth$getRenderViewEntity(EntityPlayer renderViewEntity) {
        return Freecam.getRenderViewEntity(renderViewEntity);
    }

    @ModifyVariable(method = "renderFood", at = @At(value = "STORE", ordinal = 0))
    private EntityPlayer renderFood$getRenderViewEntity(EntityPlayer renderViewEntity) {
        return Freecam.getRenderViewEntity(renderViewEntity);
    }

    @ModifyVariable(method = "renderHealthMount", at = @At(value = "STORE", ordinal = 0))
    private EntityPlayer renderHealthMount$getRenderViewEntity(EntityPlayer renderViewEntity) {
        return Freecam.getRenderViewEntity(renderViewEntity);
    }
}
