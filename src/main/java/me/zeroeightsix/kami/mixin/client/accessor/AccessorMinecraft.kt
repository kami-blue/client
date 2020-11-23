package me.zeroeightsix.kami.mixin.client.accessor

import net.minecraft.client.Minecraft
import net.minecraft.util.Timer

val Minecraft.timer: Timer get() = (this as AccessorMinecraft).timer

val Minecraft.renderPartialTicksPaused: Float get() = (this as AccessorMinecraft).renderPartialTicksPaused

var Minecraft.rightClickDelayTimer: Int
    get() = (this as AccessorMinecraft).rightClickDelayTimer
    set(value) {
        (this as AccessorMinecraft).rightClickDelayTimer = value
    }

fun Minecraft.rightClickMouse() = (this as AccessorMinecraft).invokeRightClickMouse()