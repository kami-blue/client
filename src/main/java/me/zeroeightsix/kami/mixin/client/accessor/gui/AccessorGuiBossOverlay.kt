package me.zeroeightsix.kami.mixin.client.accessor.gui

import net.minecraft.client.gui.BossInfoClient
import net.minecraft.client.gui.GuiBossOverlay
import net.minecraft.world.BossInfo
import java.util.*

val GuiBossOverlay.mapBossInfos: Map<UUID, BossInfoClient>? get() = (this as AccessorGuiBossOverlay).mapBossInfos

fun GuiBossOverlay.render(x: Int, y: Int, info: BossInfo) = (this as AccessorGuiBossOverlay).invokeRender(x, y, info)