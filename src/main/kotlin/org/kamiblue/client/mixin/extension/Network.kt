package org.kamiblue.client.mixin.extension

import net.minecraft.network.play.client.CPacketChatMessage
import net.minecraft.network.play.client.CPacketCloseWindow
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.network.play.server.SPacketChat
import net.minecraft.network.play.server.SPacketEntityVelocity
import net.minecraft.network.play.server.SPacketExplosion
import net.minecraft.network.play.server.SPacketPlayerPosLook
import net.minecraft.util.text.ITextComponent
import org.kamiblue.client.mixin.client.accessor.network.*

var CPacketChatMessage.packetMessage: String
    get() = this.message
    set(value) {
        (this as AccessorCPacketChatMessage).setMessage(value)
    }

val CPacketCloseWindow.windowID: Int
    get() = (this as AccessorCPacketCloseWindow).kbGetWindowID()


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
    get() = this.getPitch(0.0f)
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

var CPacketUseEntity.id: Int
    get() = (this as AccessorCPacketUseEntity).id
    set(value) {
        (this as AccessorCPacketUseEntity).id = value
    }

var CPacketUseEntity.packetAction: CPacketUseEntity.Action
    get() = this.action
    set(value) {
        (this as AccessorCPacketUseEntity).setAction(value)
    }

var SPacketChat.textComponent: ITextComponent
    get() = this.chatComponent
    set(value) {
        (this as AccessorSPacketChat).setChatComponent(value)
    }

var SPacketEntityVelocity.packetMotionX: Int
    get() = this.motionX
    set(value) {
        (this as AccessorSPacketEntityVelocity).setMotionX(value)
    }
var SPacketEntityVelocity.packetMotionY: Int
    get() = this.motionY
    set(value) {
        (this as AccessorSPacketEntityVelocity).setMotionY(value)
    }
var SPacketEntityVelocity.packetMotionZ: Int
    get() = this.motionZ
    set(value) {
        (this as AccessorSPacketEntityVelocity).setMotionZ(value)
    }

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

var SPacketPlayerPosLook.rotationYaw: Float
    get() = this.yaw
    set(value) {
        (this as AccessorSPacketPosLook).setYaw(value)
    }
var SPacketPlayerPosLook.rotationPitch: Float
    get() = this.pitch
    set(value) {
        (this as AccessorSPacketPosLook).setPitch(value)
    }