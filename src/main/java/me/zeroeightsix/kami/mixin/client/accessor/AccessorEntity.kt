package me.zeroeightsix.kami.mixin.client.accessor.entity

import me.zeroeightsix.kami.mixin.client.accessor.AccessorEntity
import net.minecraft.entity.Entity

val Entity.isInWeb: Boolean get() = (this as AccessorEntity).isInWeb