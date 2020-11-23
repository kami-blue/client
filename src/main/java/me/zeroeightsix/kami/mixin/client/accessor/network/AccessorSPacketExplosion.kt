package me.zeroeightsix.kami.mixin.client.accessor.network

import net.minecraft.network.play.server.SPacketExplosion

var SPacketExplosion.packetMotionX: Float
    get() = this.motionX
    set(value) {
        (this as AccessorSPacketExplosion).setMotionX(value)
    }

var SPacketExplosion.packetMotionY: Float
    get() = this.motionY
    set(value) {
        (this as AccessorSPacketExplosion).setMotionY(value)
    }

var SPacketExplosion.packetMotionZ: Float
    get() = this.motionZ
    set(value) {
        (this as AccessorSPacketExplosion).setMotionZ(value)
    }