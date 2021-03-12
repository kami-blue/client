package org.kamiblue.client.setting.settings

import org.kamiblue.client.setting.settings.impl.number.DoubleSetting
import org.kamiblue.client.setting.settings.impl.number.FloatSetting
import org.kamiblue.client.setting.settings.impl.number.IntegerSetting
import org.kamiblue.client.setting.settings.impl.other.BindSetting
import org.kamiblue.client.setting.settings.impl.other.ColorSetting
import org.kamiblue.client.setting.settings.impl.primitive.BooleanSetting
import org.kamiblue.client.setting.settings.impl.primitive.EnumSetting
import org.kamiblue.client.setting.settings.impl.primitive.StringSetting
import org.kamiblue.client.util.Bind
import org.kamiblue.client.util.color.ColorHolder
import java.util.function.BooleanSupplier

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

    fun <T : Any> AbstractSetting<T>.atValue(page: T): () -> Boolean = {
        this.value == page
    }

    fun <T : Any> AbstractSetting<T>.atValue(page: T, block: BooleanSupplier): () -> Boolean = {
        this.value == page && block.asBoolean
    }

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