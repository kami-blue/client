package me.zeroeightsix.kami.mixin.client.accessor.entity;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface AccessorEntity {

    @Accessor
    boolean getIsInWeb();

}
