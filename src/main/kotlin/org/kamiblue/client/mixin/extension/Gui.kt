package org.kamiblue.client.mixin.extension

import net.minecraft.client.gui.*
import net.minecraft.client.gui.inventory.GuiEditSign
import net.minecraft.tileentity.TileEntitySign
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.BossInfo
import org.kamiblue.client.mixin.client.accessor.gui.AccessorGuiBossOverlay
import org.kamiblue.client.mixin.client.accessor.gui.AccessorGuiChat
import org.kamiblue.client.mixin.client.accessor.gui.AccessorGuiDisconnected
import org.kamiblue.client.mixin.client.accessor.gui.AccessorGuiEditSign
import java.util.*

val GuiBossOverlay.mapBossInfos: Map<UUID, BossInfoClient>? get() = (this as AccessorGuiBossOverlay).mapBossInfos
fun GuiBossOverlay.render(x: Int, y: Int, info: BossInfo) = (this as AccessorGuiBossOverlay).invokeRender(x, y, info)

var GuiChat.historyBuffer: String
    get() = (this as AccessorGuiChat).historyBuffer
    set(value) {
        (this as AccessorGuiChat).historyBuffer = value
    }
var GuiChat.sentHistoryCursor: Int
    get() = (this as AccessorGuiChat).sentHistoryCursor
    set(value) {
        (this as AccessorGuiChat).sentHistoryCursor = value
    }

val GuiDisconnected.parentScreen: GuiScreen get() = (this as AccessorGuiDisconnected).parentScreen
val GuiDisconnected.reason: String get() = (this as AccessorGuiDisconnected).reason
val GuiDisconnected.message: ITextComponent get() = (this as AccessorGuiDisconnected).message

val GuiEditSign.tileSign: TileEntitySign get() = (this as AccessorGuiEditSign).tileSign
val GuiEditSign.editLine: Int get() = (this as AccessorGuiEditSign).editLine