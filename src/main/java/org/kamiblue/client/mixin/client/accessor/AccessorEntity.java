package org.kamiblue.client.mixin.client.accessor;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface AccessorEntity {

    @Accessor("isInWeb")
    boolean getIsInWeb();

}
