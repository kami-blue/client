package me.zeroeightsix.kami.gui.rgui.windows

import me.zeroeightsix.kami.gui.rgui.component.AbstractSlider
import me.zeroeightsix.kami.util.math.Vec2f
import org.lwjgl.input.Keyboard

abstract class SettingWindow<T: Any>(
        name: String,
        val element: T,
        posX: Float,
        posY: Float,
        settingGroup: SettingGroup
) : ListWindow(name, posX, posY, 100.0f, 200.0f, settingGroup) {

    override val minWidth: Float get() = 100.0f
    override val minHeight: Float get() = draggableHeight

    override val minimizable get() = false

    var listeningChild: AbstractSlider? = null; private set

    override fun onDisplayed() {
        super.onDisplayed()
        lastActiveTime = System.currentTimeMillis() + 1000L
    }

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        (hoveredChild as? AbstractSlider)?.let {
            listeningChild = if (it.listening) it
            else null
        }
    }

    override fun onTick() {
        super.onTick()
        if (listeningChild?.listening == false) listeningChild = null
        Keyboard.enableRepeatEvents(listeningChild != null)
    }

    override fun onClosed() {
        super.onClosed()
        listeningChild = null
    }

    override fun onKeyInput(keyCode: Int, keyState: Boolean) {
        listeningChild?.onKeyInput(keyCode, keyState)
    }

}