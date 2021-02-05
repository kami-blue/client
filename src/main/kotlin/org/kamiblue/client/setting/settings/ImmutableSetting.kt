package org.kamiblue.client.setting.settings

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
    override val name: String,
    valueIn: T,
    override val visibility: () -> Boolean,
    val consumer: (prev: T, input: T) -> T,
    override val description: String
) : AbstractSetting<T>() {
    override val value: T = valueIn
    override val valueClass: Class<T> = valueIn.javaClass
}