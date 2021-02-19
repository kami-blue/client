package org.kamiblue.client.mixin.extension

import net.minecraft.client.multiplayer.PlayerControllerMP
import org.kamiblue.client.mixin.client.accessor.player.AccessorPlayerControllerMP

var PlayerControllerMP.blockHitDelay: Int
    get() = (this as AccessorPlayerControllerMP).blockHitDelay
    set(value) {
        (this as AccessorPlayerControllerMP).blockHitDelay = value
    }

val PlayerControllerMP.currentPlayerItem: Int get() = (this as AccessorPlayerControllerMP).currentPlayerItem

fun PlayerControllerMP.syncCurrentPlayItem() = (this as AccessorPlayerControllerMP).kb_invokeSyncCurrentPlayItem()