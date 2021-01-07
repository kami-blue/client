package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.GuiRepair
import net.minecraft.client.gui.inventory.GuiEditSign
import net.minecraft.util.MovementInputFromOptions
import net.minecraftforge.client.event.InputUpdateEvent
import org.kamiblue.event.listener.listener
import org.lwjgl.input.Keyboard

object InventoryMove : Module(
    name = "InventoryMove",
    description = "Allows you to walk around with GUIs opened",
    category = Category.MOVEMENT
) {
    private val rotateSpeed = setting("RotateSpeed", 5, 0..20, 1)
    val sneak = setting("Sneak", false)

    private var hasSent = false

    init {
        listener<InputUpdateEvent> {
            if (it.movementInput !is MovementInputFromOptions || checkGui()) return@listener

            if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
                mc.player.rotationYaw = mc.player.rotationYaw - rotateSpeed.value
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
                mc.player.rotationYaw = mc.player.rotationYaw + rotateSpeed.value
            }

            // pitch can not exceed 90 degrees nor -90 degrees, otherwise AAC servers will flag this and kick you.
            if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
                mc.player.rotationPitch = (mc.player.rotationPitch - rotateSpeed.value).coerceAtLeast(-90.0f)
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
                mc.player.rotationPitch = (mc.player.rotationPitch + rotateSpeed.value).coerceAtMost(90.0f)
            }

            it.movementInput.moveStrafe = 0.0f
            it.movementInput.moveForward = 0.0f

            try {
                if (Keyboard.isKeyDown(mc.gameSettings.keyBindForward.keyCode)) {
                    ++it.movementInput.moveForward
                    it.movementInput.forwardKeyDown = true
                } else {
                    it.movementInput.forwardKeyDown = false
                }

                if (Keyboard.isKeyDown(mc.gameSettings.keyBindBack.keyCode)) {
                    --it.movementInput.moveForward
                    it.movementInput.backKeyDown = true
                } else {
                    it.movementInput.backKeyDown = false
                }

                if (Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.keyCode)) {
                    ++it.movementInput.moveStrafe
                    it.movementInput.leftKeyDown = true
                } else {
                    it.movementInput.leftKeyDown = false
                }

                if (Keyboard.isKeyDown(mc.gameSettings.keyBindRight.keyCode)) {
                    --it.movementInput.moveStrafe
                    it.movementInput.rightKeyDown = true
                } else {
                    it.movementInput.rightKeyDown = false
                }

                if (Keyboard.isKeyDown(mc.gameSettings.keyBindJump.keyCode)) {
                    it.movementInput.jump = true
                }

                if (Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.keyCode) && sneak.value) {
                    it.movementInput.sneak = true
                }
            } catch (e: IndexOutOfBoundsException) {
                if (!hasSent) {
                    KamiMod.LOG.error("$chatName Error: Key is bound to a mouse button!")
                    e.printStackTrace()
                    hasSent = true
                }
            }
        }
    }

    private fun checkGui() = mc.currentScreen == null
        || mc.currentScreen is GuiChat
        || mc.currentScreen is GuiEditSign
        || mc.currentScreen is GuiRepair
}