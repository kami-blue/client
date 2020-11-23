package me.zeroeightsix.kami.mixin.client.accessor

import me.zeroeightsix.kami.mixin.client.accessor.render.AccessorRenderManager
import net.minecraft.client.renderer.entity.RenderManager

val RenderManager.renderPosX: Double get() = (this as AccessorRenderManager).renderPosX

val RenderManager.renderPosY: Double get() = (this as AccessorRenderManager).renderPosY

val RenderManager.renderPosZ: Double get() = (this as AccessorRenderManager).renderPosZ