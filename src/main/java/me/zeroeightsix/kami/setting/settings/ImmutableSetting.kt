package me.zeroeightsix.kami.setting.settings

import me.zeroeightsix.kami.util.translation.TranslationKey

/**
 * Basic ImmutableSetting class
 *
 * @param T Type of this setting
 * @param name Name of this setting
 * @param visibility Called by [isVisible]
 * @param consumer Called on setting [value] to process the value input
 * @param description Description of this setting
 */
abstract class ImmutableSetting<T : Any>(
    override val name: TranslationKey,
    valueIn: T,
    override val visibility: () -> Boolean,
    val consumer: (prev: T, input: T) -> T,
    override val description: TranslationKey
) : AbstractSetting<T>() {
    override val value: T = valueIn
    override val valueClass: Class<T> = valueIn.javaClass
}