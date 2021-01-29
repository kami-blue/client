package me.zeroeightsix.kami.setting.settings

import me.zeroeightsix.kami.setting.settings.impl.number.DoubleSetting
import me.zeroeightsix.kami.setting.settings.impl.number.FloatSetting
import me.zeroeightsix.kami.setting.settings.impl.number.IntegerSetting
import me.zeroeightsix.kami.setting.settings.impl.other.BindSetting
import me.zeroeightsix.kami.setting.settings.impl.other.ColorSetting
import me.zeroeightsix.kami.setting.settings.impl.primitive.BooleanSetting
import me.zeroeightsix.kami.setting.settings.impl.primitive.EnumSetting
import me.zeroeightsix.kami.setting.settings.impl.primitive.StringSetting
import me.zeroeightsix.kami.util.Bind
import me.zeroeightsix.kami.util.color.ColorHolder

/**
 * Setting register overloading
 *
 * @param T Type to have extension function for registering setting
 */
interface SettingRegister<T : Any> {

    /** Integer Setting */
    fun T.setting(
        name: String,
        value: Int,
        range: IntRange,
        step: Int,
        visibility: () -> Boolean = { true },
        consumer: (prev: Int, input: Int) -> Int = { _, input -> input },
        description: String = "",
        fineStep: Int = step,
    ) = setting(IntegerSetting(name, value, range, step, visibility, consumer, description, fineStep))

    /** Double Setting */
    fun T.setting(
        name: String,
        value: Double,
        range: ClosedFloatingPointRange<Double>,
        step: Double,
        visibility: () -> Boolean = { true },
        consumer: (prev: Double, input: Double) -> Double = { _, input -> input },
        description: String = "",
        fineStep: Double = step,
    ) = setting(DoubleSetting(name, value, range, step, visibility, consumer, description, fineStep))

    /** Float Setting */
    fun T.setting(
        name: String,
        value: Float,
        range: ClosedFloatingPointRange<Float>,
        step: Float,
        visibility: () -> Boolean = { true },
        consumer: (prev: Float, input: Float) -> Float = { _, input -> input },
        description: String = "",
        fineStep: Float = step,
    ) = setting(FloatSetting(name, value, range, step, visibility, consumer, description, fineStep))

    /** Bind Setting */
    fun T.setting(
        name: String,
        value: Bind,
        visibility: () -> Boolean = { true },
        description: String = ""
    ) = setting(BindSetting(name, value, visibility, description))

    /** Color Setting */
    fun T.setting(
        name: String,
        value: ColorHolder,
        hasAlpha: Boolean = true,
        visibility: () -> Boolean = { true },
        description: String = ""
    ) = setting(ColorSetting(name, value, hasAlpha, visibility, description))

    /** Boolean Setting */
    fun T.setting(
        name: String,
        value: Boolean,
        visibility: () -> Boolean = { true },
        consumer: (prev: Boolean, input: Boolean) -> Boolean = { _, input -> input },
        description: String = ""
    ) = setting(BooleanSetting(name, value, visibility, consumer, description))

    /** Enum Setting */
    fun <E : Enum<E>> T.setting(
        name: String,
        value: E,
        visibility: () -> Boolean = { true },
        consumer: (prev: E, input: E) -> E = { _, input -> input },
        description: String = ""
    ) = setting(EnumSetting(name, value, visibility, consumer, description))

    /** String Setting */
    fun T.setting(
        name: String,
        value: String,
        visibility: () -> Boolean = { true },
        consumer: (prev: String, input: String) -> String = { _, input -> input },
        description: String = ""
    ) = setting(StringSetting(name, value, visibility, consumer, description))
    /* End of setting registering */

    /**
     * Register a setting
     *
     * @param S Type of the setting
     * @param setting Setting to register
     *
     * @return [setting]
     */
    fun <S : AbstractSetting<*>> T.setting(setting: S): S
}