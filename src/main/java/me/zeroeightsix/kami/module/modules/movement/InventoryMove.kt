package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.PlayerUpdateMoveEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.event.listener
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.GuiRepair
import net.minecraft.client.gui.inventory.GuiEditSign
import org.lwjgl.input.Keyboard

@Module.Info(
        name = "InventoryMove",
        description = "Allows you to walk around with GUIs opened",
        category = Module.Category.MOVEMENT
)
object InventoryMove : Module() {
    private val rotateSpeed = register(Settings.integerBuilder("RotateSpeed").withValue(5).withRange(0, 20).withStep(1))
    val sneak = register(Settings.b("Sneak", false))

    private var hasSent = false

    init {
        listener<PlayerUpdateMoveEvent> {
            if (mc.currentScreen == null || mc.currentScreen is GuiChat || mc.currentScreen is GuiEditSign || mc.currentScreen is GuiRepair) return@listener

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

            mc.player.movementInput.moveStrafe = 0.0f
            mc.player.movementInput.moveForward = 0.0f

            try {
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
            } catch (e: IndexOutOfBoundsException) {
                if (!hasSent) {
                    KamiMod.log.error("$chatName Error: Key is bound to a mouse button!")
                    e.printStackTrace()
                    hasSent = true
                }
            }
        }
    }
}