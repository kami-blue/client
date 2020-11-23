package me.zeroeightsix.kami.mixin.client.accessor.network

import net.minecraft.network.play.client.CPacketChatMessage

var CPacketChatMessage.packetMessage: String
    get() = this.message
    set(value) {
        (this as AccessorCPacketChatMessage).setMessage(value)
    }