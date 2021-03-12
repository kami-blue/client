package org.kamiblue.client.util

import org.kamiblue.client.setting.settings.AbstractSetting
import java.util.function.BooleanSupplier

val BOOLEAN_SUPPLIER_FALSE = BooleanSupplier { false }

fun <T : Any> AbstractSetting<T>.notAtValue(value: T) =
    BooleanSupplier {
        this.value != value
    }

fun <T : Any> AbstractSetting<T>.atValue(value: T) =
    BooleanSupplier {
        this.value == value
    }

fun <T : Any> AbstractSetting<T>.atValues(value1: T, value2: T) =
    BooleanSupplier {
        this.value == value1 || this.value == value2
    }

fun AbstractSetting<Boolean>.atTrue() =
    BooleanSupplier {
        this.value
    }

fun AbstractSetting<Boolean>.atFalse() =
    BooleanSupplier {
        !this.value
    }

infix fun BooleanSupplier.or(block: BooleanSupplier) =
    BooleanSupplier {
        this.asBoolean || block.asBoolean
    }

infix fun BooleanSupplier.and(block: BooleanSupplier) =
    BooleanSupplier {
        this.asBoolean && block.asBoolean
    }