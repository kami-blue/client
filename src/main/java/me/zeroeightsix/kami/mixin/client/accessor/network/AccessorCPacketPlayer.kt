package me.zeroeightsix.kami.mixin.client.accessor.network

import net.minecraft.network.play.client.CPacketPlayer

var CPacketPlayer.x: Double
    get() = this.getX(0.0)
    set(value) {
        (this as AccessorCPacketPlayer).setX(value)
    }

var CPacketPlayer.y: Double
    get() = this.getY(0.0)
    set(value) {
        (this as AccessorCPacketPlayer).setY(value)
    }

var CPacketPlayer.z: Double
    get() = this.getZ(0.0)
    set(value) {
        (this as AccessorCPacketPlayer).setZ(value)
    }

var CPacketPlayer.yaw: Float
    get() = this.getYaw(0.0f)
    set(value) {
        (this as AccessorCPacketPlayer).setYaw(value)
    }

var CPacketPlayer.pitch: Float
    get() = this.getYaw(0.0f)
    set(value) {
        (this as AccessorCPacketPlayer).setPitch(value)
    }

var CPacketPlayer.onGround: Boolean
    get() = this.isOnGround
    set(value) {
        (this as AccessorCPacketPlayer).setOnGround(value)
    }

val CPacketPlayer.moving: Boolean get() = (this as AccessorCPacketPlayer).moving

val CPacketPlayer.rotating: Boolean get() = (this as AccessorCPacketPlayer).rotating