package me.zeroeightsix.kami.mixin.client.accessor.entity

import net.minecraft.entity.Entity

val Entity.isInWeb: Boolean get() = (this as AccessorEntity).isInWeb