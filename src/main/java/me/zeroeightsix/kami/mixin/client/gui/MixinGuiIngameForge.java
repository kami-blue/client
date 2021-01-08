package me.zeroeightsix.kami.mixin.client.gui;

import me.zeroeightsix.kami.module.modules.player.Freecam;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.GuiIngameForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = GuiIngameForge.class, remap = false)
public class MixinGuiIngameForge {
    @ModifyVariable(method = "renderAir", at = @At(value = "STORE", ordinal = 0))
    private EntityPlayer renderAir$getRenderViewEntity(EntityPlayer renderViewEntity) {
        return gerRenderViewEntity(renderViewEntity);
    }

    @ModifyVariable(method = "renderHealth", at = @At(value = "STORE", ordinal = 0))
    private EntityPlayer renderHealth$getRenderViewEntity(EntityPlayer renderViewEntity) {
        return gerRenderViewEntity(renderViewEntity);
    }

    @ModifyVariable(method = "renderFood", at = @At(value = "STORE", ordinal = 0))
    private EntityPlayer renderFood$getRenderViewEntity(EntityPlayer renderViewEntity) {
        return gerRenderViewEntity(renderViewEntity);
    }

    @ModifyVariable(method = "renderHealthMount", at = @At(value = "STORE", ordinal = 0))
    private EntityPlayer renderHealthMount$getRenderViewEntity(EntityPlayer renderViewEntity) {
        return gerRenderViewEntity(renderViewEntity);
    }

    private EntityPlayer gerRenderViewEntity(EntityPlayer renderViewEntity) {
        EntityPlayer player = Wrapper.getPlayer();

        if (Freecam.INSTANCE.isEnabled() && player != null) {
            return player;
        } else {
            return renderViewEntity;
        }
    }
}
