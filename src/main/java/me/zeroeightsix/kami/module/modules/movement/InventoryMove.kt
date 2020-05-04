package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import net.minecraft.client.gui.GuiChat
import org.lwjgl.input.Keyboard

/**
 * @author dominikaaaa
 * Created by dominikaaaa on 06/04/20
 * updated on 04/05/20 by ionar2
 * @see me.zeroeightsix.kami.mixin.client.MixinMovementInputFromOptions
 */
@Module.Info(
        name = "InventoryMove",
        description = "Allows you to walk around with GUIs opened",
        category = Module.Category.MOVEMENT
)
class InventoryMove : Module() {
    private val speed = register(Settings.i("Look speed", 10))
    @JvmField
    var sneak: Setting<Boolean> = register(Settings.b("Sneak", false))

    override fun onUpdate() {
        if (mc.player == null || mc.currentScreen == null || mc.currentScreen is GuiChat) return

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
    }
}