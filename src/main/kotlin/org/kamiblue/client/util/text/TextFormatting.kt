package org.kamiblue.client.util.text

import org.kamiblue.client.util.color.EnumTextColor
import net.minecraft.util.text.TextFormatting

fun formatValue(any: Any) = TextFormatting.GRAY format "[$any]"

fun formatValue(any: Int) = TextFormatting.GRAY format "($any)"

infix fun TextFormatting.format(any: Any) = "$this$any${TextFormatting.RESET}"

infix fun EnumTextColor.format(any: Any) = "$this$any${TextFormatting.RESET}"