package me.zeroeightsix.kami.mixin.client.accessor.player

import net.minecraft.client.multiplayer.PlayerControllerMP

val PlayerControllerMP.currentPlayerItem: Int get() = (this as AccessorPlayerControllerMP).currentPlayerItem

fun PlayerControllerMP.syncCurrentPlayItem() = (this as AccessorPlayerControllerMP).invokeSyncCurrentPlayItem()