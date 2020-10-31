package me.zeroeightsix.kami.gui.rgui.windows

import me.zeroeightsix.kami.gui.rgui.component.*
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.impl.number.NumberSetting
import me.zeroeightsix.kami.setting.impl.other.BindSetting
import me.zeroeightsix.kami.setting.impl.other.ColorSetting
import me.zeroeightsix.kami.setting.impl.primitive.BooleanSetting
import me.zeroeightsix.kami.setting.impl.primitive.EnumSetting
import me.zeroeightsix.kami.setting.impl.primitive.StringSetting
import me.zeroeightsix.kami.util.math.Vec2f
import org.lwjgl.input.Keyboard

abstract class SettingWindow<T : Any>(
        name: String,
        val element: T,
        posX: Float,
        posY: Float,
        settingGroup: SettingGroup
) : ListWindow(name, posX, posY, 100.0f, 200.0f, settingGroup) {

    override val minWidth: Float get() = 100.0f
    override val minHeight: Float get() = draggableHeight

    override val minimizable get() = false

    var listeningChild: Slider? = null; private set
    private var initialized = false

    protected abstract fun getSettingList(): List<Setting<*>>

    override fun onGuiInit() {
        super.onGuiInit()
        if (!initialized) {
            for (setting in getSettingList()) {
                when (setting) {
                    is BooleanSetting -> SettingButton(setting)
                    is NumberSetting -> SettingSlider(setting)
                    is EnumSetting -> EnumSlider(setting)
                    is ColorSetting -> Button(setting.name, { listeningChild = it }, setting.description)
                    is StringSetting -> StringButton(setting)
                    is BindSetting -> BindButton(setting)
                    else -> null
                }?.also {
                    children.add(it)
                }
            }
            initialized = true
        }
    }

    override fun onDisplayed() {
        super.onDisplayed()
        lastActiveTime = System.currentTimeMillis() + 1000L
    }

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        (hoveredChild as? Slider)?.let {
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