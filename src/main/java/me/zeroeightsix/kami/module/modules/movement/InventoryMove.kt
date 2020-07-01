package me.zeroeightsix.kami.module.modules.movement

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PlayerUpdateMoveEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.GuiRepair
import net.minecraft.client.gui.inventory.GuiEditSign
import org.lwjgl.input.Keyboard

/**
 * @author dominikaaaa
 * @author ionar2
 * Created by dominikaaaa on 06/04/20
 * Updated on 04/05/20 by ionar2
 * https://github.com/ionar2/salhack/blob/fa9e383/src/main/java/me/ionar/salhack/module/movement/NoSlowModule.java
 * @see me.zeroeightsix.kami.mixin.client.MixinMovementInputFromOptions
 */
@Module.Info(
        name = "InventoryMove",
        description = "Allows you to walk around with GUIs opened",
        category = Module.Category.MOVEMENT
)
class InventoryMove : Module() {
    private val speed = register(Settings.i("LookSpeed", 10))
    var sneak: Setting<Boolean> = register(Settings.b("Sneak", false))

    @EventHandler
    private val sendListener = Listener(EventHook { event: PlayerUpdateMoveEvent ->
        if (mc.currentScreen != null && mc.currentScreen !is GuiChat && mc.currentScreen !is GuiEditSign && mc.currentScreen !is GuiRepair) {
            // pitch can not exceed 90 degrees nor -90 degrees, otherwise AAC servers will flag this and kick you.
            if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
                mc.player.rotationYaw = mc.player.rotationYaw - speed.value
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
                mc.player.rotationYaw = mc.player.rotationYaw + speed.value
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
                mc.player.rotationPitch = (mc.player.rotationPitch - speed.value).coerceAtLeast(-90f)
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
                mc.player.rotationPitch = (mc.player.rotationPitch + speed.value).coerceAtMost(90f)
            }

            mc.player.movementInput.moveStrafe = 0.0f
            mc.player.movementInput.moveForward = 0.0f

            if (Keyboard.isKeyDown(mc.gameSettings.keyBindForward.keyCode)) {
                ++mc.player.movementInput.moveForward
                mc.player.movementInput.forwardKeyDown = true
            } else {
                mc.player.movementInput.forwardKeyDown = false
            }

            if (Keyboard.isKeyDown(mc.gameSettings.keyBindBack.keyCode)) {
                --mc.player.movementInput.moveForward
                mc.player.movementInput.backKeyDown = true
            } else {
                mc.player.movementInput.backKeyDown = false
            }

            if (Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.keyCode)) {
                ++mc.player.movementInput.moveStrafe
                mc.player.movementInput.leftKeyDown = true
            } else {
                mc.player.movementInput.leftKeyDown = false
            }

            if (Keyboard.isKeyDown(mc.gameSettings.keyBindRight.keyCode)) {
                --mc.player.movementInput.moveStrafe
                mc.player.movementInput.rightKeyDown = true
            } else {
                mc.player.movementInput.rightKeyDown = false
            }

            if (Keyboard.isKeyDown(mc.gameSettings.keyBindJump.keyCode)) {
                mc.player.movementInput.jump = true
            }

            if (Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.keyCode) && sneak.value) {
                mc.player.movementInput.sneak = true
            }
        }
    })
}