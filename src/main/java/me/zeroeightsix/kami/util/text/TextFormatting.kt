package me.zeroeightsix.kami.util.text

import me.zeroeightsix.kami.util.color.EnumTextColor
import net.minecraft.util.text.TextFormatting

fun formatValue(string: String) = TextFormatting.GRAY format "[$string]"

fun <T : Number> formatValue(number: T) = TextFormatting.GRAY format "($number)"

infix fun <T : Number> TextFormatting.format(number: T) = "$this$number${TextFormatting.RESET}"

infix fun TextFormatting.format(string: String) = "$this$string${TextFormatting.RESET}"

infix fun <T : Number> EnumTextColor.format(number: T) = "$this$number${TextFormatting.RESET}"

infix fun EnumTextColor.format(string: String) = "$this$string${TextFormatting.RESET}"