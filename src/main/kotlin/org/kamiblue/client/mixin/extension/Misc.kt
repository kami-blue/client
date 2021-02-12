package org.kamiblue.client.mixin.extension

import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.item.ItemTool
import net.minecraft.util.Timer
import org.kamiblue.client.mixin.client.accessor.AccessorEntity
import org.kamiblue.client.mixin.client.accessor.AccessorItemTool
import org.kamiblue.client.mixin.client.accessor.AccessorMinecraft
import org.kamiblue.client.mixin.client.accessor.AccessorTimer

val Entity.isInWeb: Boolean get() = (this as AccessorEntity).isInWeb

val ItemTool.attackDamage get() = (this as AccessorItemTool).attackDamage

val Minecraft.timer: Timer get() = (this as AccessorMinecraft).timer
val Minecraft.renderPartialTicksPaused: Float get() = (this as AccessorMinecraft).renderPartialTicksPaused
var Minecraft.rightClickDelayTimer: Int
    get() = (this as AccessorMinecraft).rightClickDelayTimer
    set(value) {
        (this as AccessorMinecraft).rightClickDelayTimer = value
    }

fun Minecraft.rightClickMouse() = (this as AccessorMinecraft).invokeRightClickMouse()

fun Minecraft.sendClickBlockToController(leftClick: Boolean) = (this as AccessorMinecraft).invokeSendClickBlockToController(leftClick)

var Timer.tickLength: Float
    get() = (this as AccessorTimer).tickLength
    set(value) {
        (this as AccessorTimer).tickLength = value
    }