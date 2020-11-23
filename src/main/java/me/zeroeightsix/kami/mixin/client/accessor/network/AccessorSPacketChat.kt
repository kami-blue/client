package me.zeroeightsix.kami.mixin.client.accessor.network

import net.minecraft.network.play.server.SPacketChat
import net.minecraft.util.text.ITextComponent

var SPacketChat.textComponent: ITextComponent
    get() = this.chatComponent
    set(value) {
        (this as AccessorSPacketChat).setChatComponent(value)
    }