package me.zeroeightsix.kami.util.text

import me.zeroeightsix.kami.util.color.EnumTextColor
import net.minecraft.util.text.TextFormatting

fun formatValue(any: Any) = TextFormatting.GRAY format "[$any]"

infix fun TextFormatting.format(any: Any) = "$this$any${TextFormatting.RESET}"

infix fun EnumTextColor.format(any: Any) = "$this$any${TextFormatting.RESET}"